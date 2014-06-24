(ns wordless.synonyms
  (:require
   [clojure.string :as str]
   [wordless.redis :as r]
   [wordless.util :as u]
   [wordless.graph :as g]
   [inflections.core :as i]
   [print.foo :refer :all]))


(defn related-words [word]
  (or (r/related-words (str/trim word))
      (r/related-words (-> word i/singular i/capitalize))
      (r/related-words (-> word i/plural i/capitalize))
      (r/related-words (-> word i/singular .toLowerCase))
      (r/related-words (-> word i/plural .toLowerCase))))

(defn related-words [word]
  (map first (r/get-related-words word)))

(defn stop-words  []
  (-> "example/stop_words.txt" slurp clojure.string/split-lines set))

(defn word->nodes [word]
  (->> (related-words word)
       (#(conj % word))
       (take 15)))

(defn syngraph [word]
  (-> word word->nodes g/nodes-and-links))


(syngraph "return")


