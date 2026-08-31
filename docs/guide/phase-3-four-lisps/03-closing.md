# Closing: Same Game, Three Philosophies

You've now seen one game (Pong) built three ways over the same C
library. Before moving to Phase 4, write a short answer (a paragraph is
plenty) to each of these, there's no single correct answer, the point is
noticing the trade-offs for yourself:

1. **Which FFI philosophy would you want if you were building a much
   bigger game, hundreds of entities, complex structs?** (A good answer
   touches on: does "boundary at the call" or "boundary at the value"
   make it easier to hold onto lots of game objects across frames?)
2. **Which one would you want if you were optimizing for raw
   performance, no JVM/GC overhead at all?** (A good answer notices that
   both Jolt and jank are JVM-free, the real performance question is
   between those two and JVM Clojure, not between Jolt and jank.)
3. **Which one would you want if you were teaching a total beginner
   their very first Lisp?** (A good answer weighs "boring, portable,
   huge ecosystem" against "get closer to the metal from day one.")
4. **Now that you've seen jank's stricter value-lifetime rule up close -
   did it change *how* you thought about the Pong port, even before you
   wrote a line of code?** (A good answer notices that a constraint on
   *where* a value can live is a constraint on program *shape*, not just
   syntax.)

## Where this goes next

Every dialect in this course binds the exact same raylib C library three
different ways, for three different trade-offs, and none of them needed
a hand-written C shim to do it. That's not an accident of raylib being
simple; it's a real design space every FFI author navigates. If this
was the most interesting part of the course for you, [Bob Nystrom's
*Game Programming Patterns*](https://gameprogrammingpatterns.com/) is
the architecture half — and for the FFI half, both sibling suites keep
`docs/guide/` directories worth reading end to end, not just the pages
this phase links. The ones this lesson never had room for:

**`raylib-jlt`** —
[`kwarg-drawing-api.md`](https://github.com/jlt-commons/raylib-jlt/blob/main/docs/guide/kwarg-drawing-api.md)
(how a raw C surface gets an ergonomic Clojure one on top), and
[`headless-smoke-testing.md`](https://github.com/jlt-commons/raylib-jlt/blob/main/docs/guide/headless-smoke-testing.md)
(how you test a thing whose whole job is to open a window).

**`b12n-raylib-jnk`** —
[`cpp-interop-toolbox.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/cpp-interop-toolbox.md)
(the `cpp/` surface, one entry per tool),
[`type-checking-and-coercion.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/type-checking-and-coercion.md)
and
[`numeric-performance.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/numeric-performance.md)
(what a native-compiled Clojure actually costs you per number), and
[`jvm-surface-gaps.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/jvm-surface-gaps.md)
(what a JVM-Clojure habit finds missing, which is the honest counterpart
to everything Phase 3 sold you on).

Both suites are still moving, so treat any count in this phase as a
snapshot: their own `example-catalog.md` is the live number.

## Next

[Phase 4: Systems & Architecture](../phase-4-systems-architecture/01-fixed-timestep.md).
