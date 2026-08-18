# Lesson 5: Tetris

## Concepts

This lesson covers the **fundamental architecture of a falling-block puzzle game**: rotations in 2D, grid-based collision detection, line clearing as a state transition, and an explicit finite-state machine (falling → locking → clearing → spawning). Unlike the action games in earlier lessons, Tetris has distinct phases: a piece falls freely, collides with the floor or another piece, locks in place, lines clear, and the next piece spawns.

1. **2D Matrix as Core Data Structure** — The board is a `rows × cols` 2D vector of cells, each either `nil` (empty) or a color (filled). Pieces are represented as offsets within a 4×4 rotation box, decoupled from the board itself.
2. **Piece Rotation** — A 90-degree clockwise rotation transforms each offset `[r c]` to `[c, 3-r]` within a 4×4 box. This is a mathematical operation, not a sprite animation.
3. **Collision Detection via Boundary Checks** — A placement is valid if every cell of the piece (1) is within bounds horizontally, (2) does not go below the board, and (3) does not overlap an already-locked piece. Negative rows (above the visible board) are allowed during spawn.
4. **Line Clearing** — When a piece locks, any row with all cells filled is removed. Rows above shift down, and empty rows are added to the top. Score increases by 100 per cleared line.
5. **Explicit State Machine** — The game progresses through: falling (piece descends each tick), locking (piece collides, gets fixed to the board), clearing (full lines are removed), spawning (a new piece appears), and lost (no room for a new piece).

**Scope note — this is a simplified Tetris:** it uses 90-degree rotation with no wall-kick table. If a rotation would push the piece out of bounds or into a filled cell, it is simply rejected—the piece stays in its original orientation. Real Tetris (SRS: Super Rotation System) nudges pieces back in; this version does not. The game also has no piece preview or hold buffer. This simplification keeps the focus on the core mechanics (rotation, grid, line clear, state machine) without the complexity of SRS or preview logic.

You'll implement a single-player Tetris where pieces fall, rotate, move left/right (with Down for soft-drop acceleration), lock when they hit obstacles, and clear lines as they fill.

## Starter Code

Open `exercises/phase_2/tetris_starter.clj` and fill in the three TODOs:

```clojure
(ns phase-2.tetris-starter
  "Phase 2, Lesson 5 — Tetris (simplified: 90-degree rotation, no wall
   kicks, no hold/preview). Left/Right move, Up rotates, Down soft-drops."
  (:require [gamedev-course.engine.game-loop :as game-loop]
            [gamedev-course.engine.raylib.core.keyboard :as keyboard]
            [gamedev-course.engine.raylib.enums :as enums]
            [gamedev-course.engine.raylib.shapes.basic :as shapes]
            [gamedev-course.engine.raylib.text.drawing :as text]
            [gamedev-course.engine.raylib.colors :as colors]))

(def cols 10)
(def rows 20)
(def cell 24)
(def width (* cols cell))
(def height (* rows cell))
(def fall-interval 0.5)

;; Every shape is a set of [row col] offsets inside a 4x4 box (col/row 0..3).
(def shapes
  {:I [[1 0] [1 1] [1 2] [1 3]]
   :O [[0 1] [0 2] [1 1] [1 2]]
   :T [[0 1] [1 0] [1 1] [1 2]]
   :S [[0 1] [0 2] [1 0] [1 1]]
   :Z [[0 0] [0 1] [1 1] [1 2]]
   :J [[0 0] [1 0] [1 1] [1 2]]
   :L [[0 2] [1 0] [1 1] [1 2]]})

(def piece-color
  {:I colors/skyblue :O colors/yellow :T colors/purple
   :S colors/green :Z colors/red :J colors/blue :L colors/orange})

(defn- rotate-cw [offsets]
  ;; TODO: Rotate each [r c] offset inside a 4x4 box by transforming it
  ;; to [c, 3-r]. This represents a 90-degree clockwise rotation around
  ;; the box's center.
  )

(defn- rand-kind [] (rand-nth (keys shapes)))

(defn- spawn [kind]
  {:kind kind :offsets (shapes kind) :row 0 :col 3})

(defn init []
  {:board       (vec (repeat rows (vec (repeat cols nil))))
   :current     (spawn (rand-kind))
   :fall-timer  0.0
   :status      :playing})

(defn- cell-positions [{:keys [offsets row col]}]
  (map (fn [[r c]] [(+ row r) (+ col c)]) offsets))

(defn- fits? [board positions]
  ;; TODO: Return true if every [r c] position is valid. A position is
  ;; valid when: (1) it is within bounds horizontally (0 <= c < cols),
  ;; (2) it does not go below the board (r < rows), and (3) for
  ;; non-negative rows only (rows above the board don't count), the cell
  ;; at [r c] is empty (nil). Negative rows are used during spawn and
  ;; should always be considered valid.
  )

(defn- move-piece [{:keys [board current] :as world} dcol]
  (let [moved (update current :col + dcol)]
    (if (fits? board (cell-positions moved))
      (assoc world :current moved)
      world)))

(defn- rotate-piece [{:keys [board current] :as world}]
  (let [rotated (assoc current :offsets (rotate-cw (:offsets current)))]
    (if (fits? board (cell-positions rotated))
      (assoc world :current rotated)
      world)))

(defn- clear-lines [board]
  ;; TODO: Remove every row that is completely filled (every cell is
  ;; non-nil). Return a map {:board <new-board> :cleared <count>} where
  ;; <new-board> has all non-full rows preserved in order, and the
  ;; appropriate number of empty rows added back to the top to maintain
  ;; a 20-row board.
  )

(defn- lock-piece [{:keys [board current] :as world}]
  (let [locked (reduce (fn [b [r c]]
                         (if (neg? r) b (assoc-in b [r c] (piece-color (:kind current)))))
                       board (cell-positions current))
        {:keys [board cleared]} (clear-lines locked)
        next-piece (spawn (rand-kind))]
    (-> world
        (assoc :board board)
        (update :score (fnil + 0) (* cleared 100))
        (assoc :current next-piece)
        (cond-> (not (fits? board (cell-positions next-piece))) (assoc :status :lost)))))

(defn- try-fall [{:keys [board current] :as world}]
  (let [dropped (update current :row inc)]
    (if (fits? board (cell-positions dropped))
      (assoc world :current dropped)
      (lock-piece world))))

(defn- handle-input [world]
  (cond-> world
    (keyboard/is-key-pressed? (:left enums/keyboard-key))  (move-piece -1)
    (keyboard/is-key-pressed? (:right enums/keyboard-key)) (move-piece 1)
    (keyboard/is-key-pressed? (:up enums/keyboard-key))    rotate-piece))

(defn- tick [world dt]
  (if (not= :playing (:status world))
    world
    (let [world (handle-input world)
          fast? (keyboard/is-key-down? (:down enums/keyboard-key))
          timer (+ (:fall-timer world) (if fast? (* dt 8) dt))]
      (if (>= timer fall-interval)
        (-> world (assoc :fall-timer (- timer fall-interval)) try-fall)
        (assoc world :fall-timer timer)))))

(defn- draw-cell! [row col color]
  (shapes/draw-rectangle! (* col cell) (* row cell) (dec cell) (dec cell) color))

(defn- draw [{:keys [board current status score]}]
  (dotimes [r rows]
    (dotimes [c cols]
      (when-let [color (get-in board [r c])] (draw-cell! r c color))))
  (doseq [[r c] (cell-positions current) :when (not (neg? r))]
    (draw-cell! r c (piece-color (:kind current))))
  (text/draw-text! (str "score " (or score 0)) 10 (- height 24) 20 colors/raywhite)
  (when (= :lost status) (text/draw-text! "GAME OVER" (- (/ width 2) 90) (/ height 2) 30 colors/red)))

(defn -main [& _args]
  (game-loop/run-game!
   {:title      "Tetris"
    :width      width
    :height     height
    :init       init
    :tick       tick
    :draw       draw
    :background colors/black}))
```

## Run It

From the repo root:

```bash
clojure -M:run -m phase-2.tetris-starter
```

Use arrow keys (Left/Right) to move the falling piece, Up to rotate it, and Down to accelerate its fall (soft-drop). Try to complete rows—when a row is completely filled, it disappears and you earn 100 points. The game ends when a new piece cannot spawn (the board is too full).

## Hints

### `rotate-cw`

Inside a 4×4 bounding box, a 90-degree clockwise rotation transforms each `[row col]` offset to `[col, 3-row]`. This is a fixed-size box rotation that works for all tetrominoes.

Example: the I-piece is originally `[[1 0] [1 1] [1 2] [1 3]]` (a horizontal line in the middle of the box). After rotation, it becomes `[[0 2] [1 2] [2 2] [3 2]]` (a vertical line at column 2).

If a rotation would move the piece out of bounds or into a filled cell, it will be rejected by the `fits?` check in `rotate-piece`, and the piece will stay in its current orientation. This is the "no wall-kick" behavior mentioned in the scope note.

### `fits?`

A set of cell positions is valid if all of them pass these checks:

1. **Horizontal bounds:** `0 <= col < cols` (cols is 10).
2. **Vertical bounds:** `row < rows` (rows is 20). Note: rows *can* be negative (during spawn), and those are always valid.
3. **Occupancy:** if and only if the row is non-negative, the cell at `[row col]` in the board must be `nil` (empty). Use `nil? (get-in board [row col])`.

Combine all three checks with `and` for each position, and `every?` across all positions.

### `clear-lines`

When a piece locks, check which rows are completely full:

1. A row is full if every cell (all 10 columns) is non-nil.
2. Keep only the rows that have at least one `nil` cell (i.e., remove the full rows).
3. Count how many rows were removed.
4. Pad the board back to 20 rows by adding that many empty rows to the *top* (using `into` or `concat`).

Return a map with `:board` (the new board) and `:cleared` (the count), since `lock-piece` uses both to update the score and check for game-over.

### Discrete Falling and Locking

Unlike Space Invaders' continuous motion, Tetris pieces fall on a timer. Every 0.5 seconds (or faster if Down is held), `try-fall` is called. If the piece can move down one more row, it does. If not, it locks immediately. This is the state transition from "falling" to "locking" to "clearing" to "spawning."

## Compare Against the Solution

Once you've got it working, read `exercises/phase_2/tetris.clj` to compare your implementation.

### No Wall-Kicks in This Version

Real Tetris (SRS) has a wall-kick table: when a rotation would fail, it tries a short sequence of nudges — up to 1 cell horizontally for most pieces, up to 2 cells for the I-piece (which has its own kick table since its rotation axis differs from the 3×3 pieces) — plus a small vertical nudge. Our version doesn't—a failed rotation is simply rejected. This simplifies the code while still teaching rotation and collision detection. If you're curious about wall-kicks, the SRS specification is a good follow-up research topic.

### Score Calculation

Each cleared line is worth 100 points. Multiple lines cleared at once (a "tetris" or "T-spin") are worth the same: 100 per line. More complex scoring (bonus for T-spins or back-to-back clears) is left as an exercise.

## Polyglot Corner

See this same design in other Clojure raylib bindings:

- **Jolt + raylib-jlt:** [`b12n-raylib-jlt/src/net/b12n/raylib_jlt/tetris.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/tetris.clj) — 10×20 well, 7 tetrominoes, rotation, line-clearing. The Jolt version includes level progression and gravity speedup, which this course's version omits in favor of simplicity.

---

**Next:** [Lesson 6: Flappy Bird](06-flappy-bird.md) — the last lesson of Phase 2.
