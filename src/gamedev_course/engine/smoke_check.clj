(ns gamedev-course.engine.smoke-check
  "Throwaway verification program for Task B.T3 — opens a window, draws
   one frame, and exits via RAYLIB_APP_AUTO_QUIT_MS. Not part of any
   lesson; proves the vendored FFI layer + smoke helpers work end to end
   before any lesson content depends on them. Safe to delete once B.T4's
   game-loop tests exist and pass — kept until then as the simplest
   possible reproduction if something breaks."
  (:require [gamedev-course.engine.raylib.core.window :as window]
            [gamedev-course.engine.raylib.core.drawing :as drawing]
            [gamedev-course.engine.raylib.core.timing :as timing]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]
            [gamedev-course.engine.smoke :as smoke]))

(defn -main [& _args]
  (window/init-window! 320 240 "smoke check")
  (timing/set-target-fps! 60)
  (let [deadline (smoke/auto-quit-deadline)]
    (loop [frame 0]
      (when (smoke/keep-running? deadline)
        (drawing/begin-drawing!)
        (drawing/clear-background! colors/raywhite)
        (text/draw-text! "smoke check" 90 110 20 colors/darkgray)
        (drawing/end-drawing!)
        (smoke/maybe-screenshot! frame 1)
        (recur (inc frame)))))
  (window/close-window!))
