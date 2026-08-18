# Phase 1, Lesson 2 — The Game Loop

> **Already know how game loops work and have written one before? Read [the worked bouncing-ball demo](#the-bouncing-ball) to see our teaching loop, then skip to the next exercise.**

## The World-State Model

A game — in this course and in the HtDP / Realm of Racket tradition — is built from one immutable value (the "world") plus three pure functions:

- **`init`** — called once at startup. Takes nothing, returns the initial world value.
- **`tick`** — called every frame. Takes the world and the time since the last frame (`dt`, in seconds), returns a new world with everything moved/updated for that frame.
- **`draw`** — called every frame (after `tick`). Takes the world, draws it to the screen, returns nothing.

This model is identical to Racket's `2htdp/universe` `big-bang` — which takes `init`, `on-tick`, and `to-draw` handlers and manages the loop for you. HtDP ([*How to Design Programs*](https://htdp.org)) and *[Realm of Racket](http://realmofracket.org)* use exactly this pattern to teach an entire language through game programming, and this course borrowed it directly because it works.

Notice what's NOT in that list: no mutable objects, no side effects inside the functions. `init` produces a value, `tick` transforms it, `draw` reads it. The loop itself — the window, the timing, the repeated calling — is owned by `run-game!`.

## `run-game!`'s Contract

Here's the full signature and docstring of our teaching loop:

```
run-game! opts

Runs a game loop described entirely as pure functions over an
immutable world-state value. `opts`:

  :title      window title string                          (required)
  :width      window width in px                            (required)
  :height     window height in px                           (required)
  :init       (fn []) -> world                               (required)
  :tick       (fn [world dt-seconds]) -> world                (required)
  :draw       (fn [world]) -> nil, called between
              begin-drawing!/end-drawing!                     (required)
  :on-key     (fn [world keycode]) -> world, called once per
              key-press event queued this frame, in press
              order via reduce. Default: (fn [w _k] w).
  :stop?      (fn [world]) -> boolean, checked before every
              frame. Default: (constantly false).
  :background a raylib color map. Default: colors/raywhite.
  :fps        target frames per second. Default: 60.

Returns the final world value when the loop stops — the window was
closed, stop? returned true, or a RAYLIB_APP_AUTO_QUIT_MS deadline
was reached.
```

The only requirements are `:title`, `:width`, `:height`, `:init`, `:tick`, and `:draw`. Everything else has a sensible default. You pass a map with these keys, and `run-game!` owns the loop from there.

## The Bouncing Ball {#the-bouncing-ball}

Here's a fully-worked example: a ball that bounces off all four edges of the window. Read it line by line, then we'll explain the key ideas.

```clojure
(ns exercises.phase-1.bouncing-ball
  "Phase 1, Lesson 2's worked example — a ball that bounces off all four
   window edges. Walked through line by line in
   docs/guide/phase-1-foundations/02-the-game-loop.md."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.colors :as colors]))

(def ^:private width 640)
(def ^:private height 480)
(def ^:private radius 20)
(def ^:private speed 220) ;; pixels per second

(defn- init []
  {:x radius :y (/ height 2) :dx speed :dy speed})

(defn- bounce [pos velocity low high]
  (cond
    (< pos low)  [low  (Math/abs (double velocity))]
    (> pos high) [high (- (Math/abs (double velocity)))]
    :else        [pos  velocity]))

(defn- tick [{:keys [x y dx dy]} dt]
  (let [[x' dx'] (bounce (+ x (* dx dt)) dx radius (- width radius))
        [y' dy'] (bounce (+ y (* dy dt)) dy radius (- height radius))]
    {:x x' :y y' :dx dx' :dy dy'}))

(defn- draw [{:keys [x y]}]
  (shapes/draw-circle! (int x) (int y) radius colors/maroon))

(defn -main [& _args]
  (game-loop/run-game!
   {:title  "Bouncing Ball"
    :width  width
    :height height
    :init   init
    :tick   tick
    :draw   draw}))
```

### The World

The world is a map with four keys:
- `:x`, `:y` — the ball's position (pixels from the top-left)
- `:dx`, `:dy` — the ball's velocity (pixels per second in each direction)

`init` starts the ball at the left edge (just touching the border), vertically centered, moving diagonally at 220 pixels/second in each direction.

### Why `tick` Returns a New Map

Remember the `move-right` example from Lesson 1? It used `update` to return a new map instead of mutating the old one:

```clojure
(defn move-right [position amount]
  (update position :x + amount))
```

`tick` does the same thing. Look at how it's structured:

1. Destructure the incoming world into `x`, `y`, `dx`, `dy`.
2. Calculate new position: `(+ x (* dx dt))`. That's `old-x + (velocity × time-since-last-frame)`.
3. Check if the new position hit a boundary, and clamp/bounce if needed.
4. Return a brand-new map: `{:x x' :y y' :dx dx' :dy dy'}`.

Nothing was mutated. The old world is untouched. This is how every game state update in this course works.

### What `dt` Buys You

`dt` is the time (in seconds) since the last frame. Without it, your game would run at a different speed on every machine:

- On a slow computer, 16 ms might pass between frames, so each frame the ball moves `220 × 0.016 = 3.5` pixels.
- On a fast computer, only 8 ms passes, so each frame the ball moves `220 × 0.008 = 1.76` pixels.

With `dt`, both machines move the ball `220 × dt` pixels per frame. Physics works out. The game runs at the same speed everywhere.

### Collision Detection and Bouncing

The `bounce` helper is doing two jobs at once:

```clojure
(defn- bounce [pos velocity low high]
  (cond
    (< pos low)  [low  (Math/abs (double velocity))]
    (> pos high) [high (- (Math/abs (double velocity)))]
    :else        [pos  velocity]))
```

- If the ball goes past `low` (the left/top edge), clamp it to `low` and make velocity positive (moving away from the edge).
- If the ball goes past `high` (the right/bottom edge), clamp it to `high` and make velocity negative (moving away from the edge).
- Otherwise, leave the position and velocity alone.

Notice: it returns a two-element vector `[new-position new-velocity]`, not a map. That's because we need both values back in `tick`, and a vector is a lightweight way to return multiple values.

In `tick`, we call `bounce` twice:

```clojure
[x' dx'] (bounce (+ x (* dx dt)) dx radius (- width radius))
[y' dy'] (bounce (+ y (* dy dt)) dy radius (- height radius))
```

For the X-axis: the ball's new position is the old position plus velocity×time; the boundaries are `radius` (left wall) to `(- width radius)` (right wall, accounting for the ball's radius so it doesn't stick out).

For the Y-axis: same idea, with `height` instead of `width`.

## Run It

Let's see the ball bounce:

```bash
cd /path/to/b12n-gamedev-course
RAYLIB_APP_AUTO_QUIT_MS=3000 clojure -M:run -m exercises.phase-1.bouncing-ball
```

A window opens showing a maroon ball bouncing diagonally. It bounces cleanly off all four edges without ever leaving the window. After 3 seconds, the window closes automatically (the `RAYLIB_APP_AUTO_QUIT_MS` environment variable sets a timeout for headless testing — remove it if you want to close manually).

## What's Next

The ball moved on its own — every frame, `tick` calculated a new position. But no one was steering it. In the next exercise, you'll build a paddle that the player *actually* controls: it won't move until you press a key, and the `run-game!` loop will call your `on-key` handler to let you change the world based on what the player pressed.
