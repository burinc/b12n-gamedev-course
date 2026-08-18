# Lesson 3: Snake

## Concepts

This lesson introduces three key ideas that make Snake mechanically different from every earlier game:

1. **Discrete Grid Movement** — the snake moves on a fixed grid, advancing one cell at a time on a timer (`move-interval`), not every frame. This is the first game in this ladder that *isn't* continuous per-frame motion.
2. **Growing Data Structure** — the snake's body is a vector that grows when it eats food and shrinks (via tail removal) when it moves without eating. A single `step-snake` function handles both cases.
3. **Self-Collision Detection** — the snake can crash into its own body, ending the game. The collision check is simple: verify the new head position doesn't overlap the rest of the body.

You'll implement a single-player Snake where you steer with arrow keys, eat food to grow, and avoid hitting yourself or the walls (which wrap around).

## Starter Code

Open `exercises/phase_2/snake_starter.clj` and fill in the two TODOs:

```clojure
(ns phase-2.snake-starter
  "Phase 2, Lesson 3 — Snake. Arrow keys steer; can't reverse directly
   into yourself. Moves on a fixed grid tick, not every frame — the
   first game in this ladder that isn't continuous motion."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def cell-size 20)
(def cols 32)
(def rows 24)
(def width (* cols cell-size))
(def height (* rows cell-size))
(def move-interval 0.12) ;; seconds between grid steps

(defn- rand-cell [] {:x (rand-int cols) :y (rand-int rows)})

(defn init []
  {:snake        [{:x 16 :y 12} {:x 15 :y 12} {:x 14 :y 12}]
   :direction    [1 0]
   :pending-dir  [1 0]
   :food         (rand-cell)
   :move-timer   0.0
   :status       :playing})

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
  ;; Return :lost status if collision, otherwise update snake and food.
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

Use arrow keys to steer the snake. Eat the red food square to grow; avoid hitting yourself or the walls wrap around — they don't stop you, they just bring you out the other side.

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
   - If there's a collision, return `(assoc world :status :lost)`.
4. **Update food if eaten** via `(assoc :food (rand-cell))`.

### Discrete Grid Movement vs. Continuous

Unlike Pong and Breakout, Snake's movement **is not every frame**. The `tick` function accumulates `:move-timer` until it reaches `move-interval` (0.12 seconds). Only then does `step-snake` run.

This means:
- The snake always moves at a consistent speed regardless of frame rate.
- Input is read every frame via `read-direction`, but `:pending-dir` is only applied at the next grid step.
- `read-direction` runs even when the timer hasn't elapsed yet — it's non-blocking and just updates state.

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/snake.clj` to compare your implementation.

### A Note on Direction Input

You'll notice that `read-direction` uses `is-key-pressed?` (a single event per press) rather than `is-key-down?` (held). This prevents spam-queueing direction changes during a single grid step — you can only queue one new direction per grid tick. The `:pending-dir` field acts as a buffer: the direction you wanted is stored, and applied at the next grid step.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **ClojureDart + raylib-jlt:** [`b12n-raylib-jlt/src/net/b12n/raylib_jlt/snake.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/snake.clj) — classic snake — arrow keys, grow, don't crash.

---

**Next:** [Lesson 4: Space Invaders](04-space-invaders.md) *(coming soon)*
