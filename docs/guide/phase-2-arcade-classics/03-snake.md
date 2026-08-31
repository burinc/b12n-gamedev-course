# Lesson 3: Snake

## Concepts

This lesson introduces three key ideas that make Snake mechanically different from every earlier game:

1. **Discrete Grid Movement**: the snake moves on a fixed grid, advancing one cell at a time on a timer (`move-interval`), not every frame. This is the first game in this ladder that *isn't* continuous per-frame motion.
2. **Growing Data Structure**: the snake's body is a vector that grows when it eats food and shrinks (via tail removal) when it moves without eating. A single `step-snake` function handles both cases.
3. **Self-Collision Detection**: the snake can crash into its own body, ending the game. The collision check is simple: verify the new head position doesn't overlap the rest of the body.

You'll implement a single-player Snake where you steer with arrow keys, eat food to grow, and avoid hitting yourself or the walls (which wrap around).

## Starter Code

Open `exercises/phase_2/snake_starter.clj` and fill in the two TODOs:

```clojure
(ns phase-2.snake-starter
  "Phase 2, Lesson 3, Snake. Arrow keys steer; can't reverse directly
   into yourself. Moves on a fixed grid tick, not every frame, the
   first game in this ladder that isn't continuous motion."
  (:require [net.b12n.game-dev.engine.game-loop :as game-loop]
            [net.b12n.game-dev.engine.raylib.core.keyboard :as keyboard]
            [net.b12n.game-dev.engine.raylib.enums :as enums]
            [net.b12n.game-dev.engine.raylib.shapes.basic :as shapes]
            [net.b12n.game-dev.engine.raylib.text.drawing :as text]
            [net.b12n.game-dev.engine.raylib.colors :as colors]))

(def cell-size 20)
(def cols 32)
(def rows 24)
(def width (* cols cell-size))
(def height (* rows cell-size))
(def move-interval 0.12) ;; seconds between grid steps

(defn- rand-cell [] {:x (rand-int cols) :y (rand-int rows)})

(defn- rand-free-cell
  "A random cell not occupied by any of `occupied` (the snake's own body).
   Food that spawns inside the snake is both unreachable-without-dying and,
   for most spawns, simply invisible under the snake, never place it
   there.

   Computes the actual free-cell set rather than rejection-sampling
   rand-cell against `taken`: rejection sampling is simpler but can loop
   forever if the board is ever completely full. That's unreachable
   during normal play (it means the snake filled the whole board), but a
   defensive fallback beats a silent hang if it ever somehow happens."
  [occupied]
  (let [taken (set occupied)
        free  (remove taken (for [x (range cols) y (range rows)] {:x x :y y}))]
    (if (seq free)
      (rand-nth free)
      (rand-cell))))

(defn init []
  (let [snake [{:x 16 :y 12} {:x 15 :y 12} {:x 14 :y 12}]]
    {:snake        snake
     :direction    [1 0]
     :pending-dir  [1 0]
     :food         (rand-free-cell snake)
     :move-timer   0.0
     :status       :playing}))

(def ^:private opposite {[1 0] [-1 0] [-1 0] [1 0] [0 1] [0 -1] [0 -1] [0 1]})

(defn- read-direction [{:keys [direction] :as world}]
  ;; TODO: Implement direction reading using keyboard input.
  ;; Similar pattern to solution: check if any arrow key is pressed,
  ;; store the wanted direction, and update :pending-dir only if
  ;; the wanted direction is not the opposite of current direction.
  world)

(defn- step-snake [{:keys [snake direction food] :as world}]
  ;; TODO: Implement snake movement and collision detection.
  ;; Compute the new head position by moving from current head in the direction.
  ;; Decide whether to grow (if new head equals food) or slide (remove tail).
  ;; Detect self-collision: check if new head collides with rest of body.
  ;; Return :lost status if collision, otherwise update snake and food -
  ;; and when you respawn food after eating, use `rand-free-cell` (not
  ;; `rand-cell`) on the NEW body, or food can spawn inside the snake
  ;; itself, which is unreachable without dying.
  world)

(defn- tick [world dt]
  (if (not= :playing (:status world))
    world
    (let [world (read-direction world)
          timer (+ (:move-timer world) dt)]
      (if (>= timer move-interval)
        (-> world
            (assoc :direction (:pending-dir world))
            (assoc :move-timer (- timer move-interval))
            step-snake)
        (assoc world :move-timer timer)))))

(defn- draw-cell [{:keys [x y]} color]
  (shapes/draw-rectangle! (* x cell-size) (* y cell-size) (dec cell-size) (dec cell-size) color))

(defn- draw [{:keys [snake food status]}]
  (draw-cell food colors/red)
  (doseq [segment snake] (draw-cell segment colors/green))
  (when (= :lost status)
    (text/draw-text! "GAME OVER" 220 200 40 colors/raywhite)))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Snake"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/black}))
```

## Run It

From the repo root:

```bash
clojure -M:run -m phase-2.snake-starter
```

Use arrow keys to steer the snake. Eat the red food square to grow; avoid hitting yourself or the walls wrap around, they don't stop you, they just bring you out the other side.

## Hints

### `read-direction`

- Each frame, `dt` (delta time) tells you how many seconds have passed since the last frame.
- Use `keyboard/is-key-pressed?` with keys from `enums/keyboard-key` (e.g., `:right`, `:left`, `:up`, `:down`). This function returns true once per press (not held), which is ideal for direction input.
- Store the wanted direction, but only update `:pending-dir` if the wanted direction is **not** the opposite of the current direction (using the `opposite` map).
- The `opposite` map prevents you from reversing directly into your own body: if you're moving right `[1 0]`, pressing left `[-1 0]` is ignored until the next grid step.

### `step-snake`

The key insight: **grow if you ate food, slide if you didn't.**

1. **Compute the new head** by taking the current head `(first snake)` and moving it in the `:direction` by one grid cell.
   - Use `mod` for wrapping: `(mod (+ x dx) cols)` for the x-coordinate, `(mod (+ y dy) rows)` for y.
2. **Decide grow-vs-slide**: compare the new head position with `:food`.
   - If they're equal, you ate food: `body = [new-head] + (entire old snake)`.
   - If not, you're sliding: `body = [new-head] + (all but last of old snake)` via `butlast`.
3. **Detect self-collision**: check if `new-head` appears anywhere in `(rest body)`.
   - Use `(some #(= new-head %) (rest body))` to test.
   - If there's a collision, return `(assoc world :snake body :status :lost)`: include the updated `:snake body`, not just `:status`, so the final drawn frame actually shows the head touching the body instead of the position one tick earlier.
4. **Update food if eaten** via `(assoc :food (rand-free-cell body))`: the NEW `body` (post-move), not `snake`. **Use `rand-free-cell`, not plain `rand-cell`, here.** Plain `rand-cell` doesn't check the snake's own position, so it will sometimes place food directly under a body segment, and reaching that food is unavoidable death: on the tick you eat it, `body` still contains that segment (it's the whole old snake, since you grew instead of sliding), so `hit-self?` sees your new head land on a cell that's *also* still occupied by the segment you just "ate," and the collision check fires. `rand-free-cell` (defined above `init`) rejects any candidate cell the snake currently occupies before returning one.

### Discrete Grid Movement vs. Continuous

Unlike Pong and Breakout, Snake's movement **is not every frame**. The `tick` function accumulates `:move-timer` until it reaches `move-interval` (0.12 seconds). Only then does `step-snake` run.

This means:
- The snake always moves at a consistent speed regardless of frame rate.
- Input is read every frame via `read-direction`, but `:pending-dir` is only applied at the next grid step.
- `read-direction` runs even when the timer hasn't elapsed yet, it's non-blocking and just updates state.

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/snake.clj` to compare your implementation.

### A Note on Direction Input

You'll notice that `read-direction` uses `is-key-pressed?` (a single event per press) rather than `is-key-down?` (held). This prevents spam-queueing direction changes during a single grid step, you can only queue one new direction per grid tick. The `:pending-dir` field acts as a buffer: the direction you wanted is stored, and applied at the next grid step.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **Jolt + raylib-jlt:** [`raylib-jlt/src/net/b12n/raylib_jlt/snake.clj`](https://github.com/jlt-commons/raylib-jlt/blob/main/src/net/b12n/raylib_jlt/snake.clj), classic snake, arrow keys, grow, don't crash.

---

**Next:** [Lesson 4: Space Invaders](04-space-invaders.md)
