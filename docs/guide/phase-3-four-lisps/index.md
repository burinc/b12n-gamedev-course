# Phase 3: Same Game, Three Lisps

You've built several games now, all on JVM Clojure. This lesson is about
*how* JVM Clojure, Jolt, and jank each talk to the exact same C library
(raylib) underneath, three genuinely different answers to one question:
**when a C function takes a small struct like `Color` or `Vector2` "by
value," how does a Lisp on top of it make that call at all?**

## The four-tier decision tree (Jolt's answer)

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
4. **Struct comes back *out* by value → none of the three help.**
   `LoadTexture` returns a 20-byte `Texture2D`, and on AArch64 anything
   over 16 bytes is returned *indirectly*: the caller allocates the
   result and passes its address in `x8`, a register reserved for
   exactly this. Chez's `foreign-procedure` has no spelling for "and
   here is the hidden result buffer in `x8`", so textures and
   framebuffers are reached through rlgl's scalar layer *underneath*
   raylib instead of through raylib's own texture API.

Read the four source pages for the full mechanics, each with real code:
[`color-by-value.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/color-by-value.md),
[`struct-by-value-pointer-trick.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/struct-by-value-pointer-trick.md),
[`rlgl-immediate-mode.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/rlgl-immediate-mode.md),
[`textures-via-rlgl.md`](https://github.com/burinc/b12n-raylib-jlt/blob/main/docs/guide/textures-via-rlgl.md).

### ...and then Jolt grew a fifth answer

Every tier above is a *workaround* for something `jolt.ffi` could not
say. As of **Jolt 0.7.23** it can say it: a binding may declare a
parameter or a return type as `[:by-value [:struct ...]]` and Jolt
handles the marshaling, including the `x8` indirect return that tier 4
exists to dodge. `b12n-raylib-jlt`'s six shader examples are built on
it, because `LoadShader` returns a `Shader` by value and there was no
other way to bind it at all:

```clojure
;; src/net/b12n/raylib_jlt/raylib.clj -- a by-value struct RETURN
(ffi/defcfn ^:private load-shader-from-memory "LoadShaderFromMemory"
  [:pointer :string]
  [:by-value [:struct [[:id :uint] [:locs :pointer]]]])
```

So why read the four tiers at all? Two reasons, and they are the
reason this lesson still leads with them:

- **The shipped bindings still use them.** The suite predates 0.7.23 and
  has not been rewritten onto the by-value API, so the code you will
  actually read in `raylib.clj` is the four-tier code. Its own catalog
  says as much: the by-value support "supersedes the workarounds," and
  a page describing the new API "is not written yet."
- **The tricks are the transferable part.** `[:by-value ...]` is one
  FFI's feature. "A 4-byte all-integer struct travels in a register
  bit-identically to a `uint32`" is a fact about your CPU, and it is
  still true in every language you will ever bind C from — including
  the ones whose FFI has no by-value support to grow.

The practical upshot if you go and write Jolt FFI code yourself today:
reach for `[:by-value [:struct ...]]` first, and treat tiers 1-4 as
what to do when you meet an FFI that cannot.

## jank's different answer: the boundary is at the *value*, not the *call*

Jolt and JVM Clojure (what you've been using) both cross the FFI boundary
**at the call**: marshaling happens per-call, but once a raylib value is
on the Clojure side, it's an ordinary value: you can return it, hold it
in an atom, pass it through `loop`/`recur`, whatever you'd do with any
other Clojure value.

jank draws the line somewhere stricter: **a native C++ value cannot
*implicitly* cross a jank function boundary** — not returned, not taken
as a parameter, not carried through `loop`/`recur`. By default a native
value is built and consumed within one `let`/call expression.

Two qualifications matter, and both are easy to over-read in either
direction:

- **It is about conversion, not about C++.** A type with a conversion
  trait — integrals, bools, C strings, `std::string` — crosses freely.
  It is `Color`, `Vector2`, `Rectangle`, `Camera2D` that have none.
- **There is a documented way out.** `cpp/new` + `cpp/box` gives you an
  ordinary jank object you *can* return, put in a vector, or hold in an
  atom, and `cpp/unbox` gets the value back. The box has to be
  heap-allocated precisely because it outlives the scope that made it.
  So "cannot cross" means "cannot cross for free", not "cannot cross".

Even so, the rule shapes the code far more than a marshaling detail
would: a closure counts as a fn boundary, so `dotimes` and `doseq` break
where `loop`/`recur` works, and an accessor you would naturally factor
into a `defn` cannot be. Read
[`native-value-lifetimes.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/native-value-lifetimes.md)
for the six faces of that one rule, each with the committed example
that proves it.

## Same library, three philosophies

| | Boundary | Portability | Shim code needed |
|---|---|---|---|
| JVM Clojure (`coffi`/Panama) | at the call | fully portable (JVM abstracts the ABI) | none |
| Jolt (`jolt.ffi`) | at the call | the shipped bindings' pointer trick (tier 2) is AArch64-specific; `[:by-value ...]`, since 0.7.23, is not | none |
| jank | at the value | portable — the constraint is jank's, not the ABI's | none, but the value-lifetime rule constrains *how you write code*, not just how it's bound |

None of these three need a hand-written C shim for raylib, that's
notable on its own (contrast: `b12n-tsj`, a sibling project binding
tree-sitter, needs a full C shim because its structs are both passed
*and returned* by value at sizes none of these tricks cover).

## Next

[Port your Pong](02-port-your-pong.md), read a real Jolt Pong
side-by-side with the one you built, then attempt a jank port yourself.
