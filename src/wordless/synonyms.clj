(ns wordless.synonyms
  (:require
   [clojure.string :as str]
   [wordless.redis :as r]
   [wordless.util :as u]
   [wordless.graph :as g]
   [inflections.core :as i]))


(defn related-words [word]
  (or (r/related-words (str/trim word))
      (r/related-words (-> word i/singular i/capitalize))
      (r/related-words (-> word i/plural i/capitalize))
      (r/related-words (-> word i/singular .toLowerCase))
      (r/related-words (-> word i/plural .toLowerCase))))

(defn word->nodes [word]
  (->> word
       related-words
       (remove #(= % word))
       (into [word])
       (take 15)))

(defn syngraph [word]
  (-> word word->nodes g/nodes-and-links))

