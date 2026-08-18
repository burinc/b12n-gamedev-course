# Particles, Object Pooling, and Easing/Math for Games

When a single space invader descends the screen, allocating a new bullet map and letting the old one become garbage is fine. When you're building a Vampire Survivors-style game with hundreds of projectiles firing every frame, that approach grinds to a halt: allocating thousands of objects per second and letting them become garbage — then waiting for the garbage collector to run — introduces stutters and frame hitches that ruin the feel.

This chapter covers two core patterns for performant game systems: **object pooling** for managing high-volume, short-lived game entities, and **easing functions** for making motion feel natural instead of mechanical.

## Object Pooling: The Pattern

### The Problem

In `simple_particles.clj` and `particle_system.clj`, each particle is represented as a map. In `simple_particles.clj`, when a particle expires (its `:alive` flag becomes false), it's filtered out of the particle vector — the garbage collector will eventually reclaim it. This works fine for a few dozen particles. But when hundreds of particles are live at once (capped at `MAX-PARTICLES` = 3000 total concurrent particles), emitting even a modest ~0.5 particles per frame (the default `:emission-rate -2` behavior) means sustained allocation and garbage-collection pressure that causes stutters and frame hitches.

The solution is **object pooling**: instead of creating new particles, you allocate a fixed pool of particle slots at startup and reuse them.

### The Data Shape

Looking at `simple_particles.clj`, each particle is a Clojure map:

```clojure
{:pos {:x 100.0 :y 200.0}
 :vel {:x 1.5 :y -2.0}
 :radius 5.0
 :color {:r 0 :g 121 :b 241 :a 255}
 :type :water
 :lifetime 0.0
 :alive true}
```

A pooled implementation would pre-allocate a fixed-size vector of these maps — say, 3000 slots — where each slot is either:
- `nil` (available for reuse)
- A live particle map with `:alive true`

When you emit a particle, you scan for the first `nil` slot and claim it. When a particle dies (`:alive` becomes false), you don't remove it from the vector; you just set it back to `nil`. The vector never grows, and the garbage collector has almost nothing to do.

The 3D particle system in `particle_system.clj` uses a similar data shape for 3D particles:

```clojure
{:position {:x 0.0 :y 0.0 :z 0.0}
 :velocity {:x 1.5 :y 5.0 :z -0.5}
 :lifetime 3.0
 :age 0.0
 :size 0.075
 :hue 45.0}
```

Again, a pooled version would pre-allocate a fixed vector of these maps and reuse slots, replacing `:alive` logic with a simple `:age >= :lifetime` check to determine whether a slot is live or dead.

### Why Pool?

- **Allocation pressure**: Creating thousands of maps per second stresses the memory allocator and garbage collector.
- **GC pauses**: Stop-the-world garbage collection pauses can cause visible frame hitches when the heap is under pressure.
- **Predictable memory**: A fixed pool uses a known amount of memory — no surprises when load spikes.
- **Cache locality**: Pooled arrays have better CPU cache behavior than scattered garbage-collected objects.

For games targeting 60 fps with tight frame budgets, pooling is not optional.

## Vectors and Easing: Making Motion Feel Natural

Linear interpolation is mechanically correct: if you move an object from A to B over 120 frames, updating its position by `(B - A) / 120` each frame gets you there on time. But it feels robotic — the motion has no character.

**Easing functions** solve this by transforming frame time into position in a non-linear way. A classic example: an object accelerates at the start of its motion, then decelerates at the end — just like a bouncing ball or a door slamming shut. No real physics needed, just the right curve.

### Easing in Practice

`easings_ball.clj` demonstrates the concept with three animated properties:

1. **Position with elastic easing** — the ball bounces into place, overshooting and settling.
2. **Radius with elastic easing** — the ball swells, bouncing as it grows.
3. **Alpha (opacity) with cubic easing** — the ball fades out smoothly.

Each animation spans a number of frames (e.g., 120 for position, 200 for radius). The easing function takes the current frame number and returns a value between the start and end — but not linearly. Here's `ease-elastic-out`:

```clojure
(defn ease-elastic-out
  "Elastic easing out"
  [t b c d]
  (let [t (/ t d)]
    (if (== t 0.0) b
        (if (== t 1.0) (+ b c)
            (let [p (* d 0.3)
                  s (/ p 4.0)]
              (+ (* c (Math/pow 2.0 (* -10.0 t))
                    (Math/sin (/ (* (- t s) 2.0 Math/PI) p)))
                 c b))))))
```

The parameters are:
- `t` — current elapsed time (frame number)
- `b` — beginning value (start position, e.g., -100)
- `c` — change (end - begin, e.g., 500)
- `d` — duration in frames (e.g., 120)

As `t` goes from 0 to 120, the function returns values from -100 to 400, but with a curve that overshoots slightly and bounces back — creating a snappy, lively feel instead of a mechanical ramp.

### A Grid of Easing Curves

`raylib-jlt`'s `easings.clj` shows 12 different easing functions in a 3×4 grid, each animating a ball ping-ponging left and right across a track. Each curve has a different character:

- **linear** — constant speed, mechanical
- **inQuad** / **outQuad** — slow-then-fast or fast-then-slow
- **inOutCubic** — slow-fast-slow, like a pendulum
- **inSine** / **outSine** — smooth, like a sine wave
- **inExpo** — explosive acceleration
- **outBounce** — bouncing, settling motion
- **inBack** — overshoot at the start
- **inOutQuart** — strong acceleration/deceleration

The lesson: the *shape* of the easing curve determines the *feel* of the motion. A cubic curve feels snappier than a sine curve. An elastic curve feels playful. Choosing the right curve is as much about game feel as it is about math.

### The Linear Alternative

Compare easing to raw linear interpolation:

```clojure
;; Linear (mechanical)
(let [progress (/ current-frame duration)
      value (+ start (* progress (- end start)))]
  value)

;; Eased (lively)
(ease-cubic-out current-frame start (- end start) duration)
```

The linear version is simpler and cheaper. Use it for background animations or objects the player never focuses on. Use easing for player-controlled characters, UI interactions, and anything the eye tracks.

---

**Next lesson:** [Procedural Generation and AI](04-procedural-generation-and-ai.md)
