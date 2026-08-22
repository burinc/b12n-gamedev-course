# Lisp Gamedev Course

Learn Lisp and game programming as one thing — not "game dev, incidentally
in Lisp." Every lesson builds a real, playable game; every game teaches a
Lisp idiom and a game-programming concept at the same time.

This course is built almost entirely from real, already-shipped game code
across eight sibling repos, spanning five Lisp dialects and three runtimes
(JVM, native-Chez-Scheme, native-C++/LLVM) plus the browser and mobile.
You'll build your own games on one primary dialect (JVM Clojure), while
every lesson shows you the same idea already running in the others.

## Who this is for

Complete beginners are covered from lesson 1 — nothing is assumed. Already
know a language, or already know some Lisp? Every "fundamentals" lesson has
a skip-this-if-you-already-know-it callout at the top, so you can move at
your own pace.

## Quick start

Prerequisites (verified in [`docs/guide/phase-0-first-contact/index.md`](docs/guide/phase-0-first-contact/index.md)):

- JDK 22+ (this course uses raylib's real C API over JDK's Panama FFI —
  older JDKs can't do this)
- [Clojure CLI](https://clojure.org/guides/install_clojure)
- [Babashka](https://github.com/babashka/babashka) (`bb`) for all course
  tooling

```bash
git clone git@github.com:burinc/b12n-gamedev-course.git
cd b12n-gamedev-course
bb tasks          # see everything you can run
```

## Course map

| Phase | What you build | Status |
|---|---|---|
| [0 — First Contact](docs/guide/phase-0-first-contact/index.md) | Nothing yet — just watch the same tiny game already running, in a browser tab and natively | ✅ done |
| [1 — Foundations](docs/guide/phase-1-foundations/01-clojure-fundamentals.md) | Clojure basics (skippable), the course's own game-loop engine, your first real programs | ✅ done |
| [2 — Arcade Classics](docs/guide/phase-2-arcade-classics/01-pong.md) | Pong → Breakout → Snake → Space Invaders → Tetris → Flappy Bird | ✅ done |
| [3 — Same Game, Three Lisps](docs/guide/phase-3-four-lisps/index.md) | Port one of your games across three FFI philosophies | ✅ done |
| [4 — Systems & Architecture](docs/guide/phase-4-systems-architecture/01-fixed-timestep.md) | Fixed timestep, ECS, particles, procedural generation, simple AI | ✅ done |
| [5 — Realistic Capstones](docs/guide/phase-5-capstones/01-mobile-capstone.md) | Ship a mobile game, a web game, or go deep into multiplayer/3D | ✅ done |
| [Track B — Web Games](docs/guide/track-b-web-games/index.md) | A standing remix playground of nine browser games, jump in anytime | ✅ done |

Full course entry point: [`docs/guide/index.md`](docs/guide/index.md).

## Where this material comes from

Every lesson past Phase 2 curates real code from these repos rather than
re-inventing it:

- [`b12n-raylib-clj`](https://github.com/burinc/b12n-raylib-clj) — JVM Clojure, this course's spine dialect
- [`b12n-raylib-jlt`](https://github.com/burinc/b12n-raylib-jlt) — Jolt (native Clojure, Chez Scheme, no JVM)
- [`b12n-raylib-jnk`](https://github.com/burinc/b12n-raylib-jnk) — jank (native Clojure, C++/LLVM, no JVM)
- [`b12n-cljsapp`](https://github.com/burinc/b12n-cljsapp) — ClojureScript browser games, zero install
- [`b12n-herfi`](https://github.com/burinc/b12n-herfi) — 3D multiplayer, hand-rolled ECS
- [`b12n-ohuntley`](https://github.com/burinc/b12n-ohuntley) — 3D zombie-survival game, procedural generation + AI
- [`b12n-rogue-shooter`](https://github.com/burinc/b12n-rogue-shooter) — mobile scrolling shooter, ClojureDart + Flutter + Flame
- [`b12n-crystal-ball`](https://github.com/burinc/b12n-crystal-ball) — mobile GLSL shader pipeline, ClojureDart + Flutter + Flame

> **Note on the sibling repos.** The three raylib suites above are public. The
> five below them (`b12n-cljsapp`, `b12n-herfi`, `b12n-ohuntley`,
> `b12n-rogue-shooter`, `b12n-crystal-ball`) are still private for now, so
> those links will 404. Phases 0 to 3 are complete and fully runnable without
> them. Phases 4 and 5 and Track B quote and explain that code inline, so the
> lessons still read end to end, but you won't be able to clone the source they
> curate from until those repos are opened up.

## License

This repository is dual licensed, because it's part course and part codebase.

- **Course material** in `docs/`, all the lesson pages and guides, is under
  [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). Share it,
  adapt it, teach from it. Just credit it and keep derivatives under the same
  license. See [`LICENSE-docs.md`](LICENSE-docs.md).
- **Code** in `src/`, `exercises/` and `test/` is under the Eclipse Public
  License 2.0, the same license Clojure itself uses. See [`LICENSE`](LICENSE).
- **The raylib binaries** in `libs/` are third party, from
  [raysan5/raylib](https://github.com/raysan5/raylib), redistributed under the
  zlib license. See [`libs/LICENSE-raylib.txt`](libs/LICENSE-raylib.txt).

Teaching from this course at a meetup, a classroom or a company onboarding
session needs no permission beyond attribution.
