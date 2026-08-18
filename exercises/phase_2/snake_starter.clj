(ns phase-2.snake-starter
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
   Food that spawns inside the snake is both unreachable-without-dying and,
   for most spawns, simply invisible under the snake — never place it
   there.

   Computes the actual free-cell set rather than rejection-sampling
   rand-cell against `taken` — rejection sampling is simpler but can loop
   forever if the board is ever completely full. That's unreachable
   during normal play (it means the snake filled the whole board), but a
   defensive fallback beats a silent hang if it ever somehow happens."
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
  ;; TODO: Implement direction reading using keyboard input.
  ;; Similar pattern to solution: check if any arrow key is pressed,
  ;; store the wanted direction, and update :pending-dir only if
  ;; the wanted direction is not the opposite of current direction.
  world)

(defn- step-snake [{:keys [snake direction food] :as world}]
  ;; TODO: Implement snake movement and collision detection.
  ;; Compute the new head position by moving from current head in the direction.
  ;; Decide whether to grow (if new head equals food) or slide (remove tail).
  ;; Detect self-collision: check if new head collides with rest of body.
  ;; Return :lost status if collision, otherwise update snake and food —
  ;; and when you respawn food after eating, use `rand-free-cell` (not
  ;; `rand-cell`) on the NEW body, or food can spawn inside the snake
  ;; itself, which is unreachable without dying.
  world)

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
