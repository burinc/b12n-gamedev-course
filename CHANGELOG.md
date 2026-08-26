# Changelog

Notable changes to the Lisp Gamedev Course, newest first. The format
follows [babashka's changelog](https://github.com/babashka/babashka/blob/master/CHANGELOG.md):
one bullet per user-visible change, written as what a reader would
notice rather than what a commit did.

Sections are dated, not numbered. This course has no released artifact
to version — it is a repo and a site that move continuously — so "which
version am I on" is not a question anyone here has. "What changed, and
when" is.

The course reads at <https://lisp-gamedev.b12n.app>.

## Unreleased

- Phase 1's Polyglot Corner tells you how to actually install Jolt
  (Homebrew, the install script, or the Nix flake) and what the prebuilt
  binary needs, instead of saying "if you have Jolt installed" and
  leaving it there. It also notes that `doc`/`find-doc`/`apropos`/`dir`
  work in a Jolt REPL as of v0.7.21.
- ROADMAP carries a set of notes on what Jolt v0.7.27 makes newly
  possible for this course and for `b12n-raylib-jlt` — by-value struct
  bindings, `ffi/layout`, scoped allocation helpers — and, for the
  performance question Phase 3 asks, why there is deliberately no number
  attached to it yet.
- Phase 3's FFI lesson grew a **fourth** ABI tier — structs coming back
  *out* by value, which none of the first three tricks touch — and a
  section on what changed underneath it. Jolt 0.7.23 added
  `[:by-value [:struct ...]]`, which supersedes all four workarounds;
  `b12n-raylib-jlt`'s six new shader examples are built on it. The
  lesson still leads with the tricks, and now says why: the shipped
  bindings predate the feature, and an ABI fact outlives one FFI's
  feature list.
- Phase 3 and Phase 1 no longer say a native value "cannot cross a jank
  function boundary at all." It cannot cross *implicitly* — types with
  a conversion trait cross freely, and `cpp/new` + `cpp/box` is the
  documented way out for the ones that don't. The rule still shapes the
  code, and the lesson now says which way.
- `b12n-raylib-jnk`'s example count is 212 of raylib's 220, not 209.
- Phase 1's Polyglot Corner lists the two prerequisites
  `b12n-raylib-jlt` now gates on before `bb bouncing-ball` will run —
  Jolt v0.7.23+ and libraylib 6.0+ — and explains why this course
  keeps shipping raylib 5.5 alongside.
- Phase 3's closing points at the guide pages both sibling suites have
  added since this course was written.
- The engine says so when it falls back to a system raylib instead of
  the bundled 5.5. raylib 6.0 changed `DrawCircleGradient`'s signature
  without changing its symbol name, so that fallback could silently
  draw in the wrong place; now it warns first.
- The engine's namespaces moved from `gamedev-course.engine.*` to
  `net.b12n.game-dev.engine.*`, matching the `net.b12n.*` root the
  sibling raylib suites already use. Every lesson's `(:require ...)`
  block is updated to match. **The commands you type are unchanged** —
  the exercises deliberately keep their short `phase-1.*` / `phase-2.*`
  namespaces, so `clojure -M:run -m phase-2.pong` still runs Pong. If
  you have your own code requiring the engine, that is the one place
  you need to edit.

- [Demo gallery](docs/guide/demos.md): every game the course builds, as an
  animated GIF, each linking through to the lesson that builds it. The
  clips were already being published but no page referenced them, so a
  visitor to the site never saw one.
- Eight demo GIFs in [`docs/demos/`](docs/demos), recorded from this
  repo's own `exercises/` code on its own vendored engine — the six
  Phase 2 arcade games, plus Phase 1's bouncing ball and following eyes.
  Not borrowed from the sibling raylib suites: these are the programs you
  end up with if you work through the lessons.
- `bb record` regenerates those GIFs from
  [`scripts/demo_manifest.edn`](scripts/demo_manifest.edn), which also
  holds the per-game capture settings and input timelines. A re-run is a
  no-op unless a game's source actually changed. Maintainer-only: it
  needs a live display and the macOS screen-recording grant.
- Breakout's clip is deliberately short, and says so. Its paddle reads a
  *held* key, which the capture tool can only tap, so the recorded ball
  always gets past the paddle. Played by hand it rallies fine.
- README leads with the site link and shows the Phase 1 and Phase 2
  galleries.
- `bb docs-sync` now pushes the site repo. It only committed before, so
  the built site's history stayed on one machine; a missing remote is
  now a reported failure rather than a silent shrug.
- `bb docs-sync` says which kind of deploy failure it hit — unreachable
  AWS, missing or expired credentials, a 403, or a genuinely absent
  bucket — instead of blaming missing infrastructure for all four and
  recommending `tofu:apply`. On a restricted laptop `HTTPS_PROXY` is the
  usual cause, and it now detects that and prints the unset-and-retry
  line.
- `bb docs-sync` exits non-zero when the deploy does not happen. It
  printed its complaint and exited 0 before, so nothing chaining off it
  could tell a publish from a no-op.
- The repo's GitHub About had no description, no website and no topics.
  All three are filled in.

## 2026-08-22 — Public launch

Highlights:

- Six phases plus Track B, 24 lesson pages, from never having written a
  line of Lisp to a shipped capstone. Every lesson builds a real,
  playable game, and every game teaches a Lisp idiom at the same time.
- Phase 2 is six guided exercises with starter files rather than
  finished listings to read: Pong, Breakout, Snake, Space Invaders,
  Tetris, Flappy Bird.
- Phase 3 has you port one of your own games across three Lisps — Jolt
  on Chez Scheme, jank through C++/LLVM, JVM Clojure over Panama — and
  watch the boundary to C move under it. That lesson is the reason the
  three raylib suites exist.
- Complete beginners are covered from lesson 1; every fundamentals
  lesson carries a skip-this-if-you-already-know-it callout.
- Dual licensed, so teaching from it needs no permission beyond
  attribution: lessons CC BY-SA 4.0, code EPL-2.0, and the vendored
  raylib binaries under zlib.
- Published at <https://lisp-gamedev.b12n.app>.

Other changes:

- `gamedev-course.engine.game-loop/run-game!`, a big-bang-style loop
  modelled on Racket's `2htdp/universe`: describe a game as pure
  functions over an immutable world value and let the engine own the
  window, the frame timing and the input queue.
- `step-fixed`, the classic fixed-timestep accumulator, optional on
  `run-game!` for games that need a stable simulation step.
- A vendored raylib FFI layer, 14 namespaces over coffi and JDK 22+'s
  Panama Foreign Function & Memory API. No wrapper library and no C
  shim: the course calls the real `libraylib`.
- 15 exercise files, 8 worked solutions and 7 starters.
- `bb check` compile-checks the engine and every exercise headlessly;
  `bb test` runs the engine's pure unit tests, excluding the `^:windowed`
  ones that need a real display.
- Phases 4 and 5 curate real code from eight sibling repos — fixed
  timestep, ECS, particles and pooling, procedural generation, simple
  AI, then mobile, web and go-deep capstones.
- Track B, a standing remix playground of nine browser games to jump
  into any time after Phase 1.
- The five sibling repos the later phases read from are still private,
  and the lessons that depend on them say so at the top. Phases 0 to 3
  stand on the three public raylib suites alone.

The work before this point is in the git history: the course was written
2026-08-18 and 2026-08-19, and everything above shipped together when the
repo was opened up.
