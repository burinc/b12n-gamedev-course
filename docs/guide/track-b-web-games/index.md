# Track B — Web Games

A standing remix playground. Jump in anytime after
[Phase 1](../phase-1-foundations/01-clojure-fundamentals.md) — it's
independent of the Phase 2-5 spine, needs no compile step (Scittle runs
the `.cljs` straight in the browser — `npx josh` below is just a tiny
local dev server, not a build), and if you want a fun detour between
phases, this is it.

## What's here

[`b12n-cljsapp`](https://github.com/burinc/b12n-cljsapp)'s nine finished
games, live at <https://burinc.github.io/b12n-cljsapp/> — Memory,
Breakout, Tetris, Snake, 2048, Connect Four, Wordle, Galaga, Asteroids —
plus a dashboard tying them together. Every one is ClojureScript.

## Run it locally

```bash
git clone git@github.com:burinc/b12n-cljsapp.git
cd b12n-cljsapp
npx josh public   # http://localhost:8000, live-reloads on save
```

## Add a tenth game

Every game lives at
`src/cljs/net/b12n/cljsapp/games/<name>.cljs` and exposes a `page`
function taking the app's state atom. Two places register it into the
dashboard, both in `src/cljs/net/b12n/cljsapp/main.cljs`:

1. A display-name entry (e.g. `:memory-game "🧠 Memory Game"`).
2. A dispatch entry mapping that same keyword to your game's `page`
   call (e.g. `:memory-game [memory/page state]`).

Read `games/memory_game.cljs` first — it's one of the simpler ones — to
see the actual `page` function shape before writing your own.

## Next

Back to the main spine: [Phase 2 — Arcade Classics](../phase-2-arcade-classics/01-pong.md)
if you haven't started it yet, or [Phase 5](../phase-5-capstones/01-mobile-capstone.md)
if you have.
