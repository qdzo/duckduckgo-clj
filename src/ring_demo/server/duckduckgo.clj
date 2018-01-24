(ns ring-demo.server.duckduckgo
  (:require
   [clj-http.client :as client]
   [ring.util.codec :refer [url-encode]]
   [clj-json.core :as json]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]))

;;;;               Simplified response from duckduckgo for query - 'clojure'
;;
;; {
;;   "Infobox": {
;;     "content": [],
;;     "meta": []
;;   },
;;   "Results": [],
;;   "Entity": "programming language",
;;   "RelatedTopics": [],
;;   "Abstract": "Clojure is a dialect of the Lisp programming language. Clojure is a general-purpose programming language with an emphasis on functional programming. It runs on the Java virtual machine and the Common Language Runtime. Like other Lisps, Clojure treats code as data and has a macro system. The current development process is community-driven, overseen by Rich Hickey as its benevolent dictator for life.",
;;   "AbstractText": "Clojure is a dialect of the Lisp programming language. Clojure is a general-purpose programming language with an emphasis on functional programming. It runs on the Java virtual machine and the Common Language Runtime. Like other Lisps, Clojure treats code as data and has a macro system. The current development process is community-driven, overseen by Rich Hickey as its benevolent dictator for life.",
;;   "ImageWidth": 270,
;;   "AbstractSource": "Wikipedia",
;;   "Definition": "",
;;   "AbstractURL": "https://en.wikipedia.org/wiki/Clojure",
;;   "Heading": "Clojure",
;;   "ImageHeight": 270,
;;   "Answer": "",
;;   "Image": "https://duckduckgo.com/i/2f15aa63.png",
;;   "DefinitionURL": "",
;;   "DefinitionSource": "",
;;   "Type": "A",
;;   "Redirect": "",
;;   "AnswerType": "",
;;   "ImageIsLogo": 1,
;;   "meta": {}
;; }

;;;;               usefull paths in response
;;
;; Infobox.content[{label, data_type(string, instance), value}]
;; Infobox.meta [{label, data_type(string, instance), value}]
;; Results.[{Icon{Height,Width,URL}, FirstURL, Text}]
;; Entity (string) (what kind of searched entity)
;; RelatedTopics[{Text,Icon,FirstURL}]
;; Abstract
;; AbstractText (same as Abstract)
;; AbstractSource (wikipedia)
;; AbstractURL
;; Heading
;; Image
;; Type "A"


;;;;                SPECS for duckduckgo data


;;;   S for `Infobox` path

(s/def :content/label string?)
(s/def :content/value
  (s/or :plain string? :complex map?))

(s/def :content/content
  (s/keys :req-un [:content/label
                   :content/value]))

(s/def :Infobox/content
  (s/coll-of :content/content))

(s/def :Infobox/meta
  (s/coll-of :content/content))

(s/def :response/Infobox
  (s/or :plain string?
        :complex (s/keys :req-un [:Infobox/content
                                  :Infobox/meta])))

;;;   S for `Results` path


;; REVIEW: think about better name than string-or-number

(s/def :topic/FirstURL string?)
(s/def :topic/Text string?)

(s/def :topic/topic
  (s/keys :req-un [:topic/FirstURL
                   :topic/Text]))

(s/def :topics/coll
  (s/coll-of :topic/topic))

(s/def :response/Results :topics/coll)

;;;    S for other entries

(s/def :group/Name string?)
(s/def :group/Topics :topics/coll)

(s/def :RelatedTopics/group
  (s/keys :req-un [:group/Name :group/Topics]))

(s/def :response/RelatedTopics
  (s/coll-of (s/or :topic :topic/topic
                   :group :RelatedTopics/group)))

(s/def :response/Entity string?)
(s/def :response/Abstract string?)
(s/def :response/AbstractText string?)
(s/def :response/Heading string?)
(s/def :response/Image string?)
(s/def :response/Answer string?)
(s/def :response/AbstractURL string?)
(s/def :response/AbstractSource string?)

(s/def :duckduckgo/response
  (s/keys :req-un [:response/Infobox
                   :response/Results
                   :response/Answer
                   :response/Image
                   :response/Entity
                   :response/RelatedTopics
                   :response/AbstractURL
                   :response/Abstract
                   :response/AbstractText
                   :response/Heading]
          :opt-un [:response/AbstractSourse]))

(comment

  ;; some testings

  (every? (partial s/valid? :duckduckgo/response)
          (map ask ["scala" "clojure" "audi" "Tramp" "wikipedia"]))

  (s/explain :duckduckgo/response (ask "scala"))

  ;; (stest/check `ask) ;; don't do that (> 10 min) and there are no ending 
)

;;;;                   EXTRACT LOGIC

(s/fdef sanitize-response
        :args (s/cat :response :duckduckgo/response)
        :ret :duckduckgo/response)

(defn sanitize-response
  "Remove unnecessery information from duckduckgo response"
  [response]
  (let [conformed-response (s/conform :duckduckgo/response response)
        leave-keys [:Heading :Entity
                    :Image :Abstract
                    :AbstractURL :AbstractText
                    :AbstractSource :Answer
                    :Results :Infobox
                    :RelatedTopics]
        ;; not interesting what data-type is there, just take value
        sanitize-conformed-content-value #(update % :value second)
        sanitize-content-coll (partial mapv #(-> % (select-keys [:label :value])
                                                 sanitize-conformed-content-value))
        sanitize-complex-infobox #(-> % (update :content sanitize-content-coll)
                                      (update :meta sanitize-content-coll))
        sanitize-conformed-infobox (fn [[type data]]
                                     (case type
                                       :plain data
                                       :complex (sanitize-complex-infobox data)))
        sanitize-topic #(select-keys % [:FirstURL :Text])
        sanitize-topics (partial mapv sanitize-topic)
        sanitize-conformed-related-topic (fn [[type data]]
                                           (case type
                                             :topic (sanitize-topic data)
                                             :group (update data :Topics sanitize-topics)))
        sanitize-conformed-related-topics (partial mapv sanitize-conformed-related-topic)]
    (-> conformed-response
        (select-keys leave-keys)
        (update :Infobox sanitize-conformed-infobox)
        (update :Results sanitize-topics)
        (update :RelatedTopics sanitize-conformed-related-topics))))

(s/def :duckduckgo/query (s/and string? not-empty))

(s/fdef ask
        :args (s/cat
               :query :duckduckgo/query
               :opts (s/* (s/cat :key keyword? :val any?)))
        :ret (s/nilable :duckduckgo/response))

(def duckduckgo-url "http://api.duckduckgo.com/")

;; TODO rename to something better: maybe `request-duckduckgo`, `perform-request` etc...
(defn ask
  "Asks for given `query` in duckduckgo ask system
   and returns json-string response if success"
  [query & opts]
  (let [default-opts {:format "json" :no_html 1}
        params (merge (apply array-map opts)
                      default-opts
                      {:q query})
        keywordize true]
    (try
      (-> (client/get duckduckgo-url {:query-params params})
          :body
          (json/parse-string keywordize))
      (catch Exception e
        (println (str "Error while fetching data:" (.getMessage e)))
        nil))))

;; (defn search
;;   "Search duckduckgo for given query and return sanitized result,
;;    cleared from unnecessery information"
;;   [query]
;;   (if-let [response (ask query)]
;;     (sanitize-response response)))

;; (spit "clojure-query.txt" (search "scala"))

(comment

  (stest/check `sanitize-response)

  (stest/instrument `ask {:stub #{`ask}})

  (ask "10" :hello "a" :hey 10)

  (stest/unstrument `ask)

  (def cloj-definition (ask "ceylon"))

  cloj-definition

  (-> cloj-definition
      sanitize-response
      (clojure.pprint/pprint
       (clojure.java.io/writer "cloj-definition.txt")))



  )

