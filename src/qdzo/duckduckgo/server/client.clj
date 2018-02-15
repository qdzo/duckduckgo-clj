(ns qdzo.duckduckgo.server.client
  (:require [clj-http.client :as client]))


(defn test-get [] (:status (client/get "http://ya.ru")))

(test-get)


(def root-url  "http://localhost:3000/")

(defn do-get
  ([url opts]
   (-> (str root-url url)
       (client/get opts)
       :body))
  ([url] (do-get url {})))


(comment
  ;; get index
  (do-get "index")

  ;; get app.js
  (do-get "assets/app.js")


  ;; get hello through path-param
  (do-get "hello/vi1000")

  ;; get hello through path-param
  (do-get "ping/Vitaly")

  ;; through query-params
  (do-get "pong/Vika" {:query-params {"to" "Vitaly"}}) ;; <-- not working query params

  ;; search
  (do-get "search" {:query-params {:q "Clojure"}})

  ;; get hello through path-param
  (do-get "ping")

  ;; get not-found
  (try
    (do-get "hh")
    (catch Exception e
      (println e)))

  )
