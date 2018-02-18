(ns qdzo.duckduckgo.client.core
  (:require [reagent.core :as reagent :refer (atom)]
            [cljs.core.async :refer [<! >! put! chan timeout]]
            [cljs-http.client :as http]
            [qdzo.duckduckgo.common.styles :refer [style]]
            [qdzo.duckduckgo.client.views :as v])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; simple logger
(def log js/console.log)

;; TODO: add api for serving path {root}/query/query-string
(def api
  (let [url "http://localhost:3000/"]
    {:ask (str url "search")
     :dummy-ask (str url "assets/dummy.edn")}))

(defonce state (atom {:input "" :response nil}))

;; chanel for dispatching events
(defonce $events-chan (chan))

(defn dispatch [action payload]
  (put! $events-chan [action payload]))

(defn subscribe-onpopstate []
  (log "Subscribe onpopstate")
  (set! js/window.onpopstate
        (fn [e]
          (log "ON_POPSTATE called")
          (when-let [state (some-> (.-state e) (js->clj :keywordize-keys true))]
            (dispatch :reset-app-state state)))))

(defn push-state-to-history! [app-state]
  (js/history.pushState
   (clj->js app-state) nil (str "/query/" (:input app-state))))

(comment
  (-> @state (dissoc :response))
  (-> @state  :response :RelatedTopics (nth 1))

  (-> @state  :response)

  (swap! state assoc :sort #{}))

(defn ask-duckduckgo [q]
  (go
    (let [response
          (-> (:ask api)
              (http/get {:query-params {:q q}})
              <!
              :body
              (js/JSON.parse)
              (js->clj :keywordize-keys true))]
      (dispatch :set-response response))))

(def actions
  {:ask ask-duckduckgo
   :change-input #(swap! state assoc :input %)
   :reset-app-state #(reset! state %)
   :set-response #(let [new-state (swap! state assoc :response %)]
                    (push-state-to-history! new-state))})

;; (dispatch :ask "Clojurescript")

(defonce -action-chan
  (go
    (while true
      (let [[action payload] (<! $events-chan)
            f (get actions action)]
        (f payload)))))

(defn app []
  (let [{:keys [input response]} @state]
    (log "APP RENDER")
    [:div#app
     [:style style]
     [v/input-panel
      {:input input
       :minimized response ;; FIXME: set more accurate data here.
       :on-change #(dispatch :change-input %)
       :on-submit #(dispatch :ask %)}]
     (when response        ;; TODO: add view for empty results
       [v/result-panel response])]))

(comment
  (-> @state  :response))


(defn init-app []
  (subscribe-onpopstate)
  (reagent/render [app]
                  (js/document.querySelector "#root")))

(init-app)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; REPL STAFF ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (+ 1 2 3))


;; called by figwheel
#_(defn on-js-reload []
   (log "[----------on-js-reload called---------]")
   (subscribe-onpopstate))
