# Lesson 2: Breakout

## Concepts

This lesson introduces two key new mechanics:

1. **Collision Response** — when the ball hits the paddle, the angle it bounces at depends on *where* it hit. Hitting near the paddle's edges sends the ball out at shallow angles; hitting near the center sends it straighter up.
2. **Destructible Objects and Level Layout as Data** — a grid of bricks that can be destroyed, represented as a vector of booleans and rendered from procedural layout constants.

You'll implement a single-player Breakout (Brick Breaker) game where you control a paddle at the bottom to bounce a ball up and clear all the bricks. The ball's outgoing angle from the paddle depends on the hit location — a core mechanic of the original Breakout arcade cabinet.

## Starter Code

Open `exercises/phase_2/breakout_starter.clj` and fill in the three TODOs:

```clojure
(ns phase-2.breakout-starter
  "Phase 2, Lesson 2 — Breakout. A/D or Left/Right move the paddle; where
   the ball hits the paddle changes the angle it leaves at, same as the
   original."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def width 640)
(def height 480)
(def paddle-width 100)
(def paddle-height 16)
(def paddle-y (- height 40))
(def paddle-speed 320.0)
(def ball-radius 8)
(def ball-speed 4.0)
(def rows 5)
(def cols 10)
(def brick-w 56)
(def brick-h 20)
(def brick-gap 4)
(def brick-top 60)
(def brick-left (/ (- width (* cols (+ brick-w brick-gap))) 2))
(def row-colors [colors/red colors/orange colors/gold colors/green colors/skyblue])

(defn- brick-rect [i]
  (let [row (quot i cols) col (mod i cols)]
    {:x (+ brick-left (* col (+ brick-w brick-gap)))
     :y (+ brick-top (* row (+ brick-h brick-gap)))
     :w brick-w :h brick-h :color (nth row-colors row)}))

(defn init []
  {:paddle-x (double (/ (- width paddle-width) 2))
   :ball     {:x (double (/ width 2)) :y (double (- paddle-y ball-radius 40))
              :dx (* ball-speed 0.6) :dy (- ball-speed)}
   :bricks   (vec (repeat (* rows cols) true))
   :status   :playing}) ;; :playing, :won, :lost

(defn- clamp [v lo hi] (max lo (min v hi)))

(defn- move-paddle [world dt]
  ;; TODO: same shape as Pong's paddle mover, horizontal this time.
  world)

(defn- brick-hit-index [bricks x y]
  ;; TODO: find the index of the first alive brick whose rect overlaps the ball.
  nil)

(defn- move-ball [{:keys [paddle-x ball bricks] :as world} _dt]
  (let [{:keys [x y dx dy]} ball
        x' (+ x dx)
        y' (+ y dy)
        [x2 dx2] (cond
                   (< x' ball-radius) [ball-radius (Math/abs dx)]
                   (> x' (- width ball-radius)) [(- width ball-radius) (- (Math/abs dx))]
                   :else [x' dx])
        [y2 dy2] (if (< y' ball-radius) [ball-radius (Math/abs dy)] [y' dy])
        hit-i    (brick-hit-index bricks x2 y2)]
    (cond
      (> y2 height)
      (assoc world :status :lost)

      (some? hit-i)
      (let [bricks' (assoc bricks hit-i false)]
        (-> world
            (assoc :bricks bricks')
            (assoc :ball {:x x2 :y y2 :dx dx2 :dy (- dy2)})
            (cond-> (not (some true? bricks')) (assoc :status :won))))

      ;; paddle: hit location changes the outgoing angle
      (and (pos? dy2)
           (>= (+ y2 ball-radius) paddle-y)
           (<= y2 (+ paddle-y paddle-height))
           (>= (+ x2 ball-radius) paddle-x)
           (<= (- x2 ball-radius) (+ paddle-x paddle-width)))
      (let [;; TODO: 0 at the paddle's left edge, 1 at its right edge, map that
            ;; linearly to an outgoing dx between -ball-speed and +ball-speed.
            hit-ratio 0.0
            new-dx    0.0]
        (assoc world :ball {:x x2 :y (- paddle-y ball-radius) :dx new-dx :dy (- (Math/abs dy2))}))

      :else
      (assoc world :ball {:x x2 :y y2 :dx dx2 :dy dy2}))))

(defn- tick [world dt]
  (if (= :playing (:status world))
    (-> world (move-paddle dt) (move-ball dt))
    world))

(defn- draw [{:keys [paddle-x ball bricks status]}]
  (dotimes [i (* rows cols)]
    (when (nth bricks i)
      (let [{:keys [x y w h color]} (brick-rect i)]
        (shapes/draw-rectangle! x y w h color))))
  (shapes/draw-rectangle! (int paddle-x) paddle-y paddle-width paddle-height colors/raywhite)
  (shapes/draw-circle! (int (:x ball)) (int (:y ball)) ball-radius colors/raywhite)
  (case status
    :won  (text/draw-text! "YOU WIN" 240 200 40 colors/green)
    :lost (text/draw-text! "GAME OVER" 220 200 40 colors/red)
    nil))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Breakout"
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
clojure -M:run -m phase-2.breakout-starter
```

Use A/D or Left/Right arrow keys to move the paddle and bounce the ball into the bricks.

## Hints

### `move-paddle`

- Same structure as Pong's paddle movement, but horizontal.
- Read keyboard input for A/Left (move left) and D/Right (move right) from `enums/keyboard-key`.
- Scale movement by `paddle-speed * dt`.
- Clamp the paddle's `x` position to stay within the window: `(clamp new-x 0 (- width paddle-width))`.

### `brick-hit-index`

- Filter the `:bricks` vector for indices where `(nth bricks i)` is `true` (alive bricks only).
- For each candidate brick, get its rectangle via `(brick-rect i)`.
- Test if the ball's bounding box (a square from `(- x ball-radius)` to `(+ x ball-radius)` horizontally and vertically) overlaps the brick's rectangle.
- Return the index of the first brick that overlaps, or `nil` if none do.
- Use `map` to compute each brick rect once, then filter on the rectangle to avoid redundant calls.

### Paddle-bounce hit-ratio

- Calculate where on the paddle the ball hit: `(/ (- ball-x paddle-x) paddle-width)`.
- Clamp this ratio to `[0.0, 1.0]`.
- Map the ratio linearly to `dx` between `-ball-speed` and `+ball-speed`: when ratio is 0 (left edge), `dx = -ball-speed`; when ratio is 1 (right edge), `dx = +ball-speed`.
- The formula: `(* ball-speed (- (* 2 hit-ratio) 1.0))`.

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/breakout.clj` to compare your implementation.

### A Note on Ball Speed

Like Pong, the ball's movement is **not** scaled by `dt` — it moves by raw `dx`/`dy` pixels per frame. The `ball-speed` constant (4.0) must be small enough that the ball doesn't "tunnel" through bricks or the paddle on a single tick.

The brick collision window is approximately `brick-w + 2*ball-radius = 72 pixels`. The paddle collision window is approximately `paddle-width + 2*ball-radius = 116 pixels`. A ball moving 4.7 pixels per tick (the magnitude of the initial velocity, `√(2.4² + 4.0²)`) leaves plenty of margin to be detected on collision. If `ball-speed` were much larger, the ball would pass through objects without triggering the collision check — exactly the bug that broke Pong in early testing.

Before shipping a solution, always run it in simulation to verify that:
1. Multiple bricks actually get destroyed (not just one, which could happen by luck).
2. The ball can eventually fall past the paddle and trigger the `:lost` state.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **Jolt + raylib-jlt:** [`breakout.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/breakout.clj) — paddle (mouse-controlled) + ball + brick grid, clear to win.

---

**Previous:** [Lesson 1: Pong](01-pong.md) · **Next:** [Lesson 3: Snake](03-snake.md)
