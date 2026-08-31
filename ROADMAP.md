# ROADMAP

Documents what this course covers, phase by phase, as of its first
complete version. Written at course completion, not maintained live
during the build, see `git log` for the actual build-out history if
that's what you're after.

## Phase B: Bootstrap
- [x] Repo skeleton, vendored raylib FFI layer, headless smoke-test
      helpers, the `game-loop` engine

## Phase 0: First Contact
- [x] First Contact page + environment setup

## Phase 1: Foundations
- [x] Clojure fundamentals (skippable)
- [x] The Game Loop lesson + worked bouncing-ball demo
- [x] Following Eyes exercise (mouse input)
- [x] Polyglot Corner #1 (bouncing ball, three Lisps)

## Phase 2: Arcade Classics
- [x] Pong, Breakout, Snake, Space Invaders, Tetris, Flappy Bird -
      each with a guided exercise + lesson page + Polyglot Corner

## Phase 3: Same Game, Three Lisps
- [x] FFI models lesson (color-by-value, pointer trick, rlgl, jank's
      value-boundary model)
- [x] Port-your-Pong guided cross-repo exercise
- [x] Closing comparative writeup

## Phase 4: Systems & Architecture
- [x] Fixed timestep (extends the game-loop engine, real new code)
- [x] ECS (curated from `herfi`)
- [x] Particles, pooling & math for games
- [x] Procedural generation, simple AI & testing a game

## Phase 5: Realistic Capstones + Track B
- [x] Mobile capstone (`rogue-shooter` → `crystal-ball`)
- [x] Web capstone (remix `cljsapp`, ship via GitHub Pages)
- [x] Go-deep capstone (`herfi`, `ohuntley`)
- [x] Track B: Web Games remix playground

## Closing
- [x] Where to go next
- [x] Doc wiring (this file, `docs/guide/index.md`, `README.md`)

## Known gaps / deliberately out of scope for this version

Per the design spec's explicit v1 scope cuts:
- No auto-grading beyond compile-check + headless-smoke-check.
- No instructor/cohort mode (schedules, slides, timing guides).
- `b12n-flame-3d` deferred, not yet documented in the wiki catalog the
  way `crystal-ball`/`rogue-shooter` are.
- Not yet mirrored to `b12n-wikis` or published via the shared b12n-docs
  site engine, both are real follow-ups, not silently dropped.

## Follow-ups: Jolt has moved a long way (notes, not commitments)

Written 2026-08-26 against **Jolt v0.7.27**. `raylib-jlt` gates on
v0.7.23 and its suite compiles clean on 0.7.27 (`bb check`, verified).
These are things the newer Jolt makes possible that this course, or the
suite it reads from, does not yet use. Nothing here is broken today.
Items 4 and 5 were cheap enough to just do; the rest are notes.

### 1. `[:by-value [:struct ...]]` — the one that touches this course

**Since 0.7.23.** `foreign-fn` and `defcfn` accept
`[:by-value [:struct ...]]` for both arguments and return types, which
is exactly the capability the four ABI tricks in
[Phase 3](docs/guide/phase-3-four-lisps/index.md) exist to work around.
Nested structs and fixed aggregates before `:varargs` work; aggregate
*callbacks*, variadic aggregate arguments, and aggregate returns
combined with `:varargs` do not.

Phase 3 now names this, so the lesson is honest. The open question is
what happens if `raylib-jlt` ever rewrites its bindings onto it:
that would make the lesson's four tiers describe code that no longer
exists. **The recommendation is not to chase that.** The tiers are
facts about AArch64 and x86-64, not about Jolt, and they stay true in
every other language you bind C from. If the suite migrates, the right
move is a `by-value.md` page in that repo and a Phase 3 that teaches
both — the ABI reality first, the feature that hides it second.

### 2. `ffi/layout` — declarative struct layouts

**Since 0.7.23.** `(ffi/layout [:struct ...])` compiles a data-only
descriptor into ABI metadata derived by Chez, with `layout-size`,
`layout-alignment`, `field-offset`, `read-field` and `write-field`.
This is what `raylib-jlt` hand-rolls today for `Camera2D`/`Camera3D`
in the tier-2 pointer trick. Limits worth knowing before anyone starts:
fixed-size scalar fields and nested structs only — **no arrays, unions,
bitfields, packing, or recursive descriptors.** `Camera3D` is scalars
and nested `Vector3`s, so it fits; check anything else against that list
first.

### 3. Scoped allocation helpers

**Since 0.7.23.** `with-alloc`, `with-out`, `with-layout`,
`with-c-string`, `with-c-string-array` release helper-owned native
allocations exactly once, on normal return *or* exception, and clean up
partially-constructed C string arrays. Pointers they create are valid
only inside the lexical body and must not escape it. This is the
manual-free-on-every-path pattern the pointer trick currently writes by
hand — a small, contained cleanup for `raylib-jlt`, invisible to
the course.

### 4. `clojure.repl/doc` and friends — ✅ done

**Since 0.7.21.** Core vars carry `:doc` and `:arglists`, and `doc`,
`find-doc`, `apropos` and `dir` are ported from Clojure 1.12.
`(doc when)` says Macro; `(doc if)` documents the special form.
`source` and `pst` are still unprovided — image-baked vars carry no
`:file` to point at. Phase 1's Polyglot Corner now says so, since the
course was otherwise sending learners into a dialect it never taught
them to explore.

### 5. An actual Jolt install path — ✅ done

Phase 1 said "if you have Jolt installed" and left the reader there,
which is the one gap that can stop someone doing Phase 3 Part 1 at all.
It now gives the three install routes (Homebrew, the install script,
and the **Nix flake** added in 0.7.20 for `x86_64-linux` /
`aarch64-darwin`) along with the prebuilt binary's real platform floor
— glibc 2.35+ or macOS 14+ arm64 — and says plainly that an Intel Mac
or musl host means building from source, which is more than an optional
aside is worth.

### 6. Two behaviour changes to keep an eye on, not act on

- **`nth`'s not-found arity now raises** on a receiver that has no `nth`
  at all (0.7.27). `(nth #{1 2} 0 :nf)` and sequential destructuring of
  a set or map used to answer nil and now throw — which is what Clojure
  does. `raylib-jlt` compiles clean on 0.7.27, but compilation
  would not catch a runtime destructure, so this is worth remembering
  if an example ever starts throwing where it did not.
- **Interruption became real** (0.7.25 / 0.7.26): a blocking wait is now
  interrupted rather than merely flagged, and `future-cancel` is
  `cancel(true)`. No current example runs a background thread, so this
  is inert here — it matters the day one does.

### 7. Performance, deliberately without a number

0.7.15 and 0.7.17 carry substantial performance work (native string
predicates, cached hashes, inline interop emission, records and
collections). [Phase 3's closing](docs/guide/phase-3-four-lisps/03-closing.md)
asks the reader which dialect they'd pick for raw performance, and the
honest answer is that nobody in this repo has benchmarked the three
against each other. If that question is ever answered here, it should
be answered with a measurement taken on one machine on one day, not
with an adjective borrowed from a changelog.
