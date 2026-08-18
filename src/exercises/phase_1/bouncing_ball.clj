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
