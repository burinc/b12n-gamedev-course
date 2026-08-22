# Procedural Generation, Simple AI & Testing a Game

> **Not public yet.** This page links to `b12n-ohuntley`, which is still
> private, so those links will 404 for now. They're being opened up as
> the course progresses, and this note goes away when they are. The
> three raylib suites the rest of the course is built on
> ([clj](https://github.com/burinc/b12n-raylib-clj),
> [jlt](https://github.com/burinc/b12n-raylib-jlt),
> [jnk](https://github.com/burinc/b12n-raylib-jnk)) are public today.

After building the game loop and rendering pipeline, we turn to the systems that make a game *feel* alive: procedurally generated content that's different every playthrough, AI agents that create challenge and presence, and the testing discipline that ensures both work correctly.

## Procedural Generation: Building Solvable Mazes

A procedurally generated maze is different every time the game starts, but it must always be solvable-otherwise the player can't win. How do you guarantee that?

Compare two implementations:

**Simple maze generation** (`raylib-clj`'s `games/retro_maze_3d.clj`): Uses **recursive backtracking**, a classic maze generation algorithm. Start at the top-left corner, carve a passage to a random unvisited neighbor, and repeat. When you reach a dead end (no unvisited neighbors), backtrack to the last cell that had choices, and try again. The algorithm terminates when every cell has been visited exactly once.

```clojure
(defn generate-maze [w h]
  (let [start-x 0
        start-y 0
        grid (-> (make-grid w h)
                 (assoc-in [start-y start-x :visited] true))]
    (generate-maze-step grid [[start-x start-y]])))
```

The result: a perfect maze (every cell reachable from every other, no loops) where there is always a path from start to any goal.

**Larger implementation** (`b12n-ohuntley`'s `src/ohuntley/maze.cljc`): Uses the same recursive backtracking algorithm, but adds one critical piece: after carving, it **finds the furthest cell from the start using breadth-first search** and makes *that* the exit. This is deterministic, it always picks the cell that requires the longest path to reach, maximizing the player's challenge.

```clojure
(defn find-furthest-cell
  "Find the cell furthest from start using BFS."
  [grid width height start-x start-y]
  ;; BFS that walks only through passages (no walls)
  ;; Returns the cell with the maximum distance
  ...)

(defn generate-maze [width height seed]
  (let [random-fn (create-random seed)
        grid (create-grid width height)
        carved-grid (carve-passages grid width height random-fn 0 0)
        exit (find-furthest-cell carved-grid width height 0 0)]
    ...))
```

### Your Turn: The Solvability Question

**Read both implementations** to understand how they work:
- In `retro_maze_3d.clj` (lines 88-107), trace through `generate-maze-step`. What happens when a cell has no unvisited neighbors? What does the algorithm guarantee about cell connectivity?
- In `ohuntley/maze.cljc` (lines 117-149), examine `carve-passages`. Does it visit every cell? Does it guarantee connectivity? Then look at `find-furthest-cell` (lines 155-190): what does it do, and does it affect whether a path to the exit exists?

**Work through a small example on paper**: a 3x3 or 4x4 grid, to trace the algorithm step by step. Watch the stack grow and shrink. Watch walls get removed.

**Then answer:** What has to be true of a randomly generated maze for it to be solvable, and where in the generation code does that get guaranteed? (Or does the code not guarantee it? If so, what's missing?)

## Simple AI: Two Shapes of Emergent Behavior

There are many ways to write an AI agent. Two common approaches appear across this course's examples:

**Emergent behavior from local rules** (`raylib-jlt`'s `boids.clj`): About 70 agents, each running three simple calculations every frame:
1. **Cohesion**: "Move toward the average position of nearby agents"
2. **Alignment**: "Match the average velocity of nearby agents"
3. **Separation**: "Move away from nearby agents to avoid crowding"

No state machine. No named behaviors. Just three weighted forces applied to each agent every frame. Yet the flock patterns that emerge-starling-like murmurations, flowing around obstacles, splitting and rejoining-look lifelike and coordinated.

```clojure
(defn step-boid [b boids]
  (let [near (near-boids b boids)
        ;; Cohesion: toward average position
        ;; Alignment: match average velocity  
        ;; Separation: repel from neighbors
        vx (+ (:vx b) 
              (* 0.0008 (- ax (:x b)))        ;; cohesion
              (* 0.05 (- avx (:vx b)))        ;; alignment
              (* 0.0010 sx))                   ;; separation
        vy (+ (:vy b) ...)])
    ;; Update position
    {:x (mod (+ (:x b) vx) width) ...}))
```

**Explicit state machines** (`b12n-ohuntley`'s `src/ohuntley/entities/zombie.cljc`): Each zombie is in exactly one of four states:
- **Patrol**: Follow waypoints around the maze
- **Chase**: Pursue the player (if they can be seen via line-of-sight)
- **Returning**: Go back to the patrol waypoint after the player is lost
- **Frozen**: Temporary immobilization from a power-up

Each state has explicit transitions: patrol sees the player → chase; chase doesn't see the player for N frames → returning; returning reaches the waypoint → patrol. The behavior is clear and debuggable: you can print the zombie's current state and know exactly what it's doing.

```clojure
(defn update-patrol [zombie delta maze player]
  (cond
    ;; Detect player -> transition to chase
    (and player (can-see-player? maze zombie player))
    (start-chase ...)
    
    ;; Path done -> advance waypoint
    (nil? (:current-path zombie))
    (advance-waypoint ...)
    
    ;; Keep moving
    :else
    (move-along-path ...)))
```

### The Core Difference

Both produce "simple AI"-neither runs pathfinding every frame or evaluates complex heuristics. But they represent opposite design philosophies:

- **Boids** (emergent): Minimal state, behavior arises from rules, harder to debug, easier to scale to hundreds of agents
- **Zombies** (state machine): Maximal clarity, each state clearly means something, easier to add special cases, requires more code per agent

Neither is "better"; they're different shapes of simplicity. Emergent AI shines when you want lifelike swarms or flocks. State machines excel for characters with clear roles and understandable personality.

## Testing a Game

How do you test gameplay? The naive approach-"automate a player running through the game"-doesn't work because you'd need to script every possible playthrough, every random event, every corner case.

The practical answer: **Test the logic, smoke-test the shell.**

**b12n-ohuntley's test suite** (58 tests, 455 assertions) is split into five modules, each testing a pure-logic system:

| Module | Tests | Assertions | What It Tests |
|--------|-------|------------|---|
| Maze | 10 | 251 | Maze generation, pathfinding, reachability |
| Pathfinding | 12 | - | A* pathfinding correctness |
| Zombie AI | 15 | - | State transitions, line-of-sight, pursuit logic |
| Combat | 9 | - | Damage calculation, effect application |
| Game state | 12 | - | Level progression, win/loss conditions |

Notice what's *not* tested in detail: the 3D rendering, the input handling, the audio playback. Those subsystems are smoke-tested-"does the game window open, accept input, and run without crashing"-but not unit-tested frame by frame. The cost would be astronomical, and the return would be low; rendering bugs are usually caught by visual inspection, not assertions.

This course's own `game-loop` engine (its core `run-game!` built back in Phase 1, extended with `step-fixed` here in Phase 4) follows the same pattern: **pure logic functions are tested directly** (your `tick` functions, your collision checks, your game-state updates), while the windowed shell (`run-game!`) is only tested to confirm it starts and stops without panicking.

The insight: **Test the systems that matter, skip the systems that are obvious.** Maze generation could be wrong in subtle ways (generates unsolvable mazes, or mazes that are too easy). Zombie behavior could break on edge cases (what if the player freezes the zombie in mid-chase?). Game state could corrupt (what if the player wins and loses simultaneously?). Those all deserve tests. But whether your 3D camera rotates smoothly or your skybox renders in the right order-you'll see that when you play it.

## What's Next

You've now built the core systems that separate "interactive simulation" from "game":
- **Earlier in Phase 4**: Fixed timestep, component-based architecture (ECS), particles and object pooling
- **This lesson**: Procedural content, agent behavior, and testing discipline

[Phase 5](../phase-5-capstones/01-mobile-capstone.md) brings it together: you'll build a complete, playable game from scratch, integrating the systems you've learned into one cohesive experience. No scaffolding, no handholding-just the skills you've developed and the freedom to design.
