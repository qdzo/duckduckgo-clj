(ns qdzo.duckduckgo.server.views
  (:require [hiccup.core :refer [html]]
            [qdzo.duckduckgo.common.styles :refer [style]]))

(defn index []
  (html
   [:div#root
    [:style style]]
   [:script {:src "assets/js/compiled/out/goog/base.js"}]
   [:script {:src "assets/js/compiled/app.js"}]))

;; [:div#panel
;;  [:div [:h1 "Твой личный поисковик."]
;;   [:p "Что ты хочешь найти?"]
;;   [:input#input {:type "text"
;;                  :placeholder "Введи запрос..."}]
;;   [:button#btn  "Найти!"]]
;;  [:p#result]]
;; [:script {:src "assets/app.js"}]

;; <script type="text/javascript" src="./cljs-dev/goog/base.js"></script>
;; <script type="text/javascript" src="./js/dev.js"></script>
;; <script type="text/javascript">goog.require("cljs_begin.core")</script>
