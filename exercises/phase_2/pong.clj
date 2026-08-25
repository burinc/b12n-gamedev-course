(ns phase-2.pong
  "Phase 2, Lesson 1 — Pong. Left paddle is you (W/S), right paddle is a
   simple tracking AI. First serve to 0 points wins nothing — this is
   about the loop, not a tournament."
  (:require [net.b12n.game-dev.engine.game-loop :as game-loop]
            [net.b12n.game-dev.engine.raylib.core.keyboard :as keyboard]
            [net.b12n.game-dev.engine.raylib.enums :as enums]
            [net.b12n.game-dev.engine.raylib.shapes.basic :as shapes]
            [net.b12n.game-dev.engine.raylib.text.drawing :as text]
            [net.b12n.game-dev.engine.raylib.colors :as colors]))

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
  (let [delta (* paddle-speed dt)]
    (cond
      (keyboard/is-key-down? (:w enums/keyboard-key))
      (update world :left-y #(clamp (- % delta) 0 (- height paddle-height)))
      (keyboard/is-key-down? (:s enums/keyboard-key))
      (update world :left-y #(clamp (+ % delta) 0 (- height paddle-height)))
      :else world)))

(defn- move-right-ai [world dt]
  (let [ball-y  (get-in world [:ball :y])
        target  (clamp (- ball-y (/ paddle-height 2)) 0 (- height paddle-height))
        current (:right-y world)
        step    (clamp (- target current) (- (* paddle-speed dt)) (* paddle-speed dt))]
    (update world :right-y + step)))

(defn- hits-paddle? [ball-x ball-y paddle-x paddle-y]
  (and (>= (+ ball-x ball-radius) paddle-x)
       (<= (- ball-x ball-radius) (+ paddle-x paddle-width))
       (>= (+ ball-y ball-radius) paddle-y)
       (<= (- ball-y ball-radius) (+ paddle-y paddle-height))))

(defn- move-ball [{:keys [ball left-y right-y left-score right-score] :as world} _dt]
  (let [{:keys [x y dx dy]} ball
        x'         (+ x dx)
        y'         (+ y dy)
        [y2 dy2]   (cond
                     (< y' ball-radius)              [ball-radius (Math/abs dy)]
                     (> y' (- height ball-radius))   [(- height ball-radius) (- (Math/abs dy))]
                     :else                            [y' dy])]
    (cond
      (< x' 0)
      (assoc world :ball (init-ball) :right-score (inc right-score))

      (> x' width)
      (assoc world :ball (init-ball) :left-score (inc left-score))

      (and (neg? dx) (hits-paddle? x' y2 left-paddle-x left-y))
      (assoc world :ball {:x (double (+ left-paddle-x paddle-width ball-radius))
                          :y y2 :dx (Math/abs dx) :dy dy2})

      (and (pos? dx) (hits-paddle? x' y2 right-paddle-x right-y))
      (assoc world :ball {:x (double (- right-paddle-x ball-radius))
                          :y y2 :dx (- (Math/abs dx)) :dy dy2})

      :else
      (assoc world :ball {:x x' :y y2 :dx dx :dy dy2}))))

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
