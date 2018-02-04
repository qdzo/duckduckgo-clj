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
  (route/resources "/assets/")
  (route/not-found "<p>Page not found</p>"))

(def app
  "main app handler"
  (wrap-defaults
   #'app-routes
   site-defaults))

#_(wrap-reload #'app) ;; try to live without this

;; jetty-run should return fn that stops server
(defn run-server []
  (future
    (jetty/run-jetty #'app {:port 3000})))

(comment

  (def server (run-server))

  )


;; calls on namespace loading (tested in repl)

