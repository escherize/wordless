(ns wordless.synonyms
  (:require
   [clojure.string :as str]
   [wordless.redis :as r]
   [wordless.util :as u]
   [wordless.graph :as g]
   [inflections.core :as i]
   [print.foo :refer :all]))


(defn related-words [word]
  (map first (r/get-related-words word)))

(defn stop-words  []
  (-> "example/stop_words.txt" slurp clojure.string/split-lines set))

(defn word->nodes [word]
  (->> (related-words word)
       (#(conj % word))
       (take 20)))

(defn syngraph [word]
  (-> word word->nodes g/nodes-and-links))


