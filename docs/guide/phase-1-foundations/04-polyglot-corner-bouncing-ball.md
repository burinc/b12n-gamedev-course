# Polyglot Corner: Bouncing Ball, Four Lisps

You just built a bouncing ball in JVM Clojure
([`exercises/phase_1/bouncing_ball.clj`](../../../exercises/phase_1/bouncing_ball.clj)).
Here's the exact same idea, already built, in the other Lisp dialects this
course's sibling repos use. You don't need to understand every line yet —
just notice how much of the *shape* survives the jump: init/tick/draw,
an immutable world value, the same raylib calls underneath.

## Jolt (native Clojure, Chez Scheme, no JVM)

[`bounce.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/bounce.clj)
in `b12n-raylib-jlt` — a bouncing ball with `IsKeyPressed`-driven pause and
an on-screen `DrawFPS` counter. Jolt calls raylib with **zero C shim code**
at all — it exploits real ABI facts about how C passes small structs
(you'll learn exactly how in Phase 3). Run it yourself if you have Jolt
installed: `cd b12n-raylib-jlt && bb bouncing-ball`.

## jank (native Clojure, C++/LLVM, no JVM)

`b12n-raylib-jnk`'s bouncing-ball port — same idea, a completely different
FFI philosophy: jank draws the line at *the value*, not *the call* — a
native raylib value can't leave the function that created it. You'll build
this same ball again, deliberately, in Phase 3's comparative module.

## What's identical, what's not

- **Identical**: the shape of the program. Every one of these has an
  `init`, something that runs every frame to move the ball, something
  that draws it, and a loop that ties them together.
- **Not identical**: how each dialect talks to raylib's C code
  underneath. JVM Clojure (what you're using) goes through JDK's Panama
  FFI. Jolt and jank each have their own answer, with real trade-offs —
  that's the whole subject of [Phase 3](../phase-3-four-lisps/index.md).

## Next

[Phase 2 — Arcade Classics](../phase-2-arcade-classics/01-pong.md): time
to build something with an actual win/lose condition.
