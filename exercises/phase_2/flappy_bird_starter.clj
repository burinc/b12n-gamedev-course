(ns phase-2.flappy-bird-starter
  "Phase 2, Lesson 6 — Flappy Bird. Space flaps. Space also starts the
   game from the title screen and restarts it after game over — one key,
   three states."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def width 640)
(def height 480)
(def bird-x 120)
(def bird-radius 12)
(def gravity 900.0)
(def flap-vy -320.0)
(def pipe-w 60)
(def pipe-gap 140)
(def pipe-speed 180.0)
(def pipe-spacing 260) ;; horizontal distance between pipe spawns

(defn- new-pipe [x]
  {:x x :gap-y (+ 80 (rand-int (- height 160 pipe-gap))) :scored? false})

(defn init []
  {:status  :title ;; :title, :playing, :over
   :bird-y  (double (/ height 2))
   :bird-vy 0.0
   :pipes   [(new-pipe width) (new-pipe (+ width pipe-spacing))]
   :score   0})

(defn- flap-pressed? [] (keyboard/is-key-pressed? (:space enums/keyboard-key)))

(defn- physics [world dt]
  ;; TODO: apply gravity to `:bird-vy` each frame, override it with
  ;; `flap-vy` on a flap press, then integrate `:bird-y`.
  world)

(defn- move-pipes [{:keys [pipes score] :as world} dt]
  (let [moved     (mapv (fn [p] (update p :x - (* pipe-speed dt))) pipes)
        passed?   (fn [p] (and (not (:scored? p)) (< (+ (:x p) pipe-w) (- bird-x bird-radius))))
        score'    (+ score (count (filter passed? moved)))
        moved     (mapv (fn [p] (if (passed? p) (assoc p :scored? true) p)) moved)
        kept      (vec (remove (fn [p] (< (+ (:x p) pipe-w) 0)) moved))
        rightmost (apply max (map :x kept))
        kept      (if (< rightmost (- width pipe-spacing))
                    ;; TODO: spawn a new pipe once the rightmost one has
                    ;; scrolled far enough left (hint: pipe-spacing).
                    kept
                    kept)]
    (assoc world :pipes kept :score score')))

(defn- collides? [bird-y pipes]
  ;; TODO: bird-vs-floor/ceiling, plus bird-vs-each-pipe (the pipe gap
  ;; is centered on `gap-y` with total height `pipe-gap`).
  false)

(defn- tick [world dt]
  (case (:status world)
    :title (if (flap-pressed?) (assoc world :status :playing) world)
    :over  (if (flap-pressed?) (init) world)
    :playing
    (let [world (-> world (physics dt) (move-pipes dt))]
      (if (collides? (:bird-y world) (:pipes world))
        (assoc world :status :over)
        world))))

(defn- draw [{:keys [status bird-y pipes score]}]
  (doseq [{:keys [x gap-y]} pipes]
    (shapes/draw-rectangle! (int x) 0 pipe-w (int (- gap-y (/ pipe-gap 2))) colors/green)
    (shapes/draw-rectangle! (int x) (int (+ gap-y (/ pipe-gap 2))) pipe-w
                            (- height (int (+ gap-y (/ pipe-gap 2)))) colors/green))
  (shapes/draw-circle! bird-x (int bird-y) bird-radius colors/yellow)
  (text/draw-text! (str score) (- (/ width 2) 10) 20 30 colors/raywhite)
  (case status
    :title (text/draw-text! "SPACE to start" 210 240 20 colors/raywhite)
    :over  (text/draw-text! "GAME OVER — SPACE to retry" 140 240 20 colors/red)
    nil))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Flappy Bird"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/skyblue}))
