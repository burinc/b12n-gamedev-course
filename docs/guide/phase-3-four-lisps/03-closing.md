# Closing: Same Game, Three Philosophies

You've now seen one game (Pong) built three ways over the same C
library. Before moving to Phase 4, write a short answer (a paragraph is
plenty) to each of these — there's no single correct answer, the point is
noticing the trade-offs for yourself:

1. **Which FFI philosophy would you want if you were building a much
   bigger game — hundreds of entities, complex structs?** (A good answer
   touches on: does "boundary at the call" or "boundary at the value"
   make it easier to hold onto lots of game objects across frames?)
2. **Which one would you want if you were optimizing for raw
   performance, no JVM/GC overhead at all?** (A good answer notices that
   both Jolt and jank are JVM-free — the real performance question is
   between those two and JVM Clojure, not between Jolt and jank.)
3. **Which one would you want if you were teaching a total beginner
   their very first Lisp?** (A good answer weighs "boring, portable,
   huge ecosystem" against "get closer to the metal from day one.")
4. **Now that you've seen jank's stricter value-lifetime rule up close —
   did it change *how* you thought about the Pong port, even before you
   wrote a line of code?** (A good answer notices that a constraint on
   *where* a value can live is a constraint on program *shape*, not just
   syntax.)

## Where this goes next

Every dialect in this course binds the exact same raylib C library three
different ways, for three different trade-offs — and none of them needed
a hand-written C shim to do it. That's not an accident of raylib being
simple; it's a real design space every FFI author navigates. If this
was the most interesting part of the course for you, [Bob Nystrom's
*Game Programming Patterns*](https://gameprogrammingpatterns.com/) and
this repo's own `docs/guide/*` pages in `b12n-raylib-jlt`/`b12n-raylib-jnk`
are worth reading end to end, not just the pages cited here.

## Next

[Phase 4 — Systems & Architecture](../phase-4-systems-architecture/index.md).
