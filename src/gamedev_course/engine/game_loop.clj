(ns gamedev-course.engine.game-loop
  "A big-bang-style teaching wrapper over the vendored raylib layer:
   describe a game as pure functions over an immutable world-state
   value, and let run-game! own the actual loop, window lifecycle,
   frame timing, and headless-smoke-test hooks. Modeled on Racket's
   2htdp/universe big-bang (world state + tick/key/draw/stop handlers)
   — see docs/guide/phase-1-foundations/02-the-game-loop.md for the
   lesson that teaches this to students."
  (:require [gamedev-course.engine.raylib.core.window :as window]
            [gamedev-course.engine.raylib.core.drawing :as drawing]
            [gamedev-course.engine.raylib.core.timing :as timing]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.colors :as colors]
            [gamedev-course.engine.smoke :as smoke]))

(defn- drain-key-events
  "Every keycode raylib queued as pressed this frame, in press order.
   GetKeyPressed drains raylib's own internal queue one event per call
   and returns 0 once it's empty."
  []
  (loop [acc []]
    (let [k (keyboard/get-key-pressed)]
      (if (zero? k)
        acc
        (recur (conj acc k))))))

(defn run-game!
  "Runs a game loop described entirely as pure functions over an
   immutable world-state value. `opts`:

     :title      window title string                          (required)
     :width      window width in px                            (required)
     :height     window height in px                           (required)
     :init       (fn []) -> world                               (required)
     :tick       (fn [world dt-seconds]) -> world                (required)
     :draw       (fn [world]) -> nil, called between
                 begin-drawing!/end-drawing!                     (required)
     :on-key     (fn [world keycode]) -> world, called once per
                 key-press event queued this frame, in press
                 order via reduce. Default: (fn [w _k] w).
     :stop?      (fn [world]) -> boolean, checked before every
                 frame. Default: (constantly false).
     :background a raylib color map. Default: colors/raywhite.
     :fps        target frames per second. Default: 60.

   Returns the final world value when the loop stops — the window was
   closed, stop? returned true, or a RAYLIB_APP_AUTO_QUIT_MS deadline
   was reached (see gamedev-course.engine.smoke)."
  [{:keys [title width height init tick draw on-key stop? background fps]
    :or   {on-key     (fn [world _keycode] world)
           stop?      (constantly false)
           background colors/raywhite
           fps        60}}]
  {:pre [(some? title) (some? width) (some? height)
         (fn? init) (fn? tick) (fn? draw)]}
  (window/init-window! width height title)
  (timing/set-target-fps! fps)
  (let [deadline (smoke/auto-quit-deadline)]
    (try
      (loop [world (init) frame 0]
        (if (or (not (smoke/keep-running? deadline)) (stop? world))
          world
          (let [dt          (timing/get-frame-time)
                world-input (reduce on-key world (drain-key-events))
                world-next  (tick world-input dt)]
            (drawing/begin-drawing!)
            (drawing/clear-background! background)
            (draw world-next)
            (drawing/end-drawing!)
            (smoke/maybe-screenshot! frame 30)
            (recur world-next (inc frame)))))
      (finally
        (window/close-window!)))))
