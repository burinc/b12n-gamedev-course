(ns phase-2.tetris
  "Phase 2, Lesson 5 — Tetris (simplified: 90-degree rotation, no wall
   kicks, no hold/preview). Left/Right move, Up rotates, Down soft-drops."
  (:require [net.b12n.game-dev.engine.game-loop :as game-loop]
            [net.b12n.game-dev.engine.raylib.core.keyboard :as keyboard]
            [net.b12n.game-dev.engine.raylib.enums :as enums]
            [net.b12n.game-dev.engine.raylib.shapes.basic :as shapes]
            [net.b12n.game-dev.engine.raylib.text.drawing :as text]
            [net.b12n.game-dev.engine.raylib.colors :as colors]))

(def cols 10)
(def rows 20)
(def cell 24)
(def width (* cols cell))
(def height (* rows cell))
(def fall-interval 0.5)

;; Every shape is a set of [row col] offsets inside a 4x4 box (col/row 0..3).
(def shapes
  {:I [[1 0] [1 1] [1 2] [1 3]]
   :O [[0 1] [0 2] [1 1] [1 2]]
   :T [[0 1] [1 0] [1 1] [1 2]]
   :S [[0 1] [0 2] [1 0] [1 1]]
   :Z [[0 0] [0 1] [1 1] [1 2]]
   :J [[0 0] [1 0] [1 1] [1 2]]
   :L [[0 2] [1 0] [1 1] [1 2]]})

(def piece-color
  {:I colors/skyblue :O colors/yellow :T colors/purple
   :S colors/green :Z colors/red :J colors/blue :L colors/orange})

(defn- rotate-cw [offsets]
  ;; (r,c) -> (c, 3-r) inside a 4x4 box — a standard box rotation.
  (mapv (fn [[r c]] [c (- 3 r)]) offsets))

(defn- rand-kind [] (rand-nth (keys shapes)))

(defn- spawn [kind]
  {:kind kind :offsets (shapes kind) :row 0 :col 3})

(defn init []
  {:board       (vec (repeat rows (vec (repeat cols nil))))
   :current     (spawn (rand-kind))
   :fall-timer  0.0
   :status      :playing})

(defn- cell-positions [{:keys [offsets row col]}]
  (map (fn [[r c]] [(+ row r) (+ col c)]) offsets))

(defn- fits? [board positions]
  (every? (fn [[r c]]
            (and (< -1 c cols) (< r rows)
                 (or (neg? r) (nil? (get-in board [r c])))))
          positions))

(defn- move-piece [{:keys [board current] :as world} dcol]
  (let [moved (update current :col + dcol)]
    (if (fits? board (cell-positions moved))
      (assoc world :current moved)
      world)))

(defn- rotate-piece [{:keys [board current] :as world}]
  ;; The O piece is a special case: it's a perfect 2x2 square, so real
  ;; Tetris (and SRS) treats it as having a single rotation state — it
  ;; never actually changes shape. Our raw box rotation (r,c) -> (c,3-r)
  ;; pivots around the CENTER of the 4x4 box, but O's 2x2 isn't centered
  ;; there (it sits at rows 0-1, cols 1-2), so rotating it would visibly
  ;; translate it by one cell each press instead of leaving it in place.
  (if (= :O (:kind current))
    world
    (let [rotated (assoc current :offsets (rotate-cw (:offsets current)))]
      (if (fits? board (cell-positions rotated))
        (assoc world :current rotated)
        world))))

(defn- clear-lines [board]
  (let [kept (vec (remove (fn [row] (every? some? row)) board))
        cleared (- rows (count kept))]
    {:board (into (vec (repeat cleared (vec (repeat cols nil)))) kept)
     :cleared cleared}))

(defn- lock-piece [{:keys [board current] :as world}]
  (let [locked (reduce (fn [b [r c]]
                         (if (neg? r) b (assoc-in b [r c] (piece-color (:kind current)))))
                       board (cell-positions current))
        {:keys [board cleared]} (clear-lines locked)
        next-piece (spawn (rand-kind))]
    (-> world
        (assoc :board board)
        (update :score (fnil + 0) (* cleared 100))
        (assoc :current next-piece)
        (cond-> (not (fits? board (cell-positions next-piece))) (assoc :status :lost)))))

(defn- try-fall [{:keys [board current] :as world}]
  (let [dropped (update current :row inc)]
    (if (fits? board (cell-positions dropped))
      (assoc world :current dropped)
      (lock-piece world))))

(defn- handle-input [world]
  (cond-> world
    (keyboard/is-key-pressed? (:left enums/keyboard-key))  (move-piece -1)
    (keyboard/is-key-pressed? (:right enums/keyboard-key)) (move-piece 1)
    (keyboard/is-key-pressed? (:up enums/keyboard-key))    rotate-piece))

(defn- tick [world dt]
  (if (not= :playing (:status world))
    world
    (let [world (handle-input world)
          fast? (keyboard/is-key-down? (:down enums/keyboard-key))
          timer (+ (:fall-timer world) (if fast? (* dt 8) dt))]
      (if (>= timer fall-interval)
        (-> world (assoc :fall-timer (- timer fall-interval)) try-fall)
        (assoc world :fall-timer timer)))))

(defn- draw-cell! [row col color]
  (shapes/draw-rectangle! (* col cell) (* row cell) (dec cell) (dec cell) color))

(defn- draw [{:keys [board current status score]}]
  (dotimes [r rows]
    (dotimes [c cols]
      (when-let [color (get-in board [r c])] (draw-cell! r c color))))
  (doseq [[r c] (cell-positions current) :when (not (neg? r))]
    (draw-cell! r c (piece-color (:kind current))))
  (text/draw-text! (str "score " (or score 0)) 10 (- height 24) 20 colors/raywhite)
  (when (= :lost status) (text/draw-text! "GAME OVER" (- (/ width 2) 90) (/ height 2) 30 colors/red)))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Tetris"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/black}))
