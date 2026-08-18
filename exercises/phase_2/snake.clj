(ns phase-2.snake
  "Phase 2, Lesson 3 — Snake. Arrow keys steer; can't reverse directly
   into yourself. Moves on a fixed grid tick, not every frame — the
   first game in this ladder that isn't continuous motion."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def cell-size 20)
(def cols 32)
(def rows 24)
(def width (* cols cell-size))
(def height (* rows cell-size))
(def move-interval 0.12) ;; seconds between grid steps

(defn- rand-cell [] {:x (rand-int cols) :y (rand-int rows)})

(defn- rand-free-cell
  "A random cell not occupied by any of `occupied` (the snake's own body).
   Food that spawns inside the snake is both unreachable-without-dying (the
   just-eaten segment is still counted as 'self' on the eating tick) and,
   for most spawns, simply invisible under the snake — never place it
   there.

   Computes the actual free-cell set rather than rejection-sampling
   rand-cell against `taken` — rejection sampling is simpler but can loop
   forever if the board is ever completely full (every one of the
   cols*rows cells occupied). That's an unreachable state during normal
   play (it means the snake has filled the entire board — you'd have won
   long before then), but a defensive fallback beats a silent hang if it
   ever somehow happens: fall back to any cell, since there is, by
   definition, no free one left to choose from."
  [occupied]
  (let [taken (set occupied)
        free  (remove taken (for [x (range cols) y (range rows)] {:x x :y y}))]
    (if (seq free)
      (rand-nth free)
      (rand-cell))))

(defn init []
  (let [snake [{:x 16 :y 12} {:x 15 :y 12} {:x 14 :y 12}]]
    {:snake        snake
     :direction    [1 0]
     :pending-dir  [1 0]
     :food         (rand-free-cell snake)
     :move-timer   0.0
     :status       :playing}))

(def ^:private opposite {[1 0] [-1 0] [-1 0] [1 0] [0 1] [0 -1] [0 -1] [0 1]})

(defn- read-direction [{:keys [direction] :as world}]
  (let [wanted (cond
                 (keyboard/is-key-pressed? (:right enums/keyboard-key)) [1 0]
                 (keyboard/is-key-pressed? (:left enums/keyboard-key))  [-1 0]
                 (keyboard/is-key-pressed? (:up enums/keyboard-key))    [0 -1]
                 (keyboard/is-key-pressed? (:down enums/keyboard-key))  [0 1]
                 :else nil)]
    (if (and wanted (not= wanted (opposite direction)))
      (assoc world :pending-dir wanted)
      world)))

(defn- step-snake [{:keys [snake direction food] :as world}]
  (let [head       (first snake)
        [dx dy]    direction
        new-head   {:x (mod (+ (:x head) dx) cols) :y (mod (+ (:y head) dy) rows)}
        ate?       (= new-head food)
        body       (into [new-head] (if ate? snake (butlast snake)))
        hit-self?  (some #(= new-head %) (rest body))]
    (cond
      hit-self? (assoc world :snake body :status :lost)
      :else (cond-> (assoc world :snake body)
              ate? (assoc :food (rand-free-cell body))))))

(defn- tick [world dt]
  (if (not= :playing (:status world))
    world
    (let [world (read-direction world)
          timer (+ (:move-timer world) dt)]
      (if (>= timer move-interval)
        (-> world
            (assoc :direction (:pending-dir world))
            (assoc :move-timer (- timer move-interval))
            step-snake)
        (assoc world :move-timer timer)))))

(defn- draw-cell [{:keys [x y]} color]
  (shapes/draw-rectangle! (* x cell-size) (* y cell-size) (dec cell-size) (dec cell-size) color))

(defn- draw [{:keys [snake food status]}]
  (draw-cell food colors/red)
  (doseq [segment snake] (draw-cell segment colors/green))
  (when (= :lost status)
    (text/draw-text! "GAME OVER" 220 200 40 colors/raywhite)))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Snake"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/black}))
