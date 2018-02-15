(ns qdzo.duckduckgo.client.core
  (:require [reagent.core :as reagent :refer (atom)]
            [cljs.core.async :refer [<! >! put! chan timeout]]
            [cljs-http.client :as http]
            [qdzo.duckduckgo.common.styles :refer [style]]
            [qdzo.duckduckgo.client.views :as v]
            [garden.units :as u])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def api
  (let [url "http://localhost:3000/"]
    {:ask (str url "search")
     :dummy-ask (str url "assets/dummy.edn")}))

;; app undo-redo history
(defonce history
  (atom {:index -1
         :history []}))

(defn add-to-history [{:keys [history index] :as h} new-item]
  (let [history-size (count history)
        next-index (inc index)]
   (if (< next-index history-size)
    (assoc h
      :history (conj (subvec history 0 next-index) new-item)
      :index next-index)
    (assoc h
      :history (conj history new-item)
      :index next-index))))

(defn add-to-history! [new-item]
  (swap! history add-to-history new-item))

(defn history-current-item [{:keys [history index]}]
  (nth history index))

(defn prev-history-item! []
  (when (> (:index @history) -1)
    (-> (swap! history update :index dec)
        history-current-item)))

(defn next-history-item! []
  (let [history-size (count (:history @history))
        next-index (inc (:index @history))]
   (when (< next-index history-size)
    (-> (swap! history update :index inc)
        history-current-item))))

(defonce state
  (atom {:input ""}))

;; TODO: don't forget to remove this watch
;; (add-watch
;;  state
;;  :logger
;;  (fn [k iref os ns]
;;    (js/console.log
;;     (str "atom-logger: state changes - "
;;          (not= os ns)))))


(defonce $events-chan (chan))

(defn dispatch [action payload]
  (put! $events-chan [action payload]))

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

(def log js/console.log)


(def actions
  {:ask ask-duckduckgo
   :change-input #(swap! state assoc :input %)
   :log #(js/console.log %)
   :reset #(reset! state {})
   ;; :toggle-content #(js/console.log %1 %2)
   :set-response #(swap! state assoc :response %)})

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
  (let [{:keys [input response] :as s} @state]
    (log "APP RENDER")
    [:div#app
     [:style style]
     [input-panel input response]
     (when response
       [result-panel response])]))                    ;; TODO: move state to let

(comment
  ((-> @state  :response))

  (v/result-summary (:response @state))
  (input-panel (:input @state) nil))



(reagent/render [app]
  (js/document.querySelector "#root"))


;; (defn index []
;;   (html
;;    [:style style]
;;    [:div#panel
;;     [:div [:h1 "Твой личный поисковик."]
;;      [:p "Что ты хочешь найти?"]
;;      [:input#input {:type "text"
;;                     :placeholder "Введи запрос..."}]
;;      [:button#btn  "Найти!"]]
;;     [:p#result]
;;     [:div#app]
;;     ]
