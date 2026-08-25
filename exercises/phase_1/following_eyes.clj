(ns phase-1.following-eyes
  "Phase 1, Lesson 3 — a pair of eyes whose pupils track the mouse.
   The first exercise where the PLAYER, not physics, drives the world."
  (:require [net.b12n.game-dev.engine.game-loop :as game-loop]
            [net.b12n.game-dev.engine.raylib.core.mouse :as mouse]
            [net.b12n.game-dev.engine.raylib.shapes.basic :as shapes]
            [net.b12n.game-dev.engine.raylib.colors :as colors]))

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
  (let [{:keys [x y]} (mouse/get-mouse-position)]
    {:mouse {:x x :y y}}))

(defn- pupil-offset [eye-center mouse]
  (let [dx    (- (:x mouse) (:x eye-center))
        dy    (- (:y mouse) (:y eye-center))
        dist  (Math/sqrt (+ (* dx dx) (* dy dy)))
        angle (Math/atan2 dy dx)
        clamped-dist (min dist pupil-range)]
    {:x (* clamped-dist (Math/cos angle))
     :y (* clamped-dist (Math/sin angle))}))

(defn- draw [{:keys [mouse]}]
  (doseq [center eye-centers]
    (shapes/draw-circle-v! center eye-radius colors/white)
    (shapes/draw-circle-lines-v! center eye-radius colors/black) ;; outline
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
