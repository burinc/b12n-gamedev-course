# Entity-Component-System

> **Not public yet.** This page links to `b12n-herfi`, which is still
> private, so those links will 404 for now. They're being opened up as
> the course progresses, and this note goes away when they are. The
> three raylib suites the rest of the course is built on
> ([clj](https://github.com/burinc/b12n-raylib-clj),
> [jlt](https://github.com/jlt-commons/raylib-jlt),
> [jnk](https://github.com/burinc/b12n-raylib-jnk)) are public today.

## The problem this solves

Every game you've built so far represents each kind of thing (the ball,
the paddle, an enemy, a pipe) as its own map shape, handled by its own
bespoke code in `tick`. That works fine at Space Invaders' scale (one
player, ~30 enemies, one bullet). It stops working once you have many
*different* kinds of entities that still share some behavior, health,
position, being drawable, because every new entity type means more
special-cased branches everywhere.

## The idea

**Entities are just IDs. Components are plain data attached to an ID.
Systems are functions that operate on every entity that has a particular
combination of components.** No inheritance hierarchy, no "an Enemy is-a
GameObject", just data, and functions over data. If that sounds like
"the way you were already thinking," that's the point: ECS is what
naturally falls out once you already think in data + pure functions,
which is exactly how every `tick` function in this course has been
written.

## A real, working example: `herfi`

[`b12n-herfi`](https://github.com/burinc/b12n-herfi) is a 3D multiplayer
game prototype (Clojure backend, ClojureScript + Three.js frontend) with
a real hand-rolled ECS under
[`src/cljs/herfi/scene/ecs.cljs`](https://github.com/burinc/b12n-herfi/blob/main/src/cljs/herfi/scene/ecs.cljs)
- forked from `infinitelives/px3d`, deliberately mimicking PlayCanvas's
own ECS design. Clone it and read through that file (it's compact).
Specifically look for:

1. How is an entity represented, what's the actual "ID"?
2. How is a component attached to an entity, what does the data shape
   look like?
3. How does a system decide which entities it cares about, does it
   filter by which components an entity has, or something else?
4. `herfi` is *multiplayer*, its
   [`scene/network.cljs`](https://github.com/burinc/b12n-herfi/blob/main/src/cljs/herfi/scene/network.cljs)
   syncs state over WebSocket. Skim it: does the ECS shape make that
   easier or harder than the ad hoc world-maps you've been building
   would?

## Next

[Particles, pooling & math for games](03-particles-pooling-math.md).
