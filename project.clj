(defproject ring-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-codec "1.1.0"] ;; uri-encoders
                 [ring/ring-defaults "0.3.1"] ;; middleware (query params)
                 ;; [ring/ring-devel "1.6.3"] ;; code hot-reload, i'l try to do as Rizhikov. without such thing. only fn-refenence(#')
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [garden "1.3.3"]
                 [clj-http "3.7.0"]
                 [clj-json "0.5.3"]
                 ;; front-end
                 [org.clojure/clojurescript "1.9.946"]
                 [reagent "0.7.0"]
                 [org.clojure/core.async "0.4.474"]
                 [cljs-http "0.1.44"]]
  

  ;; java-9 support
  ;; :jvm-opts ["--add-modules" "java.xml.bind"]

  ;; download libs from repl without restart
  :profiles {:dev {:dependencies [[com.cemerick/pomegranate "1.0.0"] 
                                  [org.clojure/test.check "0.9.0"]
                                  [binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.14"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   ;; to load user.clj for dev 
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}}



  :plugins [[lein-ring "0.12.1"] ;; code live-reload, uberjar,
            [cider/cider-nrepl "0.15.1"]
            [refactor-nrepl "2.3.1"]
            [lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  ;; for 'lein ring server' command and code hot-reload
  :ring {:handler ring-demo.core/app}

  :cljsbuild {:builds [{:id "dev"
                         :source-paths ["src"]
                         ;; inject figwheel client into build
                         :figwheel {:on-jsload "ring-demo.client.core/on-js-reload"}
                         :compiler {; :main ui.core
                                    :optimizations :none
                                    :asset-path "js/compiled/out"
                                    :output-to "resources/public/js/compiled/app.js"
                                    :output-dir "resources/public/js/compiled/out/"
                                    :pretty-print true
                                    :source-map-timestamp true
                                    ;; get this part from 'lein figwheel' template
                                    :preloads [devtools.preload]}}
                       {:id "min"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/app.js"
                                   :main ring-demo.client.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}


  :figwheel {:open-file-command "emacsclient"
             ;; :css-dirs ["resources/public/css"] ;; <- not needed currently
             }

  )
