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
