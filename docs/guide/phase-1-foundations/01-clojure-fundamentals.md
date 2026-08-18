# Phase 1, Lesson 1 — Clojure Fundamentals

> **Already comfortable with `def`, `defn`, `let`, and basic recursion in
> Clojure? Skip to [Phase 1, Lesson 2 — The Game Loop](02-the-game-loop.md).**

## Values and `def`

Clojure has a few essential data types: numbers, strings, keywords, vectors (indexed collections), and maps (dictionaries). You create named values with `def`:

```clojure
(def player-name "Ada")
(def player-score 0)
(def player-position {:x 100 :y 200})
player-position
;; => {:x 100, :y 200}
```

Notice that `:x` and `:y` are *keywords* — a Clojure type that's perfect for map keys. When you evaluate `player-position` by itself, the REPL shows you what it contains.

## Functions with `defn`

You define functions using `defn`. Here's a function that moves a position to the right:

```clojure
(defn move-right [position amount]
  (update position :x + amount))

(move-right player-position 10)
;; => {:x 110, :y 200}
```

**Important:** `move-right` didn't change `player-position` — it returned a *new* map. Nothing in Clojure mutates by default. This matters a lot once you meet `run-game!` in the next lesson: your `tick` function will work exactly like `move-right` does here.

## `let` for Local Bindings

Sometimes you need temporary variables for calculations. Use `let`:

```clojure
(defn distance [a b]
  (let [dx (- (:x a) (:x b))
        dy (- (:y a) (:y b))]
    (Math/sqrt (+ (* dx dx) (* dy dy)))))

(distance {:x 0 :y 0} {:x 3 :y 4})
;; => 5.0
```

The `let` gives you `dx` and `dy` to work with inside the function. Outside the function, they don't exist.

## Recursion with `loop` and `recur`

Clojure doesn't have traditional for-loops. Instead, it uses recursion. Here's a `loop`/`recur` pattern that counts down — the same pattern that powers `run-game!`'s frame loop (which you'll read the source of in the next lesson):

```clojure
(defn countdown [n]
  (loop [remaining n acc []]
    (if (zero? remaining)
      acc
      (recur (dec remaining) (conj acc remaining)))))

(countdown 5)
;; => [5 4 3 2 1]
```

`loop` sets up the initial bindings (`remaining` starts at `n`, `acc` is an empty vector). `recur` jumps back to the top with new values. When `remaining` reaches zero, we return `acc`.

## Your Turn: `clamp`

Write a function `clamp` that takes `value`, `min-value`, and `max-value`, returning `value` pinned into that range. You'll use exactly this pattern to keep a paddle or ball on-screen starting in Phase 2.

```clojure
(defn clamp [value min-value max-value]
  (max min-value (min value max-value)))

(clamp 150 0 100) ;; => 100
(clamp -5 0 100)  ;; => 0
(clamp 50 0 100)  ;; => 50
```

The trick: `(min value max-value)` clamps to the max, and `(max min-value ...)` clamps to the min.

---

Ready? Move on to [Phase 1, Lesson 2 — The Game Loop](02-the-game-loop.md).
