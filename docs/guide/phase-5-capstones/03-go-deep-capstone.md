# Capstone: Go Deep

Both of these are real, already-shipped codebases well past what this
course teaches directly. The goal here isn't to finish them — it's to
read production-scale game code and, if something grabs you, extend it.

## Multiplayer & 3D: `herfi`

[`b12n-herfi`](https://github.com/burinc/b12n-herfi) — the ECS you read
in [Phase 4](../phase-4-systems-architecture/02-ecs.md), plus real
WebSocket multiplayer (`scene/network.cljs`) and Three.js 3D rendering,
with a Clojure/Aleph backend. If Phase 3's FFI-philosophy question ("what
would you want for a much bigger game?") stuck with you, this is what
"bigger" actually looks like in this ecosystem.

## Single-player 3D, AI, and real test coverage: `ohuntley`

[`b12n-ohuntley`](https://github.com/burinc/b12n-ohuntley) — the
procedural maze generation and zombie-AI state machine from
[Phase 4](../phase-4-systems-architecture/04-procedural-generation-and-ai.md),
in full: 3D rendering via Three.js, four power-up types, particle
effects, procedural sound, and — worth studying on its own — 58 tests /
455 assertions across maze/pathfinding/AI/combat/game-state. If you want
to see what "test your game's pure logic thoroughly" looks like at real
scale, this is the reference.

## Next

[Where to go next](../where-to-go-next.md) — this course's own guided
material ends here.
