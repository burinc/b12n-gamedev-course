# Lisp Gamedev Course

Learn Lisp and game programming as one thing — not "game dev, incidentally
in Lisp." Every lesson builds a real, playable game; every game teaches a
Lisp idiom and a game-programming concept at the same time.

This course is built almost entirely from real, already-shipped game code
across seven sibling repos, spanning four Lisp dialects and three runtimes
(JVM, native-Chez-Scheme, native-C++/LLVM) plus the browser and mobile.
You'll build your own games on one primary dialect (JVM Clojure), while
every lesson shows you the same idea already running in the others.

## Who this is for

Complete beginners are covered from lesson 1 — nothing is assumed. Already
know a language, or already know some Lisp? Every "fundamentals" lesson has
a skip-this-if-you-already-know-it callout at the top, so you can move at
your own pace.

## Quick start

Prerequisites (verified in [`docs/guide/phase-0-first-contact/index.md`](docs/guide/phase-0-first-contact/index.md)
once that page exists):

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
| 0 — First Contact | Nothing yet — just watch the same tiny game already running, in a browser tab and natively | 🚧 in progress |
| 1 — Foundations | Clojure basics (skippable), the course's own game-loop engine, your first real programs | 🚧 in progress |
| 2 — Arcade Classics | Pong → Breakout → Snake → Space Invaders → Tetris → Flappy Bird | 🚧 in progress |
| 3 — Same Game, Four Lisps | Port one of your games across three FFI philosophies | 🚧 in progress |
| 4 — Systems & Architecture | Fixed timestep, ECS, particles, procedural generation, simple AI | 🚧 in progress |
| 5 — Realistic Capstones | Ship a mobile game, a web game, or go deep into multiplayer/3D | 🚧 in progress |
| Track B — Web Games | A standing remix playground of nine browser games, jump in anytime | 🚧 in progress |

Full course entry point once it exists: [`docs/guide/index.md`](docs/guide/index.md).

## Where this material comes from

Every lesson past Phase 2 curates real code from these repos rather than
re-inventing it:

- [`b12n-raylib-clj`](https://github.com/burinc/b12n-raylib-clj) — JVM Clojure, this course's spine dialect
- [`b12n-raylib-jlt`](https://github.com/burinc/b12n-raylib-jlt) — Jolt (native Clojure, Chez Scheme, no JVM)
- [`b12n-raylib-jnk`](https://github.com/burinc/b12n-raylib-jnk) — jank (native Clojure, C++/LLVM, no JVM)
- [`b12n-cljsapp`](https://github.com/burinc/b12n-cljsapp) — ClojureScript browser games, zero install
- [`b12n-herfi`](https://github.com/burinc/b12n-herfi) — 3D multiplayer, hand-rolled ECS
- [`b12n-ohuntley`](https://github.com/burinc/b12n-ohuntley) — 3D zombie-survival game, procedural generation + AI
- [`b12n-crystal-ball`](https://github.com/burinc/b12n-crystal-ball) / [`b12n-rogue-shooter`](https://github.com/burinc/b12n-rogue-shooter) — mobile games, ClojureDart + Flutter + Flame
