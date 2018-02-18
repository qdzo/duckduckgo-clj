(ns qdzo.duckduckgo.client.core
  (:require [reagent.core :as reagent :refer (atom)]
            [cljs.core.async :refer [<! >! put! chan timeout]]
            [cljs-http.client :as http]
            [qdzo.duckduckgo.common.styles :refer [style]]
            [qdzo.duckduckgo.client.views :as v])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; simple logger
(def log js/console.log)

(def api
  (let [url "http://localhost:3000/"]
    {:ask (str url "search")
     :dummy-ask (str url "assets/dummy.edn")}))

(defonce state (atom {:input ""}))

;; TODO: don't forget to remove this watch
;; (add-watch
;;  state
;;  :logger
;;  (fn [k iref os ns]
;;    (js/console.log
;;     (str "atom-logger: state changes - "
;;          (not= os ns)))))


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

(defn unsubscribe-onpopstate []
  (log "Unsubscribe onpopstate")
  (set! js/window.onpopstate nil))

  (defn push-state-to-history [app-state]
    (log app-state)
    (js/history.pushState
      (clj->js app-state) nil (str "/query/" (:input app-state))))

(comment
  (-> @state (dissoc :response))
  (-> @state  :response :RelatedTopics (nth 1))

  (-> @state  :response)

  (swap! state assoc :sort #{}))




;; (defonce dummy-data (-> @state  :response ))
;; dummy-data


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
                    (push-state-to-history new-state))})

;; TODO: remove next lines: use browser-history instead
;; TODO: add undo-redo
;; (aset js/window "undo"
;;       (fn [e]
;;         (when (> (count @history) 1)
;;           (reset! state (prev-history-item!)))))

(comment

  (-> @state :toggled))

;; (dispatch :ask "Clojurescript")

(defonce -action-chan
  (go
    (while true
      (let [[action payload] (<! $events-chan)
            f (get actions action)]
        (f payload)))))

(defn input-panel [input minimized]
  [:div#panel (if minimized {:class "minimized"})
   [:div [:h1 "DuckDuckGo Instant Answers"]
    [:input
     {:type "text"
      :placeholder "Enter query..."
      :value input
      :on-change
      #(dispatch :change-input
                 (.. % -target -value))
      :on-key-down
      #(when (= (.. % -keyCode) 13) ;; 13=ENTER key
         (dispatch :ask input))}]
    [:button#btn
     {:on-click #(dispatch :ask input)} "Ask me!"]]])

(defn result-panel
  [response]
  (let [infobox-meta (get-in response [:Infobox :meta])
        infobox-content (get-in response [:Infobox :content])
        related-topics (response :RelatedTopics)
        results-topics (response :Results)]
    [:div.result
     [v/result-summary response]
     [v/topics
      {:title "RESULT TOPICS"
       :topics results-topics}]
     [v/topics
      {:title "RELATED TOPICS"
       :topics related-topics}]
     [v/infobox
      {:title "CONTENT"
       :content infobox-content}]
     [v/infobox
      {:title    "META-CONTENT"
       :content  infobox-meta}]
     #_[:div {:background-color "red"
              :color "yellow"
              :height (u/px 10)
              :width (u/px 10)
              :margin-top "7px"
              :margin-right "7px"
              :on-click #(dispatch :reset "")}
             "XXX"]]))

(defn app []
  (let [{:keys [input response]} @state]
    (log "APP RENDER")
    [:div#app
     [:style style]
     [input-panel input response]
     (when response
       [result-panel response])]))

(comment
  (-> @state  :response) 
  @state 
  (v/result-summary (:response @state))
  (input-panel (:input @state) nil))


(defn init-app []
  (unsubscribe-onpopstate)
  (subscribe-onpopstate)
  (reagent/render [app]
                  (js/document.querySelector "#root")))

(init-app)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; REPL STAFF ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
         )
;; called by figwheel
#_(defn on-js-reload []
  (log "[----------on-js-reload called---------]")
  (unsubscribe-onpopstate)
  (subscribe-onpopstate))
