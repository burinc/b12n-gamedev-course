# Capstone: Ship a Mobile Game

## Before you start: this setup is heavier than everything else in this course

Every exercise so far has run on the JDK + Babashka setup from
[Phase 0](../phase-0-first-contact/index.md). This capstone runs on
**ClojureDart + Flutter + the Flame game engine** — a materially bigger
toolchain (a Flutter SDK install, an iOS/Android/macOS build target, and
ClojureDart's own compiler). Budget real setup time before you expect to
see a window. Follow
[`b12n-rogue-shooter`'s own README](https://github.com/burinc/b12n-rogue-shooter)
for the actual install steps — they're specific to that toolchain and
would go stale fast if duplicated here.

## Step 1 — Rogue Shooter (start here, not Crystal Ball)

[`b12n-rogue-shooter`](https://github.com/burinc/b12n-rogue-shooter) is a
scrolling shooter with no shaders — a gentler on-ramp than Crystal Ball.
Read these, in order, once it's running on your machine:

1. [`sprite-animation-cljd.md`](https://github.com/burinc/b12n-wikis/blob/main/b12n-rogue-shooter/sprite-animation-cljd.md)
   — sprite sheets, `^:async onLoad`, and a real crash trap worth
   knowing before you hit it yourself: an untyped `#dart [...]` literal
   compiles fine and crashes at runtime against a typed Dart setter.
2. [`timer-component-cljd.md`](https://github.com/burinc/b12n-wikis/blob/main/b12n-rogue-shooter/timer-component-cljd.md)
   — spawn/fire timing, two shapes of `TimerComponent`.
3. [`batching-and-perf-cljd.md`](https://github.com/burinc/b12n-wikis/blob/main/b12n-rogue-shooter/batching-and-perf-cljd.md)
   — sprite batching and a live perf HUD; this is Phase 4's "object
   pooling" lesson, for real, at mobile-game scale.

Then extend it: add a new enemy type, a power-up, or a second weapon.
You don't need to build something original from scratch for this
capstone — modifying a real, already-shipped game is the point.

## Step 2 — Crystal Ball (once Rogue Shooter feels comfortable)

[`b12n-crystal-ball`](https://github.com/burinc/b12n-crystal-ball) adds
one new thing on top of everything Rogue Shooter taught: a real GLSL
fragment-shader pipeline (four shaders: firefly sparkle, fog, water
reflection, ball glow). Read its
[`index.md`](https://github.com/burinc/b12n-wikis/blob/main/b12n-crystal-ball/index.md)
for the full page list — shaders are graphics-programming territory this
course hasn't touched anywhere else, so go in expecting genuinely new
material, not a repeat of Rogue Shooter's patterns.

## Next

Once you've shipped something here (even a small extension), you're done
with this course's guided material. [Where to go next](../where-to-go-next.md)
has pointers for continuing past it.
