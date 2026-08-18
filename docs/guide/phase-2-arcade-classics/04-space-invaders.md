# Lesson 4: Space Invaders

## Concepts

This lesson introduces techniques for managing **multiple entities of different types** as simple data structures, along with **formation-level collision detection** and **win/lose conditions over a whole collection**. Space Invaders is the first game in this ladder where the enemy is not a single object, but a grid of entities that move together:

1. **Multiple Entity Types as Data** — the player, a single bullet (or nil), and an array of enemies are all just maps. No objects, no inheritance; just plain Clojure data.
2. **Formation Movement** — all enemies move as one unit. When any enemy touches an edge, the entire formation drops and reverses direction (`enemy-dir`). This is coordination at the collection level.
3. **One Bullet in Flight** — unlike modern games, classic Space Invaders only allows one bullet at a time. The `:bullet` field is either `nil` or a map; it can only spawn when the previous bullet has been destroyed or left the screen.
4. **Win/Lose Conditions Over Collections** — the game ends when **all enemies are destroyed** (`:status :won`) or when **any alive enemy reaches the player row** (`:status :lost`).

You'll implement a single-player Space Invaders where you move left/right, fire upward, and either clear all enemies before they reach you or lose when they do.

## Starter Code

Open `exercises/phase_2/space_invaders_starter.clj` and fill in the three TODOs:

```clojure
(ns phase-2.space-invaders-starter
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

;; ... rest of the code, with three TODOs in:
;; - `maybe-fire`
;; - `bullet-hits?`
;; - `move-enemies`
```

## Run It

From the repo root:

```bash
clojure -M:run -m phase-2.space-invaders-starter
```

Use arrow keys to move left and right. Press Space to fire. Destroy all enemies to win; if any reach the bottom row (where your ship sits), you lose.

## Hints

### `maybe-fire`

You can only fire when:
1. No bullet is already in flight (`:bullet` is `nil`), **and**
2. The Space key was **just pressed** (not held).

Use `keyboard/is-key-pressed?` to detect a single press event, and spawn a bullet at the player's center x-coordinate, at `player-y` (the player's y position). The bullet map should have `:x` and `:y` keys.

### `bullet-hits?`

Implement **AABB (Axis-Aligned Bounding Box) overlap detection** between the bullet rectangle and an enemy rectangle:

- The bullet occupies the rectangle `[bullet.x, bullet.x + bullet-w) × [bullet.y, bullet.y + bullet-h)`.
- The enemy occupies the rectangle `[enemy.x, enemy.x + enemy-w) × [enemy.y, enemy.y + enemy-h)`.
- Two rectangles overlap if they share any area on both axes.

**Overlap condition:** two intervals `[a, a+w)` and `[b, b+w')` overlap if and only if `a < b + w'` **and** `b < a + w`. Apply this to both x and y axes.

### `move-enemies`

The alive formation moves as one unit:

1. **Find the alive enemies** (filter by `:alive?`).
2. **Compute their bounding box**: find the minimum and maximum x-coordinates among all alive enemies.
   - The left edge is `min-x`.
   - The right edge is `max-x + enemy-w` (the rightmost enemy's far side).
3. **Detect edge collision**: the formation hits an edge if the left edge is close to the left boundary (e.g., `<= 10`) or the right edge is close to the right boundary (e.g., `>= (- width 10)`).
4. **If edge collision**, drop the formation:
   - Increase every enemy's `:y` by `enemy-drop`.
   - Flip `:enemy-dir` (multiply by -1).
5. **Otherwise**, shift every enemy horizontally by `enemy-speed * enemy-dir * dt`.

Remember: `enemy-dir` is `1` (moving right) or `-1` (moving left). The direction determines the sign of the horizontal shift.

### Discrete vs. Continuous Motion

Unlike Snake's timer-based movement, Space Invaders' enemies and bullet move **every frame**, scaled by `dt`. This gives smooth continuous motion. The formation drop only happens at edge collision, not on a timer.

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/space_invaders.clj` to compare your implementation.

### A Note on One Bullet at a Time

The classic Space Invaders constraint — one bullet per ship — is a design choice, not a technical limitation. It makes the game harder: you must time your shots carefully. Modern games often allow multiple bullets because it feels more responsive. Here, `maybe-fire` enforces the constraint by checking `:bullet` before spawning.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **ClojureDart + raylib-jlt:** [`b12n-raylib-jlt/src/net/b12n/raylib_jlt/space_invaders.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/space_invaders.clj) — marching alien grid, shoot up.

---

**Next:** [Lesson 5: Asteroids](05-asteroids.md) *(coming soon)*
