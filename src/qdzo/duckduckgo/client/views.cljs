(ns qdzo.duckduckgo.client.views
  (:require [clojure.string :as str :refer [blank?]]
            [reagent.core :as r]))

(def ENTER 13)

(def non-blank?
  "predicate for non-blank string"
  (complement blank?))

(defn warning-panel [msg]
  [:div.panel.warning-panel
   [:strong msg]])

;; Infobox.content[{label, data_type(string, instance), value}]
;; Infobox.meta [{label, data_type(string, instance), value}]
(defn infobox
  "View for `Infobox.content` and `Infobox.meta` data"
  [{:keys [title content]}]
  [:div.panel.infobox-panel
   [:strong title]
   [:div.infobox-content
    [:table
     [:tbody
      (for [{:keys [label value]} content]
        ^{:key label}
        [:tr [:td label]
         [:td {} value]])]]]])

(comment
  sort-toggled (fn [xs toggled]
                 (sort (comp not nil? toggled :label) xs))
  (let [toggled #{"one" "two"}
        data [{:label "one"}
              {:label "three"}
              {:label "two"}]]
    (sort (comp not nil? toggled :label) data)))

(defn sub-topic? [topic]
  (contains? topic :Topics))

;; Results.[{Icon{Height,Width,URL}, FirstURL, Text}]
;; RelatedTopics[{Text,Icon,FirstURL}]
(defn topics
  "View for `Results` and `RelatedTopics` data"
  [{title :title tops :topics}]
  [:div.panel.topics-panel
   [:strong title]
   [:div.topics-content
    [:ul
     (for [item tops]
       ^{:key item}
       [:li (if (sub-topic? item)
              [topics {:title (:Name item)
                       :topics (:Topics item)}]
              [:a {:href (:FirstURL item)
                   :target "_blank"}
               (:Text item)])])]]])

#_(-> (apply str (take 90 (:Text item)))
    (str "..."))

;; Entity (string) (what kind of searched entity)
;; Abstract
;; AbstractText (same as Abstract)
;; AbstractSource (wikipedia)
;; AbstractURL
;; Heading
;; Image
;; Type "A"
(defn result-summary
  "Aggregate view of all small results (see above)"
  [response]
  (let [{:keys
         [;; Abstract ;; REVIEW: why i don't use this key: Abstract
          AbstractText
          AbstractSource
          AbstractURL
          Heading
          Image
          Entity]} response]
    (when (non-blank? Heading)
      [:div.panel.summary-panel
       [:div.header
        [:div.heading  Heading]
        (when (non-blank? Image)
          [:div.logo
           [:img {:src Image :alt Heading}]])]
       [:div.content
        (when (non-blank? Entity)
          [:div.entity
           [:strong.prop-name  "Type: "]
           Entity])
        (when (non-blank? AbstractText)
          [:div.definition
           [:strong.prop-name "Definition: "]
           AbstractText])
        (when (non-blank? AbstractSource)
          [:div.info
           [:strong.prop-name "info:"]
           [:a {:href AbstractURL :target "_blank"}
            AbstractSource]])]])))


(defn input-panel [{:keys [input minimized on-change on-submit]}]
  [:div#panel (if minimized {:class "minimized"})
   [:div [:h1 "DuckDuckGo Instant Answers"]
    [:input
     {:type "text"
      :placeholder "Enter query..."
      :value @input
      :on-change #(on-change (.. % -target -value))
      :on-key-down #(when (= (.. % -keyCode) ENTER)
                       (on-submit @input))}]
    [:button#btn
     {:on-click #(on-submit @input)} "Ask me!"]]])

(defn result-panel
  [response]
  (let [infobox-content (get-in response [:Infobox :content])
        related-topics (response :RelatedTopics)
        results-topics (response :Results)]
    [:div.result
     (when (non-blank? response)
       [result-summary response])
     (when (non-blank? infobox-content)
       [infobox
        {:title "CONTENT"
         :content infobox-content}])
     (when (not-empty results-topics)
       [topics
        {:title "RESULT TOPICS"
         :topics results-topics}])
     (when (not-empty  related-topics)
       [topics
        {:title "RELATED TOPICS"
         :topics related-topics}])]))
