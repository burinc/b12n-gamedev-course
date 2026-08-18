# ROADMAP

Documents what this course covers, phase by phase, as of its first
complete version. Written at course completion, not maintained live
during the build — see `git log` for the actual build-out history if
that's what you're after.

## Phase B — Bootstrap
- [x] Repo skeleton, vendored raylib FFI layer, headless smoke-test
      helpers, the `game-loop` engine

## Phase 0 — First Contact
- [x] First Contact page + environment setup

## Phase 1 — Foundations
- [x] Clojure fundamentals (skippable)
- [x] The Game Loop lesson + worked bouncing-ball demo
- [x] Following Eyes exercise (mouse input)
- [x] Polyglot Corner #1 (bouncing ball, three Lisps)

## Phase 2 — Arcade Classics
- [x] Pong, Breakout, Snake, Space Invaders, Tetris, Flappy Bird —
      each with a guided exercise + lesson page + Polyglot Corner

## Phase 3 — Same Game, Three Lisps
- [x] FFI models lesson (color-by-value, pointer trick, rlgl, jank's
      value-boundary model)
- [x] Port-your-Pong guided cross-repo exercise
- [x] Closing comparative writeup

## Phase 4 — Systems & Architecture
- [x] Fixed timestep (extends the game-loop engine — real new code)
- [x] ECS (curated from `herfi`)
- [x] Particles, pooling & math for games
- [x] Procedural generation, simple AI & testing a game

## Phase 5 — Realistic Capstones + Track B
- [x] Mobile capstone (`rogue-shooter` → `crystal-ball`)
- [x] Web capstone (remix `cljsapp`, ship via GitHub Pages)
- [x] Go-deep capstone (`herfi`, `ohuntley`)
- [x] Track B — Web Games remix playground

## Closing
- [x] Where to go next
- [x] Doc wiring (this file, `docs/guide/index.md`, `README.md`)

## Known gaps / deliberately out of scope for this version

Per the design spec's explicit v1 scope cuts:
- No auto-grading beyond compile-check + headless-smoke-check.
- No instructor/cohort mode (schedules, slides, timing guides).
- `b12n-flame-3d` deferred — not yet documented in the wiki catalog the
  way `crystal-ball`/`rogue-shooter` are.
- Not yet mirrored to `b12n-wikis` or published via the shared b12n-docs
  site engine — both are real follow-ups, not silently dropped.
