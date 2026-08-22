# Phase 3: Same Game, Three Lisps

You've built several games now, all on JVM Clojure. This lesson is about
*how* JVM Clojure, Jolt, and jank each talk to the exact same C library
(raylib) underneath, three genuinely different answers to one question:
**when a C function takes a small struct like `Color` or `Vector2` "by
value," how does a Lisp on top of it make that call at all?**

## The three-tier decision tree (Jolt's answer)

Jolt binds raylib with **zero C shim code**, by exploiting real facts
about how your CPU's calling convention (ABI) passes small structs:

1. **Struct fits in one register, all-integer fields → pack it as an
   int.** raylib's `Color` is 4 bytes, `{r,g,b,a}` as bytes, bit-identical
   to a `uint32_t`. Every `Color` argument is just `r | g<<8 | b<<16 |
   a<<24`, no native memory involved.
2. **Struct too big for a register (>16 bytes on AArch64) → pass a
   pointer.** `Camera2D`/`Camera3D` are 24/44 bytes, big enough that
   AArch64's own calling convention passes them *indirectly* (caller
   allocates, passes a pointer) even in C. Jolt just builds that struct
   in native memory and passes the pointer, this is AArch64-specific,
   not portable to x86-64 as-is.
3. **Struct is a small float aggregate (≤16 bytes, e.g. `Vector2`/
   `Vector3`) → neither trick above works** (floats go in different
   registers than integers, and it's too small for the pointer trick).
   raylib ships a fallback API for exactly this, `rlgl`: scalar
   immediate-mode calls (`rlVertex2f`, `rlColor4ub`, ...) where every
   argument is a plain number. Functions like `DrawTriangle`/`DrawCube`
   get rebuilt out of these instead of called directly.

Read the three source pages for the full mechanics, each with real code:
[`color-by-value.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/color-by-value.md),
[`struct-by-value-pointer-trick.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/struct-by-value-pointer-trick.md),
[`rlgl-immediate-mode.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/rlgl-immediate-mode.md).

## jank's different answer: the boundary is at the *value*, not the *call*

Jolt and JVM Clojure (what you've been using) both cross the FFI boundary
**at the call**: marshaling happens per-call, but once a raylib value is
on the Clojure side, it's an ordinary value: you can return it, hold it
in an atom, pass it through `loop`/`recur`, whatever you'd do with any
other Clojure value.

jank draws the line somewhere stricter: **a native C++ value can be
constructed and used inline, but it cannot cross a jank *function*
boundary at all**: not returned, not taken as a parameter, not carried
through `loop`/`recur`. Every native value must be built and consumed
within one `let`/call expression. Read
[`native-value-lifetimes.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/native-value-lifetimes.md)
for exactly what that forces in practice.

## Same library, three philosophies

| | Boundary | Portability | Shim code needed |
|---|---|---|---|
| JVM Clojure (`coffi`/Panama) | at the call | fully portable (JVM abstracts the ABI) | none |
| Jolt (`jolt.ffi`) | at the call | the pointer trick (tier 2) is AArch64-specific | none |
| jank | at the value | - | none, but the value-lifetime rule constrains *how you write code*, not just how it's bound |

None of these three need a hand-written C shim for raylib, that's
notable on its own (contrast: `b12n-tsj`, a sibling project binding
tree-sitter, needs a full C shim because its structs are both passed
*and returned* by value at sizes none of these tricks cover).

## Next

[Port your Pong](02-port-your-pong.md), read a real Jolt Pong
side-by-side with the one you built, then attempt a jank port yourself.
