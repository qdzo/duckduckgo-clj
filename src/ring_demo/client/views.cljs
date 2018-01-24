(ns ring-demo.client.views
  (:require [clojure.string :refer [blank?]]))

(def non-blank?
  "predicate for non-blank string"
  (complement blank?))

;; Infobox.content[{label, data_type(string, instance), value}]
;; Infobox.meta [{label, data_type(string, instance), value}]
(defn infobox
  "View for `Infobox.content` and `Infobox.meta` data"
  [content-or-meta]
  [:ul.infobox-panel
   (for [i content-or-meta]
     ^{:key i}
     [:li [:div (:label i)]
          [:div (:value i)]])])


;; Results.[{Icon{Height,Width,URL}, FirstURL, Text}]
;; RelatedTopics[{Text,Icon,FirstURL}]
(defn topics
  "View for `Results` and `RelatedTopics` data"
  [topics]
  [:ul.topics-panel
   (for [item topics]
     ^{:key item}
     [:li (:Text item) " - " (:FirstURL item)])])


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
  (let [{:keys [Abstract
                AbstractText
                AbstractSource
                AbstractURL
                Heading
                Image
                Entity]} response]
    [:div.summary
     [:div.header
      [:div.heading  Heading]
      (when (non-blank? Image)
        [:div.logo  [:img {:src Image
                           :alt Heading
                           }]])]
      (when (non-blank? Entity)
        [:div.entity "type: " Entity])
      (when (non-blank? AbstractText)
        [:div.definition "Definition: " AbstractText])
      (when (non-blank? AbstractSource)
        [:div.info "info: " [:a {:href AbstractURL} AbstractSource]])]))


