(ns qdzo.duckduckgo.server.duckduckgo
  (:require
   [clj-http.client :as client]
   [ring.util.codec :refer [url-encode]]
   [clj-json.core :as json]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.string :as str]))

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

(s/def :Infobox/meta :Infobox/content)

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

(defn duckduckgo-response-empty? [response]
  (let [select-values (comp vals select-keys)
        topics-keys [:Results :RelatedTopics]
        topics (select-values response topics-keys)
        text-keys [:Heading :AbstractText :AbstractURL :AbstractSource :Abstract :Type]
        texts (select-values response text-keys)]
    (and (every? empty? topics)
         (every? str/blank? texts))))

(s/def :duckduckgo/not-empty-response
  (s/and :duckduckgo/response
         (complement duckduckgo-response-empty?)))


(comment

  ;; some testings

  (every? (partial s/valid? :duckduckgo/response)
          (map ask ["scala" "clojure" "audi" "Tramp" "wikipedia"]))

  (s/explain :duckduckgo/response (ask "scala"))

  ;; (stest/check `ask) ;; don't do that (> 10 min) and there are no ending 
)

;;;;                   EXTRACT LOGIC


(def duckduckgo-url "http://api.duckduckgo.com/")

(defn request-duckduckgo-api [params]
  "Send http-request to duckduckgo api server and parse response body"
  (let [default-params {:format "json" :no_html 1}]
    (-> (client/get duckduckgo-url {:query-params (merge params default-params)})
        (:body)
        (json/parse-string true)))) ;; keywordize = true


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
        content-with-instanceof-label? #(= (:label %) "Instance of") ;; <- this 'content' has strange values
        remove-content-with-instanceof-label (partial filter (complement content-with-instanceof-label?))
        ;; not interesting what data-type is there, just take value
        sanitize-conformed-content-value #(update % :value second)
        sanitize-content-coll (partial mapv #(-> % (select-keys [:label :value])
                                           sanitize-conformed-content-value))
        sanitize-complex-infobox #(-> %
                                      (update :content (comp sanitize-content-coll
                                                          remove-content-with-instanceof-label))
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

(s/def :ask/query
  (s/with-gen (s/and string? not-empty)
    #(s/gen #{"clojure" "10" "audi" "BMW" "Scala"})))

(s/def :ask-response/status #{:found :not-found :error})

(s/def :ask-response/value
  (s/or :found :duckduckgo/not-empty-response
        :not-found nil?
        :error string?))

(s/def :ask/response
  (s/and (s/keys :req-un [:ask-response/status :ask-response/value])
         #(= (:status %) (first (:value %))))) ;; use `first`, cos conformed :value, like [:branch val]

(s/fdef ask
        :args (s/cat
               :query :ask/query
               :opts (s/* (s/cat :key keyword? :val any?)))
        :ret :ask/response)


;; TODO rename to something better: maybe `request-duckduckgo`, `perform-request` etc...
(defn ask
  "Asks for given `query` in duckduckgo ask system
   and returns json-string response if success"
  [query & opts]
  (try
    (let [params (merge (apply array-map opts) {:q query})
          response (request-duckduckgo-api params)
          [status value] (if (duckduckgo-response-empty? response)
                           [:not-found nil]
                           [:found (sanitize-response response)])]
      {:status status :value value})
    (catch Exception e
      (println (str "Error while fetching data:" (.getMessage e)))
      {:status :error :value (.getMessage e)})))


(comment

  (stest/check `sanitize-response)

  (stest/instrument `ask {:stub #{`ask}})

  (-> (ask "clojure") :value  :Infobox :content)

  (s/explain :ask/valid-response (ask "clojure" ))

  (stest/unstrument `ask)

  (def cloj-definition (ask "ceylon"))

  cloj-definition

  (-> cloj-definition
      sanitize-response
      (clojure.pprint/pprint
       (clojure.java.io/writer "cloj-definition.txt")))



  )

