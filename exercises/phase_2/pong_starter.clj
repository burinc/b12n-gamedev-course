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
