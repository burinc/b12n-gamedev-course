# Lesson 1: Pong

## Concepts

This lesson introduces three core game mechanics:

1. **Delta-time movement** — how to scale motion based on elapsed time so gameplay feels consistent across different frame rates (even though we'll take a shortcut here for simplicity).
2. **AABB collision** — axis-aligned bounding box overlap tests to detect when the ball hits a paddle.
3. **Score as world state** — tracking score as part of your game's state dictionary, updated during gameplay and rendered each frame.

You'll implement a two-player Pong game where you control the left paddle (W to move up, S to move down) and a simple AI tracks the ball on the right side.

## Starter Code

Open `exercises/phase_2/pong_starter.clj` and fill in the three TODOs:

```clojure
(ns phase-2.pong-starter
  "Phase 2, Lesson 1 — Pong. Left paddle is you (W/S), right paddle is a
   simple tracking AI. First serve to 0 points wins nothing — this is
   about the loop, not a tournament."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def width 640)
(def height 480)
(def paddle-width 12)
(def paddle-height 80)
(def paddle-speed 300.0)
(def ball-radius 8)
(def ball-speed 4.0)
(def ^:private left-paddle-x 0)
(def ^:private right-paddle-x (- width paddle-width))

(defn- clamp [v lo hi] (max lo (min v hi)))

(defn- init-ball []
  {:x (double (/ width 2)) :y (double (/ height 2)) :dx ball-speed :dy ball-speed})

(defn init []
  {:left-y     (double (/ (- height paddle-height) 2))
   :right-y    (double (/ (- height paddle-height) 2))
   :ball       (init-ball)
   :left-score 0
   :right-score 0})

(defn- move-left-paddle [world dt]
  ;; TODO: read :w/:s via keyboard/is-key-down?
  ;; and move :left-y, clamped to the window.
  world)

(defn- move-right-ai [world dt]
  (let [ball-y  (get-in world [:ball :y])
        target  (clamp (- ball-y (/ paddle-height 2)) 0 (- height paddle-height))
        current (:right-y world)
        step    (clamp (- target current) (- (* paddle-speed dt)) (* paddle-speed dt))]
    (update world :right-y + step)))

(defn- hits-paddle? [ball-x ball-y paddle-x paddle-y]
  ;; TODO: AABB overlap test between the ball (a
  ;; ball-radius-sized square is close enough) and a paddle rect.
  false)

(defn- move-ball [{:keys [ball left-y right-y left-score right-score] :as world} _dt]
  ;; TODO: advance the ball, bounce off top/bottom, bounce
  ;; off a paddle when hits-paddle? is true, score + reset when it
  ;; passes an edge. Hint: get the wall-bounce case working first, using
  ;; -main to watch the ball do that, before adding paddle bounce and
  ;; scoring.
  world)

(defn- tick [world dt]
  (-> world
      (move-left-paddle dt)
      (move-right-ai dt)
      (move-ball dt)))

(defn- draw [{:keys [left-y right-y ball left-score right-score]}]
  (shapes/draw-rectangle! left-paddle-x (int left-y) paddle-width paddle-height colors/raywhite)
  (shapes/draw-rectangle! right-paddle-x (int right-y) paddle-width paddle-height colors/raywhite)
  (shapes/draw-circle! (int (:x ball)) (int (:y ball)) ball-radius colors/raywhite)
  (text/draw-text! (str left-score) (- (/ width 2) 40) 20 40 colors/raywhite)
  (text/draw-text! (str right-score) (+ (/ width 2) 20) 20 40 colors/raywhite))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Pong"
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
clojure -M:run -m phase-2.pong-starter
```

When both paddles are controlled by the AI, watch the ball bounce. Then implement `move-left-paddle` so you can play: use W to move up, S to move down.

## Hints

### `move-left-paddle`

- Each frame, `dt` (delta time) tells you how many seconds have passed since the last frame.
- Movement distance is `paddle-speed * dt` — this scales motion to elapsed time.
- Use `keyboard/is-key-down?` with keys from `enums/keyboard-key` (e.g., `:w` and `:s`).
- Keep the paddle within the window using `clamp`: `(clamp new-y 0 (- height paddle-height))`.

### `hits-paddle?`

- A ball traveling as a circle and a paddle as a rectangle need an overlap test.
- Treat the ball as a square (width and height = `2 * ball-radius`) for simplicity.
- Check if the ball's bounding box overlaps the paddle's bounding box:
  - Ball left edge: `(- ball-x ball-radius)`, Ball right edge: `(+ ball-x ball-radius)`
  - Ball top edge: `(- ball-y ball-radius)`, Ball bottom edge: `(+ ball-y ball-radius)`
  - Paddle left edge: `paddle-x`, Paddle right edge: `(+ paddle-x paddle-width)`
  - Paddle top edge: `paddle-y`, Paddle bottom edge: `(+ paddle-y paddle-height)`
- Two rectangles overlap if they overlap on both axes.

### `move-ball`

- Start by handling wall bounces (top and bottom). Update the ball's `y` and `dy` (vertical velocity).
  - If the ball goes above the top (`y' < ball-radius`), clamp it and flip the sign of `dy`.
  - If the ball goes below the bottom (`y' > height - ball-radius`), clamp it and flip the sign of `dy`.
- Next, add paddle bounces:
  - Check if the ball is moving left (`dx < 0`) and hits the left paddle. If so, reposition the ball just past the paddle and flip `dx` to positive.
  - Check if the ball is moving right (`dx > 0`) and hits the right paddle. If so, reposition the ball just past the paddle and flip `dx` to negative.
- Finally, add scoring:
  - If the ball goes off the left edge (`x' < 0`), the right player scores and the ball resets to center.
  - If the ball goes off the right edge (`x' > width`), the left player scores and the ball resets to center.

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/pong.clj` to compare your implementation.

### A Note on Delta Time

You'll notice that the ball's `dx` and `dy` velocities are **not** scaled by `dt` — the ball moves by raw pixel amounts each frame (`dx` pixels per frame, `dy` pixels per frame), not time-scaled. This is a deliberate simplification for this lesson: it only looks correct at a fixed target frame rate (here, 60 FPS). Real games scale velocity by `dt`, like the bouncing-ball demo in Phase 1, Lesson 2.

This is a **limitation of the current approach** — frame-rate-dependent gameplay is fragile. Phase 4's "fixed timestep" lesson exists specifically to solve this problem properly. For now, understand that this works at 60 FPS but would look wrong on a 30 FPS device or a 120 FPS display. That's a preview of why time-scaled movement matters.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **Jolt + raylib-jlt:** [`b12n-raylib-jlt/src/net/b12n/raylib_jlt/pong.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/pong.clj) — two-paddle classic, you (W/S) vs a ball-tracking CPU.
- **Clojure (JVM) + raylib-clj:** [`b12n-raylib-clj/src/examples/pong.clj`](https://github.com/burinc/b12n-raylib-clj/blob/main/src/examples/pong.clj) — another Pong variant for comparison.

---

**Next:** [Lesson 2: Breakout](02-breakout.md)
