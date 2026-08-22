# Capstone: Ship a Web Game

> **Not public yet.** This page links to `b12n-cljsapp`, which is still
> private, so those links will 404 for now. They're being opened up as
> the course progresses, and this note goes away when they are. The
> three raylib suites the rest of the course is built on
> ([clj](https://github.com/burinc/b12n-raylib-clj),
> [jlt](https://github.com/burinc/b12n-raylib-jlt),
> [jnk](https://github.com/burinc/b12n-raylib-jnk)) are public today.

This is the fastest capstone to a shareable result — no native toolchain
at all, and `b12n-cljsapp` provides a reference implementation with nine
finished games you can fork and remix.

## Two ways in

**Remix an existing game.** `b12n-cljsapp`'s
[`src/cljs/net/b12n/cljsapp/games/`](https://github.com/burinc/b12n-cljsapp/tree/main/src/cljs/net/b12n/cljsapp/games)
has nine finished games — Memory, Breakout, Tetris, Snake, 2048, Connect
Four, Wordle, Galaga, Asteroids, plus a dashboard. Fork the repo, pick
one, and change something real: a difficulty curve, a new power-up, a
visual theme, an entirely different win condition.

**Build a tenth.** See [Track B](../track-b-web-games/index.md) for
the exact two touchpoints (a display-name entry and a page-dispatch
entry in `main.cljs`) a new game needs to show up in the dashboard.

## Ship it

Fork `b12n-cljsapp`, push to your own GitHub repo, then under
Settings → Pages, set Source to "Deploy from a branch" and pick `main` /
`public`. This is the simplest path to a public URL for your game — no
custom Actions workflow required. You'll have a shareable link the same
day.

(`b12n-cljsapp` itself uses a custom Actions-based deploy for faster
iteration; that's a more involved setup worth reading once the simple
Settings → Pages path feels limiting, not a prerequisite to shipping
your first fork.)

## Next

[Go deep](03-go-deep-capstone.md) — or if a shipped web game is your
finish line for this course, [Where to go next](../where-to-go-next.md).
