# Phase 4, Lesson 1 — Fixed Timestep

## The Limitation We Flagged Back in Pong

Back in Phase 2's Pong lesson, `move-ball` did something that came with a warning attached:

> You'll notice that the ball's `dx` and `dy` velocities are **not** scaled by `dt` — the ball moves by raw pixel amounts each frame (`dx` pixels per frame, `dy` pixels per frame), not time-scaled. This is a deliberate simplification for this lesson: it only looks correct at a fixed target frame rate (here, 60 FPS). Real games scale velocity by `dt`, like the bouncing-ball demo in Phase 1, Lesson 2.
>
> This is a **limitation of the current approach** — frame-rate-dependent gameplay is fragile. Phase 4's "fixed timestep" lesson exists specifically to solve this problem properly.

This is that lesson. Here's the problem in full, and the tool that solves it.

## Two Ways to Handle Time, Both With Problems

**Variable dt** (what `run-game!` has done since Phase 1): every frame, `tick` gets called once with whatever `dt` the frame actually took. This is what the bouncing-ball demo does — scale movement by `dt` and speed is consistent across machines. It's a real fix for Pong's raw-pixel-per-frame problem... but it introduces a subtler one: **your simulation is no longer deterministic**. Run the same game twice with slightly different frame timings (a dropped frame here, a GC pause there) and you get slightly different physics each time, because every `tick` call integrates over a different, real-world-measured `dt`. For a two-paddle Pong that's invisible. For anything with more delicate physics — stacked objects, replay systems, networked games that need both players' simulations to agree — it's a real problem.

**Fixed dt, naively**: call `tick` with the same constant `dt` every frame, no matter how long the frame actually took. Now the simulation is deterministic — same inputs, same `dt`, same result, every time. But now you've reintroduced Pong's original bug at a different layer: if a frame takes longer to render than your fixed `dt` assumes (window resize, OS hiccup, alt-tab), the game just falls behind real time and never catches up. Worse, imagine you try to fix falling-behind by calling `tick` extra times to "catch up" whenever a frame runs long — each catch-up `tick` costs real CPU time, which makes the *next* frame even later, which demands *more* catch-up ticks next time. This runaway feedback loop has a name: the **spiral of death**. A single bad frame turns into a permanently unplayable game.

## The Accumulator Pattern

The classic fix (dating back to Glenn Fiedler's "Fix Your Timestep!") is to decouple *simulation* time from *render* time using an accumulator:

1. Each frame, add the real elapsed time to a running `accumulator`.
2. While the accumulator holds at least one whole `fixed-dt` chunk, call `tick` with exactly `fixed-dt` and subtract `fixed-dt` from the accumulator. This can run zero, one, or several times per frame, depending on how much real time has piled up.
3. Cap the number of catch-up steps per frame (`max-steps`) — if the accumulator has enough backlog to justify 1000 steps, only run a handful and let the rest wait for future frames. This trades "instantly caught up" for "never spirals," which is the right trade: a briefly-slow simulation recovers gracefully; a spiraling one never does.
4. Whatever fraction of `fixed-dt` didn't fit this frame carries over in the accumulator to next frame — no time is lost, it's just deferred.

This is exactly what `step-fixed` does:

```clojure
(defn step-fixed
  "The classic fixed-timestep accumulator: given the world, a fixed dt,
   how much unspent simulation time carried over from last frame
   (accumulator), how much real time elapsed this frame, and a cap on
   steps per frame (guards against a 'spiral of death' after a long
   stall — each step takes real time to compute, so catching up too much
   at once can make the next frame even slower), calls tick once per
   whole fixed-dt chunk the accumulated time can afford, always with the
   SAME dt value every call. Returns [world' accumulator']."
  [tick world accumulator elapsed fixed-dt max-steps]
  (loop [w world acc (+ accumulator elapsed) steps 0]
    (if (and (>= acc fixed-dt) (< steps max-steps))
      (recur (tick w fixed-dt) (- acc fixed-dt) (inc steps))
      [w acc])))
```

It's a pure function — no window, no timing calls, no side effects. Given a world, an accumulator, and how much time elapsed, it returns the new world and the new accumulator. `run-game!` is the only thing that calls it with real frame timings; you can call it yourself with made-up numbers to see exactly what it does, which is the point of the two worked examples below.

## Worked Example 1: Splitting 25ms Into 10ms Chunks

```clojure
(let [calls (atom [])
      tick  (fn [w dt] (swap! calls conj dt) (update w :n inc))
      [world' acc'] (game-loop/step-fixed tick {:n 0} 0.0 0.025 0.01 10)]
  (:n world')  ;=> 2      (25ms elapsed / 10ms fixed-dt = 2 whole steps)
  acc'         ;=> 0.005  (5ms of unspent time carries over)
  @calls)      ;=> [0.01 0.01]
```

Starting accumulator is `0.0`, `0.025` seconds (25ms) elapsed this frame, `fixed-dt` is `0.01` (10ms), and up to `10` steps are allowed. `step-fixed` adds the elapsed time to the accumulator (`0.0 + 0.025 = 0.025`), then peels off whole `0.01`-second chunks: first chunk brings the accumulator to `0.015`, second chunk brings it to `0.005`. At `0.005` there's not a full `0.01` left, so it stops. `tick` ran exactly twice, both times with `dt = 0.01` — never with the "wrong" 25ms, and never with an unpredictable third value. The leftover `0.005` isn't thrown away — it's still there in `acc'`, waiting to combine with whatever elapses next frame.

## Worked Example 2: The Cap Stops the Spiral

```clojure
(let [tick (fn [w _dt] (update w :n inc))
      [world' _acc'] (game-loop/step-fixed tick {:n 0} 0.0 10.0 0.01 5)]
  (:n world'))  ;=> 5   (not 1000, even though 10s / 10ms = 1000 whole steps fit)
```

This is the spiral-of-death guard in action. `10.0` seconds elapsed — maybe the app was suspended, or a breakpoint sat mid-frame — and with a `10ms` fixed step, that's a full 1000 catch-up steps' worth of backlog. Without a cap, `step-fixed` would try to run `tick` 1000 times in a single frame, which would itself take real time, delaying the *next* frame's `get-frame-time`, growing the backlog further, and so on — the spiral. With `max-steps` set to `5`, it runs exactly 5 steps and stops, leaving `9.95` seconds still sitting in the accumulator to be worked off gradually over subsequent frames. The simulation falls behind after a stall, same as it would with no fixed timestep at all — but it recovers, instead of never recovering.

## Using It: `run-game!`'s Two New Optional Keys

`run-game!` calls `step-fixed` for you when you opt in — everything from Phases 1 through 3 keeps working exactly as before, because both new keys are optional and default to variable-dt behavior:

```clojure
(game-loop/run-game!
 {:title    "Fixed-Step Demo"
  :width    640
  :height   480
  :fixed-dt (/ 1.0 60.0)   ; simulate at a fixed 60Hz, regardless of render fps
  :init     init
  :tick     tick            ; tick's dt argument is now always (/ 1.0 60.0)
  :draw     draw})
```

- **`:fixed-dt`** — when supplied, `tick` is called zero or more times per frame via `step-fixed`, always with this exact `dt` value, instead of once per frame with the frame's real (variable) `dt`. Omit it and nothing changes: `run-game!` calls `tick` once per frame with the real frame `dt`, exactly like every exercise in Phases 1-3 already does.
- **`:max-steps-per-frame`** — the spiral-of-death cap from Worked Example 2. Only matters when `:fixed-dt` is set. Defaults to `5`.

`draw` still gets called exactly once per frame either way — `step-fixed` only changes how many times (and with what `dt`) *simulation* runs; rendering stays at the render loop's own cadence.

## Go Try It

You now have the tool — go update your own Pong's `move-ball` (in `exercises/phase_2/pong.clj`) to use `:fixed-dt` instead of hardcoded per-frame pixel deltas. This is an **optional, ungraded extension** — Pong's exercise file belongs to Phase 2, not this lesson, so nothing here requires you to touch it. But if you want to see the fix-your-timestep pattern solve the exact problem it was foreshadowing, that's the place to try it.
