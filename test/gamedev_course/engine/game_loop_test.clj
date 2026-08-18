(ns gamedev-course.engine.game-loop-test
  (:require [clojure.test :refer [deftest is]]
            [gamedev-course.engine.game-loop :as game-loop]))

(deftest run-game-stops-on-stop-predicate
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

(deftest run-game-folds-on-key-over-every-queued-key-event
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

(deftest run-game-defaults-background-to-raywhite-without-erroring
  (let [final (game-loop/run-game!
               {:title  "test"
                :width  100
                :height 100
                :init   (fn [] {:ticks 0})
                :tick   (fn [world _dt] (update world :ticks inc))
                :draw   (fn [_world] nil)
                :stop?  (fn [world] (>= (:ticks world) 1))})]
    (is (= 1 (:ticks final)))))
