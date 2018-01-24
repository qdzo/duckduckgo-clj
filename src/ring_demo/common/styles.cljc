(ns ring-demo.common.styles
  (:require [garden.core :refer [css]]
            [garden.units :as u]))

(def body-style
  [:body
   {;; :background "#d4d4d4"
    :background "#352727" ;; good with color white
    :color "white"
    :height "100%"
    :width "100%"
    :margin 0
    :padding 0
    :display :flex
    :flex-direction :column
    :justify-content :center}
   ["input::-webkit-input-placeholder" {:color "white"}]
   [:#input
    {:margin-right (u/px 10)
     :height (u/px 35)
     :width (u/px 270)
     :font-size (u/px 15)
     :background :transparent
     :border 0
     :outline 0
     :color :white
     :border-bottom "1px solid #8282d4"}
    [:&:focus :&:hover
     {:border-bottom "2px solid #8292e2"
      :height (u/px 34)}]]
   [:#btn
    {:width (u/px 70)
     :height (u/px 35)
     :font-size (u/px 15)
     :padding-bottom (u/px 1)
     :color "white"
     :background-color "#8282d4"
     :border "1px  solid #6868f1"
     :border-radius (u/px 4)}
    [:&:focus {:outline (u/px 2)}]
    [:&:hover {:cursor :pointer
               :background-color "#8292e2"}]]])


(def panel-style
  [:#panel
   {:text-align "center"
    :display :flex
    :flex-direction :column
    :justify-content :center
    :align-items :center}
   [:.minimized {:flex-grow 1}]])


(def result-panel-style
  [:.result
   {:border "1px solid green"
    :margin-top (u/px 10)
    :overflow-y "auto"
    :min-height (u/px 200)
    :flex-grow 2}
   [:.summary-panel
    {:width "40%"
     :border "1px solid yellow"
     :margin (u/px 5)
     :padding (u/px 7)}
    [:.header
     {:display "flex"
      :justify-content "space-between"}
     [:.heading
      {:font-size (u/px 27)
       :quotes "“"
       :margin-left (u/px 7)
       :margin-top (u/px 5)
       :max-width (u/px 150)
       :font-style "italic"}]
     [:.logo
      {:height (u/px 64)
       :margin (u/px 5)}
      [:img {:height (u/px 64)}]]]
    [:.entity {:margin-bottom (u/px 10)}]
    [:.definition {:margin-bottom (u/px 10)}]
    [:.info
     {:display "flex"
      :justify-content "center"} ;; TODO change to right
     [:a {:color "pink"}                ;; TODO change color
      :&:visited {:color "green"}]]]])  ;; TODO change color

(def style
  (css body-style
       panel-style
       result-panel-style))
