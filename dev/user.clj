(ns user
  (:require [cemerick.pomegranate :refer [add-dependencies]]
            [figwheel-sidecar.repl-api :as f]))

(defn add-deps
  "`deps` is double-vector of [[dep-name dep-version]]"
  [deps]
  (add-dependencies
   :coordinates deps
   :repositories (merge cemerick.pomegranate.aether/maven-central
                        {"clojars" "https://clojars.org/repo"})))


(defn fig-start!
  "This starts the figwheel server and watch based auto-compiler"
  []
  (f/start-figwheel!))

(defn fig-stop!
  "Stop the figwheel server and watch based auto-compiler"
  []
  (f/stop-figwheel!))


(defn cljs-repl
  "Launch a ClojureScript REPL that is connected
  to your bulid and host environment"
  []
  (f/cljs-repl))

(comment

  (add-deps '[[clj-json "0.5.3"]])

  ;; (json/generate-string (ask "clojure"))

  ;; load deps if them added to project.clj

  ;; (require '[ring.middleware.defaults :refer :all])
  )
