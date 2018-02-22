(ns qdzo.duckduckgo.common.styles
  (:require [garden.core :refer [css]]
            [garden.units :as u]
            [garden.stylesheet :refer [at-media]]))

(def default-font-size 22)
(def default-header-font-size 23)
(def default-background-color "#352727")
(def default-color "white")
(def default-border-color "#6868f1")
(def default-btn-color "#8282d4")
(def default-btn-hover-color "#8292e2")
(def default-btn-active-color "#8292e2") ;; TODO change color
(def default-border-radius (u/px 3))

(def body-style
  [:body
   {;; :background "#d4d4d4"
    :background default-background-color ;; good with color white
    :color      default-color
    :height     "100%"
    :width      "100%"
    :margin     0
    :padding    0
    :font-size  default-font-size}

    [:strong {:font-size default-header-font-size}]

   [:#app
    {:width "100%"
     :height "100%"
     :display :flex
     :flex-direction :column
     :justify-content :center}]

   [:a {:color default-color}

    [:&:visited {:color default-btn-active-color}] ;; TODO change colors

    [:&:hover {:color default-btn-hover-color}]]

   [:ul {:padding-left (u/px 22)}

    [:li {:padding-bottom (u/px 5)}]]

   [:button
    {
     :padding-bottom (u/px 1)
     :color "white"
     :background-color default-btn-color
     :border (str "1px  solid " default-border-color)
     :border-radius default-border-radius
     :transition "200ms"}

    [:&:focus {:outline (u/px 2)}]

    [:&:hover {:cursor :pointer
               :background-color default-btn-hover-color}]

    [:&:active {:background-color "RED"}]]]) ;; TODO change color

(def input-panel-style
  [:#panel
   {:text-align "center"
    :display :flex
    ;; :margin-bottom (u/px 20)
    :flex-grow 1
    :flex-direction :column
    :justify-content :center
    :transition "1s"
    :align-items :center}

   [:&.minimized {:display "block"}]

   [:h1 {:margin-top  (u/px 7)
         :font-size   default-header-font-size
         :font-weight "lighter"}]

   ["input::-webkit-input-placeholder" {:color "white"}]

   [:input
    {:margin-right  (u/px 10)
     :height        (u/px 34)
     :width         (u/px 270)
     :font-size     default-font-size
     :background    :transparent
     :border-top    0
     :border-left   0
     :border-right  0
     :outline       0
     :color         "white"
     :border-bottom (str "1px solid " default-btn-color)
     :transition    "300ms"
     :text-align    "center"}

    [:&:focus :&:hover
     {:border-bottom (str "2px solid " default-btn-hover-color)
      :height (u/px 33)}]]

   [:#btn
    {:height     (u/px 35)
     :font-size  (- default-font-size 4)
     :transition "300ms"}

    [:&:focus {}]

    [:&:hover {}]]])

(def infobox-panel-style
  [:div.infobox-panel
   {:color default-color
    :border (str "1px solid " default-border-color)
    :border-radius default-border-radius
    :margin (u/px 7)
    :margin-bottom (u/px 0)
    :padding (u/px 7)}

   [:div.infobox-content
    {:padding-top (u/px 5)
     :padding-bottom (u/px 5)
     :margin-top (u/px 2)
     :margin-bottom (u/px 5)
     ;; :height "85%"
     ;; :width "90%"
     ;; :min-width "400px"
     ;; :position "absolute"
     :overflow-y "auto"}]

   [:table {:border-collapse "collapse"
            :margin-top (u/px 5)
            ;; FIXME: remove this 3 lines
            :width "100%"
            :border "none"
            :cellspacing 0
            :font-size default-font-size
            :cellpadding 0}]

   [:tr :td :th
    {:border "0px solid white"
     :border-bottom (str "1px solid" default-border-color)
     :padding (u/px 5)}]])

(def topics-panel-style
  [:div.topics-panel
   {:color default-color
    :border (str "1px solid " default-border-color)
    :border-radius default-border-radius
    :margin (u/px 7)
    :margin-bottom (u/px 0)
    ;; :position "relative"
    :padding (u/px 7)}

   [:div.topics-content
    {:padding-top (u/px 5)
     :padding-bottom (u/px 5)
     :margin-top (u/px 2)
     :margin-bottom (u/px 5)
     :overflow-y "auto"}]])

(def summary-panel-style
  [:.summary-panel
   {;; :width "40%"
    :border (str "1px solid " default-border-color)
    :border-radius default-border-radius
    :margin (u/px 7)
    :margin-bottom (u/px 0)
    :padding (u/px 7)}

   [:.header
    {:display "flex"
     :justify-content "space-between"}

    [:.heading
     {
      :font-size   (+ default-header-font-size 6)
      :quotes      "“"
      :margin-left (u/px 7)
      :margin-top  (u/px 5)
      :max-width   (u/px 150)
      :font-style  "italic"}]

    [:.logo
     {:height (u/px 64)
      :margin (u/px 5)}

     [:img {:height (u/px 64)}]]]

   [:.content
    {:padding-top (u/px 5)
     :padding-bottom (u/px 5)
     :margin-top (u/px 2)
     :margin-bottom (u/px 5)}]

   [:.entity {:margin-bottom (u/px 10)}]

   [:.definition {:margin-bottom (u/px 10)}]

   [:.info
    {:display "flex"
     :justify-content "center"}]]) ;; TODO change to right
;; TODO change color

(def result-panel-style
  [:.result
   {:margin-top (u/px 7)
    :overflow-y "auto"
    :flex-direction "column"
    :display "flex"
    :padding-right (u/vh 35)
    :padding-left (u/vh 35)}

   (at-media
     {:max-width (u/px 1480)}
     [:&.result
      {:padding-right (u/vh 30)
       :padding-left (u/vh 30)}])

   (at-media
     {:max-width (u/px 1350)}
     [:&.result
      {:padding-right (u/vh 20)
       :padding-left (u/vh 20)}])

   (at-media
     {:max-width (u/px 1150)}
     [:&.result
      {:padding-right (u/vh 15)
       :padding-left (u/vh 15)}])

   (at-media
     {:max-width (u/px 1050)}
     [:&.result
      {:padding-right (u/vh 10)
       :padding-left (u/vh 10)}])

   (at-media
     {:max-width (u/px 980)}
     [:&.result
      {:padding-right 0
       :padding-left 0}])

   summary-panel-style
   topics-panel-style
   infobox-panel-style

   [:.prop-name
    {:font-style "italic"
     :display "inline"}]])

(def style
  (css body-style
       input-panel-style
       result-panel-style))
