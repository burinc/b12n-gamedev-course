# Phase 1, Lesson 3 — Following Eyes (Mouse Input)

This is your first graded exercise — no worked example walking you through every line, just a starter stub with TODOs and hints. Fill in the gaps yourself. You'll practice reading mouse input and using polar-to-Cartesian coordinate math to make a pair of eyes whose pupils track the player's cursor.

## The Starter Stub

Here's `exercises/phase_1/following_eyes_starter.clj`, with three TODOs to fill in:

```clojure
(ns phase-1.following-eyes-starter
  "Phase 1, Lesson 3 — a pair of eyes whose pupils track the mouse.
   Fill in the TODOs. Compare against following_eyes.clj (the solution,
   same directory) once yours runs."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.mouse :as mouse]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.colors :as colors]))

(def ^:private width 640)
(def ^:private height 480)
(def ^:private eye-radius 60)
(def ^:private pupil-radius 20)
(def ^:private pupil-range 25) ;; how far the pupil can wander from center

(def ^:private eye-centers
  [{:x 220 :y 240} {:x 420 :y 240}])

(defn- init []
  {:mouse {:x 0 :y 0}})

(defn- tick [_world dt]
  ;; TODO: read the current mouse position (mouse/get-mouse-position
  ;; returns a {:x .. :y ..} map already) and return it as the new world.
  )

(defn- pupil-offset [eye-center mouse]
  ;; TODO: compute {:x .. :y ..}, the pupil's offset from eye-center,
  ;; pointed toward `mouse` but never further than pupil-range away.
  ;; Hints: Math/atan2 for the angle toward the mouse, Math/cos/Math/sin
  ;; to turn an angle + distance back into an x/y offset, and Lesson 1's
  ;; clamp pattern (or just `min`) to cap the distance.
  )

(defn- draw [{:keys [mouse]}]
  (doseq [center eye-centers]
    (shapes/draw-circle-v! center eye-radius colors/white)
    ;; TODO: outline the eye — draw-circle-lines-v! wants the same
    ;; args as draw-circle-v!, just no fill.
    (let [offset (pupil-offset center mouse)
          pupil  {:x (+ (:x center) (:x offset))
                  :y (+ (:y center) (:y offset))}]
      (shapes/draw-circle-v! pupil pupil-radius colors/black))))

(defn -main [& _args]
  (game-loop/run-game!
   {:title  "Following Eyes"
    :width  width
    :height height
    :init   init
    :tick   tick
    :draw   draw}))
```

## Run Your Version

Once you've filled in the three TODOs, compile and run your code:

```bash
cd /path/to/b12n-gamedev-course
clojure -M:run -m phase-1.following-eyes-starter
```

Move your mouse around the window. You should see two eyes with pupils that track your cursor. The pupils should stay within the eyes' outline and never travel more than 25 pixels from the center of each eye.

## Stuck?

Here are the hints from the starter code, expanded:

**TODO #1 in `tick`:** You need to capture the current mouse position every frame and return it as part of the world. `mouse/get-mouse-position` returns a map like `{:x 320 :y 240}`. Return a world map with this mouse data so `draw` can use it.

**TODO #2 in `pupil-offset`:** This is the math part. The pupil needs to:
1. Point toward the mouse from the eye's center (use `Math/atan2` with the dy and dx between the two points).
2. Place itself at most `pupil-range` pixels away (use `min` to cap the distance).
3. Convert that angle and distance back into an offset `{:x .. :y ..}` (use `Math/cos` and `Math/sin`).

**TODO #3 in `draw`:** The two TODOs in `draw` combine to: draw a white filled eye, then outline it with a black circle. Use `shapes/draw-circle-lines-v!` — it takes the same arguments as `shapes/draw-circle-v!` (center, radius, color) but draws only the outline, not the fill. This is the key visual fix that makes the eyes look right.

## Compare Against the Solution

When your version runs, compare it against `exercises/phase_1/following_eyes.clj` (the solution, in the same directory). Both should look identical visually — two white eyes with black outlines, pupils tracking your cursor.

**Important visual detail:** if your eyes appear solid black instead of white with an outline, you're missing the `draw-circle-lines-v!` outline step. A naive approach (drawing a filled black circle on top of a filled white circle) just repaints the same circle black — you see a solid black eye. The solution uses `draw-circle-lines-v!` to draw only the outline, leaving the white fill inside visible. That's the difference between a correct and broken version.
