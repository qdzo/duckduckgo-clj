(ns ring-demo.client.core
  (:require [reagent.core :as reagent :refer (atom)]
            [cljs.core.async :refer [<! >! put! chan timeout]]
            [cljs-http.client :as http]
            [ring-demo.common.styles :refer [style]]
            [ring-demo.client.views :as v]
            [garden.units :as u])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def api
  (let [url "http://localhost:3000/"]
    {:ask (str url "search")
     :dummy-ask (str url "assets/dummy.edn")
     :ping (str url "ping")}))

(defonce state
  (atom {:input ""
         :online true
         :last-online nil
         :toggled {:content #{} :meta-content #{}}
         :sort #{}}))

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

  (-> @state  :response )


  (swap! state assoc :sort #{})

  )

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


(defn ping-server []
  (go (-> api :ping http/get <! :status (= 200))))

(defn apply-ping
  [{:keys [online] :as state} is-online]
  (if (= is-online online)
    state
    (if (= is-online false)
      (-> state
          (assoc :online is-online)
          (assoc :last-online
                 (let [time (js/Date.)
                       seconds (.getSeconds time)
                       minus-5-seconds (js/Date. (.setSeconds time (- seconds 5)))]
                   (.toLocaleString minus-5-seconds))))
      (->  state
           (assoc :online is-online)
           (assoc :last-online nil)))))

;; (go (println (<! (ping-server))))

;; (apply-ping @state false)
(def log js/console.log)

(defonce -ping-machine
  (go (while true
        (<! (timeout 5000))
        (dispatch :ping (<! (ping-server))))))

(defn toggle-in-set
  "Generic togging function.
   Adds or removes to-toggle item to(from) set"
  [toggled to-toggle]
  (if (toggled to-toggle)
    (disj toggled to-toggle)
    (conj toggled to-toggle)))

(defn sort-toggled-content
  "Sorts `coll`. Firts toggled goes."
  [coll toggled]
  (sort (comp not nil? toggled :label) coll))

(defn set-response-and-reset-toggles
  [state response]
  (-> state
      (assoc :response response)
      (assoc-in [:toggled :content] #{})
      (assoc-in [:toggled :meta-content] #{})
      (assoc :sort #{})))

(set-response-and-reset-toggles @state "")

@state

;; (reset! state {})


                                        ;(toggle-content #{:some :two} :two)
;; (-> @state (dissoc :response))

(def actions
  {:ask ask-duckduckgo
   :change-input #(swap! state assoc :input %)
   :log #(js/console.log %)
   :reset #(reset! state {})
   :toggle-content #(swap! state update-in
                           [:toggled (first %)] toggle-in-set (second %))
   ;; :toggle-content #(js/console.log %1 %2)
   :toggle-sort-content #(swap! state update :sort toggle-in-set %)
   :ping #(swap! state apply-ping %)
   :set-response #(swap! state set-response-and-reset-toggles %)})

(comment

  (-> @state :toggled)
  )
;; (dispatch :ask "Clojurescript")

(defonce -action-chan
  (go
    (while true
      (let [[action payload] (<! $events-chan)
            f (get actions action)]
        (f payload)))))

#_(go (while true
      (let [response (<! (http/get (:ping api)))
            t (<! (timeout 5000))
            is-online (= (:status response) 200)
            prev (:online @state)]
        (when (and  (not is-online)
                    (not= is-online prev))
          (swap! state assoc :last-online
                 (let [time (js/Date.)
                       seconds (.getSeconds time)
                       minus-5-seconds (js/Date. (.setSeconds time (- seconds 5)))]
                   (.toLocaleString minus-5-seconds)))
          (when (not= is-online prev)
            (swap! state assoc :online is-online))))))

;; -req-chan


(defn input-panel [input]
  [:div#panel
   [:div [:h1 "Твой личный поисковик."]
    [:input
     {:type "text"
      :placeholder "Введи запрос..."
      :value input
      :on-change
      #(dispatch :change-input
                 (.. % -target -value))
      :on-key-down
      #(when (= (.. % -keyCode) 13) ;; 13=ENTER key
         (dispatch :ask input))}]
    [:button#btn
     {:on-click #(dispatch :ask input)} "Найти!"]]])

(defn result-panel
  [response state]
  (let [infobox-meta (get-in response [:Infobox :meta])
        infobox-content (get-in response [:Infobox :content])
        related-topics (response :RelatedTopics)
        results-topics (response :Results)
        toggled-content (get-in state [:toggled :content])
        toggled-meta-content (get-in state [:toggled :meta-content])
        sort-content? (-> state :sort :content)
        sort-meta-content? (-> state :sort :meta-content)]
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
       :content (if sort-content?
                  (sort-toggled-content infobox-content toggled-content)
                  infobox-content)
       :toggled-items toggled-content
       :on-toggle-sort #(dispatch :toggle-sort-content :content)
       :on-toggle-item #(dispatch :toggle-content [:content %])}]
     [v/infobox
      {:title    "META-CONTENT"
       :content  (if sort-meta-content?
                   (sort-toggled-content infobox-meta toggled-meta-content)
                   infobox-meta)
       :toggled-items  toggled-meta-content
       :on-toggle-sort #(dispatch :toggle-sort-content :meta-content)
       :on-toggle-item #(dispatch :toggle-content [:meta-content %])}]
     #_[:div {:background-color "red"
              :color "yellow"
              :height (u/px 10)
              :width (u/px 10)
              :margin-top "7px"
              :margin-right "7px"
              :on-click #(dispatch :reset "")
              } "XXX"]]))

(defn online-indicator
  [is-online]
  [:div.online-indicator
   {:class (if is-online
             :online
             :offline)}
   (if is-online
     "on-line"
     "off-line")])

(defn app []
  (let [{:keys [online input response] :as s} @state]
    (log "APP RENDER")
    [:div#app
     [:style style]
     [online-indicator online]
     [input-panel input]
     (when response
       [result-panel response s])]))                    ;; TODO: move state to let

(comment
  (-> @state  )

  (v/result-summary (:result @state)))

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
