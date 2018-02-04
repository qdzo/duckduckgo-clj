(ns ring-demo.server.core
  (:require [ring.adapter.jetty :as jetty]
            ;; [ring.middleware.reload :refer [wrap-reload]] ;; try to live without this
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clj-json.core :as json]
            [ring-demo.server.views :refer :all]
            [ring-demo.server.duckduckgo :refer [ask sanitize-response]]))

(defn warning-msg
  "make wanding msg as json"
  [msg]
  (json/generate-string {:warning msg}))

;; (GET "/hello/:name{[a-z]{2}[0-9]+}"
;;   [name]
;;   (str "<h1>Hello " name "!</h1>"))
;; (GET ["/ping/:id", :id #"\w+"]
;;   [id]
;;   (str "<h1>Hello " id "!</h1>"))
;; (GET "/pong/:from"
;;   [from to & z :as r]
;;   (str "Pong " from " to " to ". Unbound params: " z ". Req-map: " r))
(defroutes app-routes
  (GET "/" [] (index))
  (GET "/index" [] (index))
  (GET "/search" [q] (if q
                       (json/generate-string (ask q))
                       (warning-msg "Query can't be an empty string.")))
  (GET "/ping" [] "pong")
  (route/resources "/assets/")
  (route/not-found "<p>Page not found</p>"))

(def app
  "main app handler"
  (wrap-defaults
   #'app-routes
   site-defaults))

;; jetty-run should return fn that stops server
(defn run-server []
  (future
    (jetty/run-jetty
     ;; (cors-wrap #'app-routes)
     #'app
     #_(wrap-reload #'app) ;; try to live without this
     {:port 3000})))

(comment

  (def maybe-stop (run-server))

  )


;; calls on namespace loading (tested in repl)

