# Lesson 6: Flappy Bird

## Concepts

This lesson combines several core mechanics into a complete, polished arcade game and applies **finite-state machines at the game-flow level** — a different facet from Tetris's, which used one to drive gameplay phases within a single round:

1. **Infinite-Scroll Illusion** — pipes scroll left continuously, but are regenerated off-screen to the right. The background always feels fresh because obstacles loop invisibly.
2. **Procedural Obstacle Generation** — each pipe's gap height is random, making every game different. Spacing and spawning are deterministic; content is not.
3. **Single-Input Physics** — gravity pulls the bird down every frame; a single input (Space) inverts velocity instantly. No sustained keys or complex controls—one button, physics-driven motion.
4. **Game-Flow State Machine** — the game lives in one of three states: `:title` (waiting to start), `:playing` (active gameplay), and `:over` (crashed, waiting to restart). Pressing Space in `:over` returns to `:title`; a second Space press then starts a fresh game in `:playing`.

You'll implement the classic Flappy Bird game where you navigate through scrolling pipe gaps by tapping Space, with scoring for each pipe cleared.

## Starter Code

Open `exercises/phase_2/flappy_bird_starter.clj` and fill in the three TODOs:

```clojure
(ns phase-2.flappy-bird-starter
  "Phase 2, Lesson 6 — Flappy Bird. Space flaps. Space also starts the
   game from the title screen and restarts it after game over — one key,
   three states."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def width 640)
(def height 480)
(def bird-x 120)
(def bird-radius 12)
(def gravity 900.0)
(def flap-vy -320.0)
(def pipe-w 60)
(def pipe-gap 140)
(def pipe-speed 180.0)
(def pipe-spacing 260) ;; horizontal distance between pipe spawns

(defn- new-pipe [x]
  {:x x :gap-y (+ 80 (rand-int (- height 160 pipe-gap))) :scored? false})

(defn init []
  {:status  :title ;; :title, :playing, :over
   :bird-y  (double (/ height 2))
   :bird-vy 0.0
   :pipes   [(new-pipe width) (new-pipe (+ width pipe-spacing))]
   :score   0})

(defn- flap-pressed? [] (keyboard/is-key-pressed? (:space enums/keyboard-key)))

(defn- physics [world dt]
  ;; TODO: apply gravity to `:bird-vy` each frame, override it with
  ;; `flap-vy` on a flap press, then integrate `:bird-y`.
  world)

(defn- move-pipes [{:keys [pipes score] :as world} dt]
  (let [moved     (mapv (fn [p] (update p :x - (* pipe-speed dt))) pipes)
        passed?   (fn [p] (and (not (:scored? p)) (< (+ (:x p) pipe-w) bird-x)))
        score'    (+ score (count (filter passed? moved)))
        moved     (mapv (fn [p] (if (passed? p) (assoc p :scored? true) p)) moved)
        kept      (vec (remove (fn [p] (< (+ (:x p) pipe-w) 0)) moved))
        rightmost (apply max (map :x kept))
        kept      (if (< rightmost (- width pipe-spacing))
                    ;; TODO: spawn a new pipe once the rightmost one has
                    ;; scrolled far enough left (hint: pipe-spacing).
                    kept
                    kept)]
    (assoc world :pipes kept :score score')))

(defn- collides? [bird-y pipes]
  ;; TODO: bird-vs-floor/ceiling, plus bird-vs-each-pipe (the pipe gap
  ;; is centered on `gap-y` with total height `pipe-gap`).
  false)

(defn- tick [world dt]
  (case (:status world)
    :title (if (flap-pressed?) (assoc world :status :playing) world)
    :over  (if (flap-pressed?) (init) world)
    :playing
    (let [world (-> world (physics dt) (move-pipes dt))]
      (if (collides? (:bird-y world) (:pipes world))
        (assoc world :status :over)
        world))))

(defn- draw [{:keys [status bird-y pipes score]}]
  (doseq [{:keys [x gap-y]} pipes]
    (shapes/draw-rectangle! (int x) 0 pipe-w (int (- gap-y (/ pipe-gap 2))) colors/green)
    (shapes/draw-rectangle! (int x) (int (+ gap-y (/ pipe-gap 2))) pipe-w
                            (- height (int (+ gap-y (/ pipe-gap 2)))) colors/green))
  (shapes/draw-circle! bird-x (int bird-y) bird-radius colors/yellow)
  (text/draw-text! (str score) (- (/ width 2) 10) 20 30 colors/raywhite)
  (case status
    :title (text/draw-text! "SPACE to start" 210 240 20 colors/raywhite)
    :over  (text/draw-text! "GAME OVER — SPACE to retry" 140 240 20 colors/red)
    nil))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Flappy Bird"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/skyblue}))
```

## Run It

From the repo root:

```bash
clojure -M:run -m phase-2.flappy-bird-starter
```

Press Space to start. Space is your only control—it instantly flaps the bird upward. Fly through the gaps in the pipes without hitting the top, bottom, or a pipe. Each pipe you pass counts as one point.

## Hints

### `physics`

- Gravity accelerates the bird downward: `vy' = vy + gravity * dt`.
- On a flap press, override velocity instantly: `vy'' = (if (flap-pressed?) flap-vy vy')`.
- Integrate position: `y' = y + vy'' * dt`.
- Return a world with `:bird-y` and `:bird-vy` updated.

### `move-pipes`

- Pipes scroll left at `pipe-speed` pixels per second.
- A pipe is "passed" if it has not been scored yet AND its right edge is behind the bird's x position.
- Each passed pipe increments the score exactly once (the `:scored?` flag prevents double-counting).
- Pipes that scroll off the left edge are removed (their right edge `+ pipe-w` is less than 0).
- When the rightmost pipe has scrolled far enough left (more than `pipe-spacing` pixels from the right edge), spawn a new pipe.

### `collides?`

Collision occurs if:

1. **Bird hits top or bottom**: `bird-y < bird-radius` or `bird-y > height - bird-radius`.
2. **Bird hits a pipe**: for each pipe, check if the bird's circle overlaps the pipe's rectangular gap.
   - The gap is centered on `gap-y` with half-height `pipe-gap / 2`.
   - The bird (at position `bird-x`) collides with the pipe if:
     - The bird's horizontal range `[bird-x - bird-radius, bird-x + bird-radius]` overlaps the pipe's horizontal range `[x, x + pipe-w]`.
     - AND the bird's vertical range `[bird-y - bird-radius, bird-y + bird-radius]` does NOT overlap the gap's vertical range `[gap-y - pipe-gap/2, gap-y + pipe-gap/2]`.

### State Machine

The game's three states are:
- `:title` — show the start prompt, transition to `:playing` on Space.
- `:playing` — run physics and collision; transition to `:over` on collision.
- `:over` — show game-over, transition back to a fresh `:title` (via `init`) on Space. A second Space press then starts a new `:playing` game.

Notice that `:title` and `:over` **don't run physics or collision** — they're idle states waiting for input. Only `:playing` updates the world. The `case` statement in `tick` encodes this cleanly.

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/flappy_bird.clj` to compare your implementation.

### Score Increments Once Per Pipe, Not Continuously

You'll notice that the `:scored?` flag prevents the same pipe from incrementing score multiple times. Without this flag, the score would increase every frame the bird is inside the pipe's gap—clearly wrong. The flag is set to `true` the moment the bird passes the pipe, ensuring each pipe contributes exactly 1 point.

### Procedural Pipe Generation

Every new pipe's gap height is random (within the range that keeps it on-screen). This makes the game replayable: the same code produces different challenges each time you play.

## Closing Phase 2: The Arcade Classics Ladder

Over six lessons, you've built an entire arcade-game suite from scratch, each one teaching a distinct game-design pattern:

1. **Pong** — Two-paddle physics and AABB collision (Lesson 1)
2. **Breakout** — Grid-based block collision and a clear-the-board win condition (Lesson 2)
3. **Snake** — Discrete grid movement, screen-edge wrapping, and self-collision on a growing body (Lesson 3)
4. **Space Invaders** — Projectile management and direction-aware formation movement (Lesson 4)
5. **Tetris** — Rotation in 2D, line-clearing state machine, and multi-step turns (Lesson 5)
6. **Flappy Bird** — Infinite-scroll illusion, procedural generation, and three-state title/play/over flow (Lesson 6)

Each game reuses the same rendering and input engine, but encodes different game loops, physics models, and state machines. You now understand:
- How to structure a game around explicit state machines.
- How to detect collisions between different shapes (circles, rectangles, grids).
- How to manage dynamic obstacle lists (bullets, pipes, falling blocks).
- How to create the illusion of an infinite world by wrapping or regenerating off-screen.
- How delta-time movement ensures consistent gameplay across frame rates.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **Jolt + raylib-jlt:** [`b12n-raylib-jlt/src/net/b12n/raylib_jlt/flappy_bird.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/flappy_bird.clj) — flap through scrolling pipe gaps (SPACE).

---

**Next:** [Phase 3: Three Lisps](../phase-3-four-lisps/index.md)
