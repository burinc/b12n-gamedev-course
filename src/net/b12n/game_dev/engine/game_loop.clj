(ns net.b12n.game-dev.engine.game-loop
  "A big-bang-style teaching wrapper over the vendored raylib layer:
   describe a game as pure functions over an immutable world-state
   value, and let run-game! own the actual loop, window lifecycle,
   frame timing, and headless-smoke-test hooks. Modeled on Racket's
   2htdp/universe big-bang (world state + tick/key/draw/stop handlers)
   — see docs/guide/phase-1-foundations/02-the-game-loop.md for the
   lesson that teaches this to students."
  (:require [net.b12n.game-dev.engine.raylib.core.window :as window]
            [net.b12n.game-dev.engine.raylib.core.drawing :as drawing]
            [net.b12n.game-dev.engine.raylib.core.timing :as timing]
            [net.b12n.game-dev.engine.raylib.core.keyboard :as keyboard]
            [net.b12n.game-dev.engine.raylib.colors :as colors]
            [net.b12n.game-dev.engine.smoke :as smoke]))

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

(defn step-fixed
  "The classic fixed-timestep accumulator: given the world, a fixed dt,
   how much unspent simulation time carried over from last frame
   (accumulator), how much real time elapsed this frame, and a cap on
   steps per frame, calls tick once per whole fixed-dt chunk the
   accumulated time can afford, always with the SAME dt value every
   call. Returns [world' accumulator'] — the accumulator is NOT
   clamped when the cap trips, so any leftover backlog carries into
   next frame's call in full, to be worked off over subsequent frames.

   `max-steps` guards against a 'spiral of death' AFTER A TRANSIENT
   STALL (each step takes real time to compute, so catching up too
   much in one frame can make the next frame even slower) — but only
   when `max-steps * fixed-dt` comfortably exceeds your target frame
   time (e.g. `:fps 60` -> ~16.7ms; the default `max-steps` 5 covers
   fixed-dt down to ~3.3ms). Choose a fixed-dt/max-steps combination
   whose product stays above your target frame time under NORMAL
   (non-stalled) play, or the backlog this fn defers will grow every
   single frame instead of only after a stall — this fn has no way to
   tell those two cases apart from inside one call."
  [tick world accumulator elapsed fixed-dt max-steps]
  (loop [w world acc (+ accumulator elapsed) steps 0]
    (if (and (>= acc fixed-dt) (< steps max-steps))
      (recur (tick w fixed-dt) (- acc fixed-dt) (inc steps))
      [w acc])))

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
     :fixed-dt   optional fixed timestep in seconds. When supplied,
                 tick is called zero or more times per frame via
                 step-fixed, always with this same dt value, instead
                 of once per frame with the real frame dt. Default:
                 nil (variable-dt mode, tick called once per frame).
     :max-steps-per-frame
                 cap on how many fixed-dt steps step-fixed will run
                 in a single frame, guarding against the 'spiral of
                 death' after a long stall. Only relevant when
                 :fixed-dt is supplied. Default: 5.

   Returns the final world value when the loop stops — the window was
   closed, stop? returned true, or a RAYLIB_APP_AUTO_QUIT_MS deadline
   was reached (see net.b12n.game-dev.engine.smoke)."
  [{:keys [title width height init tick draw on-key stop? background fps
           fixed-dt max-steps-per-frame]
    :or   {on-key             (fn [world _keycode] world)
           stop?              (constantly false)
           background         colors/raywhite
           fps                60
           max-steps-per-frame 5}}]
  {:pre [(some? title) (some? width) (some? height)
         (fn? init) (fn? tick) (fn? draw)]}
  (window/init-window! width height title)
  (timing/set-target-fps! fps)
  (let [deadline (smoke/auto-quit-deadline)]
    (try
      (when-not (window/is-window-ready?)
        (throw (ex-info (str "raylib's window failed to open for \"" title
                             "\" — is a display available? A headless box, "
                             "CI runner, or simply a locked/sleeping screen "
                             "will all cause this. Without this check, "
                             "run-game! used to silently return init's "
                             "world having run zero frames, which looked "
                             "exactly like a clean early stop.")
                        {:title title :width width :height height})))
      (loop [world (init) frame 0 accumulator 0.0]
        (if (or (not (smoke/keep-running? deadline)) (stop? world))
          world
          (let [dt          (timing/get-frame-time)
                world-input (reduce on-key world (drain-key-events))
                [world-next accumulator']
                (if fixed-dt
                  (step-fixed tick world-input accumulator dt fixed-dt max-steps-per-frame)
                  [(tick world-input dt) 0.0])]
            (drawing/begin-drawing!)
            (drawing/clear-background! background)
            (draw world-next)
            (drawing/end-drawing!)
            (smoke/maybe-screenshot! frame 30)
            (recur world-next (inc frame) accumulator'))))
      (finally
        (window/close-window!)))))
