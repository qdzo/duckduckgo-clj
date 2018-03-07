(ns qdzo.duckduckgo.client.core
  (:require
    [reagent.core :as reagent :refer (atom)]
    [cljs.core.async :refer [<! >! put! chan timeout]]
    [cljs-http.client :as http]
    [qdzo.duckduckgo.common.styles :refer [style]]
    [qdzo.duckduckgo.client.views :as v]
    [clojure.string :as str])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; simple logger
(def log js/console.log)

;; TODO: add api for serving path {root}/query/query-string
(def api
  (let [url js/window.location.origin]
    {:ask (str url "/search")
     :dummy-ask (str url "/assets/dummy.edn")}))

(defn subscribe-on-history-pop-state [cb]
  (log "Subscribe onpopstate")
  (set! js/window.onpopstate
        (fn [e]
          (some-> (.-state e)
                  (js->clj :keywordize-keys true)
                  (cb)))))

(defn make-query-url [query]
  (str "/query?q=" query))

(defn push-state-to-history! [app-state]
  (js/history.pushState (clj->js app-state)
                        nil
                        (make-query-url (:input app-state))))

;; TODO: add client-routing, to block reloading app after url changes (by hand)
(defn location-query []
  "Gets `search` from `js/location`
  if there are path like [root]/query?q=[search]"
  (when (and (= js/location.pathname "/query")
             (str/starts-with? js/location.search "?q=")
             (> (count js/location.search) 3))
    (-> (subs js/location.search 3)
        (js/decodeURIComponent))))

(defn handle-location-query [cb]
  "If location query exists, grab it's content and send query."
  (when-let [query (location-query)]
    (log "Location query catch up")
    (cb query)))

(defn ask-duckduckgo [query cb]
  (go (-> (:ask api)
          (http/get {:query-params {:q query}})
          (<!)
          (:body)
          (js/JSON.parse)
          (js->clj :keywordize-keys true)
          (cb))))

(defn make-dispatch-fn [channel]
  (fn [action payload]
    (put! channel [action payload])))

(defn action-dispatcher [channel actions]
  "Reads `channel` for `[action payload]` pairs.
   Searches for action in `actions` map and evaluates them"
  (log "action-dispatcher starting")
  (go (while true
        (let [[action payload] (<! channel)
              f (get actions action)]
          (f payload)))))

(defn app [state dispatch]
  (let [{:keys [status value]} (:response @state)
        input-cursor (reagent/cursor state [:input])]
    (log "APP RENDER")
    [:div#app
     [:style style]
     [v/input-panel
      {:input input-cursor
       :minimized (not (nil? status))
       :on-change #(dispatch :set-input %)
       :on-submit #(dispatch :ask %)}]
     (case status
       "found" [v/result-panel value]
       "not-found" [v/warning-panel "Duckduckgo doesn't know answer for your question :("]
       "error" [v/warning-panel (str "Error while try fetch data: " value)]
       nil)]))

(defonce state (atom {:input "" :response nil}))

(defn actions [state dispatch]
  {:ask #(ask-duckduckgo % (partial dispatch :set-response))
   :set-input #(swap! state assoc :input %)
   :set-app-state #(reset! state %)
   :set-response #(let [new-state (swap! state assoc :response %)]
                    (push-state-to-history! new-state))})

(defn on-mount [app dispatch]
  (with-meta
    app
    {:component-did-mount
     (fn [_]
       (subscribe-on-history-pop-state (partial dispatch :set-app-state))
       (handle-location-query #(do (dispatch :set-input %)
                                   (dispatch :ask %))))}))


(defn render-app []
  (let [ch (chan)
        dispatch (make-dispatch-fn ch)
        actions (actions state dispatch)
        app (on-mount app dispatch)]
    (action-dispatcher ch actions)
    (reagent/render [app state dispatch]
                    (js/document.querySelector "#root"))))

(render-app)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; REPL STAFF ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (-> @state (dissoc :response))
  (-> @state  :response :RelatedTopics )

  (-> @state  )

  (swap! state assoc :sort #{}))

;; called by figwheel
#_(defn on-js-reload []
   (log "[----------on-js-reload called---------]")
   (subscribe-on-history-pop-state))
