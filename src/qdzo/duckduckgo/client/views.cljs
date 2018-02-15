(ns qdzo.duckduckgo.client.views
  (:require [clojure.string :refer [blank?]]
            [reagent.core :refer [atom]]))

(def non-blank?
  "predicate for non-blank string"
  (complement blank?))

;; Infobox.content[{label, data_type(string, instance), value}]
;; Infobox.meta [{label, data_type(string, instance), value}]
(defn infobox
  "View for `Infobox.content` and `Infobox.meta` data"
  [{:keys [title content]}]
  [:div.infobox-panel
   [:strong title]
   [:div.infobox-content
    [:table
     [:tbody
      (for [{:keys [label value]} content]
        ^{:key label}
        [:tr
         [:td label]
         [:td value]])]]]])

(comment
  sort-toggled (fn [xs toggled]
                 (sort (comp not nil? toggled :label) xs))
  (let [toggled #{"one" "two"}
        data [{:label "one"}
              {:label "three"}
              {:label "two"}]]
    (sort (comp not nil? toggled :label) data))

  )

;; Results.[{Icon{Height,Width,URL}, FirstURL, Text}]
;; RelatedTopics[{Text,Icon,FirstURL}]
(defn topics
  "View for `Results` and `RelatedTopics` data"
  [{:keys [title topics]}]
  [:div.topics-panel
   [:strong title]
   [:div.topics-content
    [:ul
     (for [item topics]
       ^{:key item}
       [:li [:a {:href (:FirstURL item)}
             (:Text item)]])]]])


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
    [:div.summary-panel
     [:div.header
      [:div.heading  Heading]
      (when (non-blank? Image)
        [:div.logo
         [:img
          {:src Image
           :alt Heading}]])]
     [:div.content
      (when (non-blank? Entity)
        [:div.entity
         [:div.prop-name  "type: "]
         Entity])
      (when (non-blank? AbstractText)
        [:div.definition
         [:div.prop-name "Definition: "]
         AbstractText])
      (when (non-blank? AbstractSource)
        [:div.info "info:" [:a {:href AbstractURL} AbstractSource]])]]))
