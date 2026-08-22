# Port Your Pong

## Part 1, read the Jolt Pong side by side with yours

Open your own
[`exercises/phase_2/pong.clj`](../../../exercises/phase_2/pong.clj)
next to
[`b12n-raylib-jlt`'s `pong.clj`](https://github.com/burinc/b12n-raylib-jlt/blob/main/src/net/b12n/raylib_jlt/pong.clj)
(clone that repo if you want to run it: `bb pong`, same `bb <name>`
pattern as the bouncing-ball demo from Phase 1's Polyglot Corner). You
don't need to understand every line of Jolt syntax, answer
these questions from reading, not from running:

1. Does Jolt's Pong represent the world as one immutable value the way
   yours does, or does it lean on mutable state? (Hint: look for `set!`
   or an atom vs. a value threaded through a loop.)
2. Where does Jolt's version touch a raylib struct that Phase 3's FFI
   lesson said needs one of the three ABI tricks, a `Color` argument to
   a draw call, or a `Vector2`? You won't see a C shim anywhere; that's
   the point.
3. What's the AI paddle doing differently, if anything, from your
   tracking-AI implementation?

## Part 2, there's no jank Pong. Build the first one.

`b12n-raylib-jnk` doesn't have a hand-built Pong, its 209 examples are
all direct ports of raylib's *own* official C examples, not original
games. You're not comparing against an answer key here; you're the first
person to port this particular game to jank.

Don't do this inside `b12n-gamedev-course`: jank ports live in
`b12n-raylib-jnk` itself, following that repo's own documented recipe:
[`docs/guide/porting-workflow.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/porting-workflow.md).
Before writing any jank code, also read
[`docs/guide/native-value-lifetimes.md`](https://github.com/burinc/b12n-raylib-jnk/blob/main/docs/guide/native-value-lifetimes.md)
(this phase's Lesson 1 already pointed you at it), Pong's ball position
needs to survive across frames, which is exactly the case that rule
constrains hardest. Start smaller than a full port: get a paddle and a
ball drawn and moving before wiring up scoring. Use `bb info` in that
repo to find existing shapes/input examples whose bindings you'll reuse
(you don't need to write any new FFI bindings, every primitive Pong
needs already exists somewhere in that repo's 209 ports).

This part of the lesson is intentionally open-ended, there's no
solution file to compare against. If you get a paddle and ball moving in
jank at all, you've done the hard part.

## Next

[Closing: same game, three philosophies](03-closing.md).
