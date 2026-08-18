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
