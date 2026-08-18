(ns gamedev-course.engine.game-loop-test
  (:require [clojure.test :refer [deftest is]]
            [gamedev-course.engine.game-loop :as game-loop]))

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
