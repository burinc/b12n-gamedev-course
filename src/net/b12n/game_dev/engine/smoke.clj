(ns net.b12n.game-dev.engine.smoke
  "Headless verification helpers for windowed exercises — ported from
   b12n-raylib-jlt's proven RAYLIB_APP_AUTO_QUIT_MS / RAYLIB_APP_SHOT
   pattern (src/net/b12n/raylib_jlt/raylib.clj:548-574) so any exercise
   or lesson example in this course can be smoke-tested without a human
   at the keyboard."
  (:require [net.b12n.game-dev.engine.raylib.core.window :as window]
            [net.b12n.game-dev.engine.raylib.core.drawing :as drawing]))

(defn auto-quit-deadline
  "RAYLIB_APP_AUTO_QUIT_MS=<n> ends the loop after n ms, so a window
   example is smoke-testable with no person at the keyboard. Returns an
   absolute ms deadline, or nil if the env var is unset or not a
   positive integer."
  []
  (when-let [v (System/getenv "RAYLIB_APP_AUTO_QUIT_MS")]
    (try (let [ms (Integer/parseInt v)]
           (when (pos? ms) (+ (System/currentTimeMillis) ms)))
         (catch Exception _ nil))))

(defn keep-running?
  "True while the window is open and any auto-quit deadline is unmet."
  [deadline]
  (and (not (window/window-should-close?))
       (or (nil? deadline) (< (System/currentTimeMillis) deadline))))

(def ^:private shot-path (System/getenv "RAYLIB_APP_SHOT"))

(defn maybe-screenshot!
  "RAYLIB_APP_SHOT=/path dumps one PNG on frame `at` — headless visual
   proof a frame actually rendered. Call after end-drawing! so raylib's
   back buffer holds the finished frame. Verified live on macOS: raylib's
   TakeScreenshot ignores any directory component in `shot-path` — it
   always writes just the basename into the process's current working
   directory, even when `shot-path` is absolute. Set the process's cwd
   (e.g. run from the target directory) if you need the file somewhere
   specific; don't rely on the path argument's directory."
  [frame at]
  (when (and shot-path (= frame at))
    (drawing/take-screenshot! shot-path)
    (binding [*out* *err*] (println "[gamedev-course] SHOT" shot-path))))
