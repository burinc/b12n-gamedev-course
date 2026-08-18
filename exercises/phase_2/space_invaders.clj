(ns phase-2.space-invaders
  "Phase 2, Lesson 4 — Space Invaders. Left/Right move, Space fires (one
   bullet in flight at a time, classic-style). The enemy formation
   marches as one unit and drops a row whenever it touches an edge."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def width 640)
(def height 480)
(def player-w 40) (def player-h 16)
(def player-y (- height 40))
(def player-speed 260.0)
(def bullet-w 4) (def bullet-h 12) (def bullet-speed 360.0)
(def enemy-w 30) (def enemy-h 20) (def enemy-gap 12)
(def enemy-rows 4) (def enemy-cols 8)
(def enemy-speed 40.0)
(def enemy-drop 20)

(defn- init-enemies []
  (vec (for [row (range enemy-rows) col (range enemy-cols)]
         {:x (+ 60 (* col (+ enemy-w enemy-gap)))
          :y (+ 40 (* row (+ enemy-h enemy-gap)))
          :alive? true})))

(defn init []
  {:player-x   (double (/ (- width player-w) 2))
   :bullet     nil ;; {:x :y} or nil when no bullet is in flight
   :enemies    (init-enemies)
   :enemy-dir  1
   :status     :playing})

(defn- clamp [v lo hi] (max lo (min v hi)))

(defn- move-player [world dt]
  (let [delta (* player-speed dt)]
    (cond
      (keyboard/is-key-down? (:left enums/keyboard-key))
      (update world :player-x #(clamp (- % delta) 0 (- width player-w)))
      (keyboard/is-key-down? (:right enums/keyboard-key))
      (update world :player-x #(clamp (+ % delta) 0 (- width player-w)))
      :else world)))

(defn- maybe-fire [{:keys [bullet player-x] :as world}]
  (if (and (nil? bullet) (keyboard/is-key-pressed? (:space enums/keyboard-key)))
    (assoc world :bullet {:x (+ player-x (/ player-w 2)) :y player-y})
    world))

(defn- move-bullet [{:keys [bullet] :as world} dt]
  (if (nil? bullet)
    world
    (let [y' (- (:y bullet) (* bullet-speed dt))]
      (if (neg? y')
        (assoc world :bullet nil)
        (assoc world :bullet (assoc bullet :y y'))))))

(defn- bullet-hits? [bullet enemy]
  (and bullet
       (>= (+ (:x bullet) bullet-w) (:x enemy)) (<= (:x bullet) (+ (:x enemy) enemy-w))
       (>= (+ (:y bullet) bullet-h) (:y enemy)) (<= (:y bullet) (+ (:y enemy) enemy-h))))

(defn- resolve-hit [{:keys [bullet enemies] :as world}]
  (let [hit-idx (some (fn [i] (when (and (:alive? (nth enemies i)) (bullet-hits? bullet (nth enemies i))) i))
                      (range (count enemies)))]
    (if hit-idx
      (-> world
          (update :enemies assoc-in [hit-idx :alive?] false)
          (assoc :bullet nil))
      world)))

(defn- move-enemies [{:keys [enemies enemy-dir] :as world} dt]
  (let [alive        (filter :alive? enemies)
        min-x        (when (seq alive) (apply min (map :x alive)))
        max-x        (when (seq alive) (apply max (map (fn [e] (+ (:x e) enemy-w)) alive)))
        hit-edge?    (and min-x (or (<= min-x 10) (>= max-x (- width 10))))
        dx           (* enemy-speed enemy-dir dt)]
    (if hit-edge?
      (-> world
          (update :enemies #(mapv (fn [e] (update e :y + enemy-drop)) %))
          (update :enemy-dir -))
      (update world :enemies #(mapv (fn [e] (update e :x + dx)) %)))))

(defn- tick [world dt]
  (if (not= :playing (:status world))
    world
    (let [world (-> world (move-player dt) maybe-fire (move-bullet dt) resolve-hit (move-enemies dt))
          enemies (:enemies world)]
      (cond
        (not-any? :alive? enemies) (assoc world :status :won)
        (some #(and (:alive? %) (> (+ (:y %) enemy-h) player-y)) enemies) (assoc world :status :lost)
        :else world))))

(defn- draw [{:keys [player-x bullet enemies status]}]
  (shapes/draw-rectangle! (int player-x) player-y player-w player-h colors/green)
  (when bullet (shapes/draw-rectangle! (int (:x bullet)) (int (:y bullet)) bullet-w bullet-h colors/raywhite))
  (doseq [e enemies :when (:alive? e)]
    (shapes/draw-rectangle! (int (:x e)) (int (:y e)) enemy-w enemy-h colors/red))
  (case status
    :won  (text/draw-text! "YOU WIN" 240 200 40 colors/green)
    :lost (text/draw-text! "GAME OVER" 220 200 40 colors/red)
    nil))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Space Invaders"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/black}))
