(ns ring-demo.client.core
  (:require [reagent.core :as reagent :refer (atom)]
            [cljs.core.async :refer [<! >! put! chan]]
            [cljs-http.client :as http]
            [ring-demo.common.styles :refer [style]]
            [ring-demo.client.views :as v])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(def api {:ask "http://localhost:3000/search"})

(def initial-state {:input ""})

(defonce state (atom initial-state))

(defonce $events-chan (chan))

(defn dispatch [action payload]
  (put! $events-chan [action payload]))

(comment
  (-> @state))


(defn ask-duckduckgo [q]
  (go
    (let [resp (<! (http/get (:ask api)
                             {:query-params {:q q}}))
          body (:body resp)
          parsed (.parse js/JSON body)
          clojurized (js->clj parsed :keywordize-keys true)]
      ;; (js/console.log (str "send ask: " q))
      (swap! state assoc :response clojurized))))

;; (ask-duckduckgo "CLojure")

(def actions
  {:ask #(ask-duckduckgo %)
   :change-input #(swap! state assoc :input %)
   :log #(js/console.log %)})

;; (dispatch :ask "Clojurescript")

(go
  (while true
    (let [[action payload] (<! $events-chan)
          f (action actions)]
      (f payload))))


;; (go (let [event (<! $events)])
;;     (event actions))

(:response @state)

(defn result-panel
  "response panel"
  [data]
  (let [{:keys [AbstractText]} data]
    [:div#result
     [:strong AbstractText]
     [:ul
      (for [i data]
        ^{:key i} [:li i])]]))

(defn app []
  (let [{:keys [input response]} @state]
    [:div
     [:style style]
     [:div#panel
      [:div [:h1 "Твой личный поисковик."]
       [:p "Что ты хочешь найти?"]
       [:input#input {:type "text"
                      :placeholder "Введи запрос..."
                      :value input
                      :on-change #(dispatch
                                   :change-input
                                   (.. % -target -value))
                      :on-key-down #(when (= (.. % -keyCode) 13) ;; 13=ENTER key
                                      (dispatch :ask input))}]
       [:button#btn {:on-click #(dispatch :ask input)} "Найти!"]]]
     (when response
       [:div.result
        [v/result-summary response]])]))


(comment
  (-> @state :response :Image)

  (v/result-summary (:result @state)))

(reagent/render [app]
  (js/document.querySelector "#app"))


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
