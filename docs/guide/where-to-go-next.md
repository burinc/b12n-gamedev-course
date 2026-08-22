# Where to Go Next

This course's guided material ends at Phase 5. Here's where capable,
still-curious learners go from here, all outside the Lisp world this
course has lived in so far, deliberately, since the whole point of
Phase 3-5 was giving you enough vocabulary to read *any* game codebase,
not just Lisp ones.

## If you want to go deeper on architecture

**[*Game Programming Patterns*](https://gameprogrammingpatterns.com/)**
by Bob Nystrom, free online. You already met its vocabulary in Phase 4:
[Game Loop](phase-4-systems-architecture/01-fixed-timestep.md),
[Component (ECS)](phase-4-systems-architecture/02-ecs.md), and
[Object Pool](phase-4-systems-architecture/03-particles-pooling-math.md);
the full book covers 19 patterns across five categories, classic design
patterns revisited for games, plus sequencing, behavioral, decoupling,
and optimization concerns. Read it once you hit your own "this doesn't
scale" moment in a project, that's when each chapter actually lands.

## If you want to go deeper on "close to the metal"

**[Handmade Hero](https://handmadehero.org/)** (Casey Muratori), not a
structured course, deliberately: 600+ episodes of building a full game
engine from raw C, one hour at a time, explaining every decision live.
This course's raylib-first, thin-FFI, no-black-box philosophy is the
same spirit at a much smaller scale, Handmade Hero is what that
philosophy looks like taken all the way down.

## If you want a structured multi-week project

**[RoguelikeDev "Does the Complete Roguelike Tutorial"](https://www.reddit.com/r/roguelikedev/)**
- an annual, community-run 8-week event: grid movement → procedural
dungeon generation → field-of-view → turn-based AI → items/inventory →
save/load → difficulty scaling → polish. Phase 4 gave you a taste of
procedural generation and simple AI; this is the full structured version
of that ladder, with a whole community doing it alongside you.

## If ECS specifically hooked you

**[ecs-faq](https://github.com/SanderMertens/ecs-faq)** (SanderMertens)
- an FAQ-format deep dive that goes well past what Phase 4's `herfi`
lesson could cover: archetypes, sparse sets, query performance, real
production ECS design trade-offs.

## If multiplayer specifically hooked you

**[Awesome-Game-Networking](https://github.com/rumaniel/Awesome-Game-Networking)**
- a curated list covering the concepts Phase 5's `herfi` capstone only
gestured at: client-side prediction, entity interpolation, lag
compensation, reliable-UDP.

## If you just want to see (and build) more finished games

**[awesome-game-remakes](https://github.com/radek-sprta/awesome-game-remakes)**
and **[osgameclones.com](https://osgameclones.com/)**: curated lists of
open-source game remakes and clones, several with no paid assets
required, i.e. genuinely cloneable this weekend.

## One more thing

Everything in this course was built from real, shipped code in this
ecosystem's own repos, not textbook examples. If you build something
worth sharing, the games in
[Track B](track-b-web-games/index.md) started exactly the same way
someone else's Phase 2 exercise did. Ship it.
