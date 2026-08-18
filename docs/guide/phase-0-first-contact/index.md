# Phase 0 — First Contact

## Watch it work first

Before installing anything: clone and run the example games locally to see what
you're building toward:

```bash
git clone git@github.com:burinc/b12n-cljsapp.git
cd b12n-cljsapp
npx josh public
```

This builds and opens nine finished browser games — Memory, Breakout, Tetris, Snake, 2048, Connect Four, Wordle, Galaga, Asteroids — plus a dashboard tying them together. Every one of them is Lisp (ClojureScript via [Scittle](https://github.com/babashka/scittle)) running directly in your browser tab — no compile step for the Clojure code itself, just plain `.cljs` files the browser interprets on load (`npx josh` above is only a tiny local dev server, needed to serve the files over `http://`, not to build anything). That's the whole course's promise in miniature: this is what Lisp-built games look like, and by the end of Phase 2 you'll have built several yourself.

## What you're about to set up

This course's hands-on exercises run on JVM Clojure, calling raylib (a
small, real C graphics library used by shipped commercial and indie games)
directly through JDK's Panama foreign-function interface — no wrapper
library, no codegen, the real C API. That's also why the setup below asks
for JDK 22+ specifically: older JDKs can't do this.

## Setup

1. **Install a JDK 22 or newer.** Check with `java -version`. If you don't
   have one, [Eclipse Temurin](https://adoptium.net/) is a good default.
2. **Install the [Clojure CLI](https://clojure.org/guides/install_clojure).**
   Check with `clojure -version`.
3. **Install [Babashka](https://github.com/babashka/babashka) (`bb`).**
   Check with `bb --version`. Whole-repo checks (`bb check`, `bb test`)
   are `bb` tasks; running one specific exercise or example uses
   `clojure -M:run -m <its-namespace>` directly — every lesson from here
   on gives you the exact command to run, so you won't need to remember
   the pattern yourself.
4. **Clone this repo and confirm it's wired up:**

   ```bash
   git clone git@github.com:burinc/b12n-gamedev-course.git
   cd b12n-gamedev-course
   bb tasks
   ```

   You should see a list of available tasks, no errors.

### macOS note

If a window you open from this course never appears, or the process
hangs, you're very likely missing the `-XstartOnFirstThread` JVM flag —
every windowed command this course teaches you to run already carries
it via `deps.edn`'s `:run`/`:test` aliases (`clojure -M:run -m ...`,
`bb test`), so this should only bite you if you've built your own
launcher that skips those aliases entirely. (`clojure -M:check`, used
by `bb check`, does NOT carry the flag — but that's fine, since it
only compiles, it never opens a window.) If you do hit this, add
`-J-XstartOnFirstThread` to whatever command you're running yourself.

## Next

Move on to [Phase 1 — Foundations](../phase-1-foundations/01-clojure-fundamentals.md).
