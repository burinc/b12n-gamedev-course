(ns net.b12n.game-dev.engine.raylib.core
  (:require
   [coffi.ffi :as ffi]
   [clojure.string :as str])
  (:import [java.io File]))

;; OS Detection
(defn- get-os-name []
  (let [os (System/getProperty "os.name")]
    (cond
      (.contains os "Mac") :macos
      (.contains os "Linux") :linux
      (.contains os "Windows") :windows
      :else :unknown)))

;; Architecture Detection
(defn- get-arch []
  (let [arch (System/getProperty "os.arch")]
    (cond
      (contains? #{"amd64" "x86_64"} arch) :amd64
      (contains? #{"x86" "i386" "i686"} arch) :i386
      (= arch "aarch64") :aarch64
      :else :unknown)))

;; Get platform subdirectory name for libs/
(defn- get-platform-dir []
  (let [os (get-os-name)
        arch (get-arch)]
    (case os
      :macos "macos"
      :linux (case arch
               :amd64 "linux_amd64"
               :i386 "linux_i386"
               "linux_amd64")  ; default to amd64
      :windows (case arch
                 :amd64 "win64_msvc16"
                 :i386 "win32_msvc16"
                 "win64_msvc16")  ; default to win64
      nil)))

;; Get library filename for current OS
(defn- get-lib-name []
  (case (get-os-name)
    :macos "libraylib.5.5.0.dylib"
    :linux "libraylib.so.5.5.0"
    :windows "raylib.dll"
    nil))

;; Find bundled library path - check multiple locations
(defn- find-bundled-lib []
  (let [lib-name (get-lib-name)
        platform-dir (get-platform-dir)
        cwd (System/getProperty "user.dir")
        lib-path (System/getProperty "java.library.path")
        ;; Candidate directories to search
        search-dirs (concat
                     ;; java.library.path directories (for packaged apps)
                     (when lib-path (str/split lib-path (re-pattern File/pathSeparator)))
                     ;; Development path with platform subdirectory
                     [(str cwd "/libs/" platform-dir)])]
    (when lib-name
      (->> search-dirs
           (map #(str % "/" lib-name))
           (filter #(.exists (File. ^String %)))
           first))))

;; Load raylib - prefer bundled, fallback to system
(let [bundled-path (find-bundled-lib)]
  (if bundled-path
    (do
      (println "[raylib] Loading bundled library:" bundled-path)
      (ffi/load-library bundled-path))
    (do
      (println "[raylib] Loading system library")
      ;; The bundled 5.5 was not found, so we take whatever raylib the
      ;; system has. That is usually fine, but it is not free: raylib 6.0
      ;; changed DrawCircleGradient to take its centre as a by-value
      ;; Vector2 where 5.5 took two ints, WITHOUT changing the symbol
      ;; name. draw-circle-gradient! below is bound to the 5.5 shape, so
      ;; against a 6.0 system library it links cleanly and then draws in
      ;; the wrong place -- a silent wrong render, not a crash. Say so,
      ;; rather than let someone debug it from the pixels.
      (println "[raylib] WARNING: bundled raylib 5.5 not found; using the"
               "system library instead.")
      (println "[raylib]          If that is raylib 6.0+,"
               "draw-circle-gradient! will draw in the wrong place")
      (println "[raylib]          (6.0 changed its signature but kept the"
               "symbol name). Nothing else is affected.")
      (ffi/load-system-library "raylib"))))
