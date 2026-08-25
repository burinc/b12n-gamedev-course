(ns net.b12n.game-dev.engine.game-loop-test
  (:require [clojure.test :refer [deftest is]]
            [net.b12n.game-dev.engine.game-loop :as game-loop]))

(deftest ^:windowed run-game-stops-on-stop-predicate
  (let [final (game-loop/run-game!
               {:title  "test"
                :width  100
                :height 100
                :init   (fn [] {:ticks 0})
                :tick   (fn [world _dt] (update world :ticks inc))
                :draw   (fn [_world] nil)
                :stop?  (fn [world] (>= (:ticks world) 5))})]
    (is (= 5 (:ticks final))
        "run-game! must return the world exactly when stop? first becomes true")))

(deftest ^:windowed run-game-folds-on-key-over-every-queued-key-event
  (let [final (game-loop/run-game!
               {:title  "test"
                :width  100
                :height 100
                :init   (fn [] {:ticks 0 :keys-seen []})
                :tick   (fn [world _dt] (update world :ticks inc))
                :draw   (fn [_world] nil)
                :on-key (fn [world keycode] (update world :keys-seen conj keycode))
                :stop?  (fn [world] (>= (:ticks world) 3))})]
    (is (= 3 (:ticks final)))
    (is (vector? (:keys-seen final))
        "on-key must fold over world even when no keys were pressed this run (an empty reduce is a no-op, not an error)")))

(deftest ^:windowed run-game-defaults-background-to-raywhite-without-erroring
  (let [final (game-loop/run-game!
               {:title  "test"
                :width  100
                :height 100
                :init   (fn [] {:ticks 0})
                :tick   (fn [world _dt] (update world :ticks inc))
                :draw   (fn [_world] nil)
                :stop?  (fn [world] (>= (:ticks world) 1))})]
    (is (= 1 (:ticks final)))))

(deftest step-fixed-calls-tick-once-per-fixed-dt-chunk-of-elapsed-time
  (let [calls (atom [])
        tick  (fn [w dt] (swap! calls conj dt) (update w :n inc))
        [world' acc'] (game-loop/step-fixed tick {:n 0} 0.0 0.025 0.01 10)]
    (is (= 2 (:n world')) "25ms elapsed / 10ms fixed-dt = 2 whole steps")
    (is (< (Math/abs (- acc' 0.005)) 1e-9) "5ms of unspent time carries over")
    (is (= [0.01 0.01] @calls))))

(deftest step-fixed-caps-steps-per-frame-to-avoid-spiral-of-death
  (let [tick (fn [w _dt] (update w :n inc))
        [world' _acc'] (game-loop/step-fixed tick {:n 0} 0.0 10.0 0.01 5)]
    (is (= 5 (:n world'))
        "even though 1000 whole steps would fit in 10s of accumulated time, max-steps caps it at 5 per frame")))

(deftest step-fixed-recovers-after-one-transient-stall
  ;; A single 1s stall, then 30 ordinary 60fps frames — the docstring's
  ;; claim this fn IS good for: max-steps * fixed-dt (5 * 10ms = 50ms)
  ;; comfortably exceeds a 60fps frame's ~16.7ms, so the backlog drains
  ;; every normal frame after the stall and the game genuinely catches up.
  (let [tick (fn [w _dt] (update w :n inc))
        step (fn [w acc elapsed] (game-loop/step-fixed tick w acc elapsed 0.01 5))
        [w1 acc1] (step {:n 0} 0.0 1.0)]
    (is (< 0.949 acc1 0.951) "after the stall, 0.95s of backlog remains (1.0 - 5*0.01)")
    (let [[_wN accN] (reduce (fn [[w acc] _] (step w acc (/ 1.0 60.0)))
                             [w1 acc1]
                             (range 30))]
      (is (< accN 0.01)
          "30 ordinary frames later the backlog has fully drained below one
           fixed-dt chunk — this is genuine recovery, not just a bounded lag"))))

(deftest step-fixed-does-not-recover-under-a-sustained-fixed-dt-mismatch
  ;; No stall at all — every frame is an ordinary 60fps frame. But
  ;; fixed-dt is chosen small enough (1ms) that max-steps * fixed-dt
  ;; (5 * 1ms = 5ms) is LESS than the 60fps frame time (~16.7ms), so
  ;; every single frame adds more real time than the cap can simulate
  ;; away. The backlog grows forever under perfectly normal play — the
  ;; documented boundary of step-fixed's spiral-of-death guard.
  (let [tick (fn [w _dt] (update w :n inc))
        step (fn [w acc elapsed] (game-loop/step-fixed tick w acc elapsed 0.001 5))
        frames (reductions (fn [[w acc] _] (step w acc (/ 1.0 60.0)))
                           [{:n 0} 0.0]
                           (range 20))
        accs   (map second (rest frames))]
    (is (apply < accs)
        "the accumulator grows strictly every single frame — it never
         stabilizes or drains, even though nothing ever 'stalled'")
    (is (> (last accs) (* 10 (first accs)))
        "over 20 ordinary frames the backlog grows by more than 10x,
         demonstrating this is unbounded growth, not a bounded lag")))

(deftest ^:windowed run-game-with-fixed-dt-still-opens-and-closes-a-real-window
  (let [final (game-loop/run-game!
               {:title     "test"
                :width     100
                :height    100
                :fixed-dt  (/ 1.0 60.0)
                :init      (fn [] {:ticks 0})
                :tick      (fn [world _dt] (update world :ticks inc))
                :draw      (fn [_world] nil)
                :stop?     (fn [world] (>= (:ticks world) 1))})]
    (is (>= (:ticks final) 1)
        "at least one fixed step must have run before stop? could see :ticks >= 1")))
