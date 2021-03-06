(ns qdzo.duckduckgo.common.styles
  (:require [garden.core :refer [css]]
            [garden.units :as u]
            [garden.color :as c]
            [garden.stylesheet :refer [at-media]]))

(def default-font-size 21)
(def default-header-font-size 22)
(def default-background-color "#352727")
(def default-color "#ffffff")
(def default-header-color "#af8088")
(def default-border-color "#6868f1")
(def default-btn-color "#8282d4")
(def default-btn-hover-color "#8292e2")
(def default-btn-active-color "#585dab") ;; TODO change color
(def default-visited-link-color "#6772ab")



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

   [:strong {:font-size default-header-font-size
             :color default-header-color
             :margin-bottom "20px"}]

   [:#app
    {:width "100%"
     :height "100%"
     :display :flex
     :flex-direction :column
     :justify-content :center}]

   [:a { :color default-color
        ;; :outline "1px dotted blue"
        ;; :color (hex->rgba default-color 1.0) 
        }

    [:&:visited {
                 :text-decoration "none"
                 :color default-btn-hover-color
                 ;; :color "rgba(255, 255, 255, 0.5)"
                 ;; :color (c/rgba 255 255 255 0.3)
                 ;; :color (hex->rgba default-color 0.6) ;; transparensy doesn't work in :visited selector
                 }]

    [:&:hover {:text-decoration-color default-btn-hover-color}]
    ]

   [:ul {:padding-left (u/px 22)
         :margin-bottom 0
         :color default-header-color}

    [:li {:padding-bottom (u/px 10)}]]

   [:button
    {
     :padding-bottom (u/px 1)
     :color "white"
     :background-color default-btn-color
     :border (str 0 default-border-color)
     :transition "200ms"}

    [:&:focus {:outline (u/px 2)}]

    [:&:hover {:cursor :pointer
               :background-color default-btn-hover-color
               }]

    [:&:active {:background-color default-btn-active-color}]]

   [:.panel
    {
     :background-color default-background-color
     ;; :box-shadow (str "0 1px 2px 0 " default-background-color)
     :color default-color
     :margin (u/px 10)
     :margin-top (u/px 0)
     :padding (u/px 7) }]])

(def warning-panel
  [:.warning-panel
   {:height "100%"
    :text-align "center"
    :display "flex"
    :justify-content "center"
    :padding-right (u/vh 35)
    :padding-left (u/vh 35)
    :align-items "center"}

   (at-media
    {:max-width (u/px 1480)}
    [:&.warning-panel
     {:padding-right (u/vh 30)
      :padding-left (u/vh 30)}])

   (at-media
    {:max-width (u/px 1350)}
    [:&.warning-panel
     {:padding-right (u/vh 20)
      :padding-left (u/vh 20)}])

   (at-media
    {:max-width (u/px 1150)}
    [:&.warning-panel
     {:padding-right (u/vh 15)
      :padding-left (u/vh 15)}])

   (at-media
    {:max-width (u/px 1050)}
    [:&.warning-panel
     {:padding-right (u/vh 10)
      :padding-left (u/vh 10)}])

   (at-media
    {:max-width (u/px 980)}
    [:&.warning-panel
     {:padding-right 0
      :padding-left 0}])

   [:strong
    {:font-size (u/px (+ default-header-font-size 10))
     :font-weight 100}]
   ])

(def input-panel-style
  [:#panel
   {:text-align "center"
    :display :flex
    :flex-grow 1
    :flex-direction :column
    :justify-content :center
    :transition "1s"
    :align-items :center}

   [:&.minimized {:display "block"}]

   [:h1 {:margin-top  (u/px 7)
         :font-size   (+ default-header-font-size 6.4)
         :font-weight 200}]

   ["input::-webkit-input-placeholder" {:color "white"}]

   [:input
    {:padding      (u/px 10)
     :height        (u/px 35)
     :margin-right (u/px 10)
     :width         (u/px 270)
     :font-size     (- default-font-size 4)
     :background    :transparent
     :border-top    0
     :border-left   0
     :border-right  0
     :outline       0
     :color         "white"
     :border-bottom (str "0.5px solid " default-btn-color)
     :transition    "200ms"
     :text-align    "center"}

    [:&:focus :&:hover
     {:border-bottom (str "1.5px solid " default-btn-hover-color)}]]

   [:#btn
    {:height     (u/px 36)
     :font-size  (- default-font-size 4)
     :transition "300ms"}

    [:&:focus {}]

    [:&:hover {}]]])

(def infobox-panel-style
  [:div.infobox-panel
   [:div.infobox-content
    {:padding-top (u/px 5)
     :padding-bottom (u/px 5)
     :margin-top (u/px 2)
     :margin-bottom (u/px 5)
     :overflow-y "auto"}]

   [:table {:border-collapse "collapse"
            :margin-top (u/px 5)
            :font-size default-font-size
            :cellpadding 0}]

   [:tr :td :th
    {;; :border "0px solid white"
     ;; :border-bottom (str "1px solid" default-border-color)
     :padding (u/px 7)}]
   [:td [:&:first-child {:text-align "right"
                         :color default-header-color}]]])

(def topics-panel-style
  [:div.topics-panel
   [:div.topics-content
    {:padding-top (u/px 5)
     :padding-bottom (u/px 5)
     :margin-top (u/px 2)
     :margin-bottom (u/px 5)
     :overflow-y "auto"}]])

(def summary-panel-style
  [:.summary-panel
   [:.header
    {:display "flex"
     :justify-content "space-between"}

    [:.heading
     {:font-size   (+ default-header-font-size 26)
      :margin-left (u/px 7)
      :margin-top  (u/px 5)
      :font-style  "italic"}]

    [:.logo
     {:height (u/px 96)
      :margin (u/px 5)}

     [:img {:height (u/px 96)}]]]

   [:.content
    {:padding-top (u/px 5)
     :padding-bottom (u/px 5)
     :margin-top (u/px 2)
     :margin-bottom (u/px 5)
     :line-height (u/px (+ default-font-size 8))}]

   [:.entity {:margin-bottom (u/px 10)}]

   [:.definition {:margin-bottom (u/px 10)}]

   [:.info
    {:display "flex"
     :justify-content "center"}]]) ;; TODO change to right

(def result-panel-style
  [:.result
   {:margin-top (u/px 7)
    :overflow-y "auto"
    :flex-direction "column"
    :display "flex"
    :flex-grow 4
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
       warning-panel
       input-panel-style
       result-panel-style))
