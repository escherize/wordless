(ns wordless.synonyms
  (:require
   [wordless.sqlite :as sql]
   [clojure.string :as str]
   [wordless.util :as u]))

(defn defs->words [definitions]
  (->> definitions
       (map #(str % " "))
       (apply str)
       (#(str/split % #" "))))

(defn quick-synonyms [lemma]
  (->>   lemma
         sql/lemma->wordsXsensesXsynsets
         (map :definition)
         defs->words
         (filter (partial u/longer-than 3))
         u/sanitize
         (sort-by (comp - count))
         (take 20)))

(defn word->synmap [word]
  "returns a map like {word (syn1, syn2, ...)}
   note this is different than {word ((synset1)(synset2)(synset3))}"
  {word (-> word
            quick-synonyms
            flatten
            distinct)})

(defn links-for-map
  "returns edges for graph"
  [m]
  (first (for [[k v] m] (if (map? v)
                          (links-for-map v)
                          (for [i v] [k i])))))

(defn size->edge-set [size]
  (map #(vector %1 %2)
       (repeat 0)
       (range 1 size)))

(defn nodes-and-links [syn-map]
  (let [nodes (-> syn-map
                  seq
                  flatten
                  distinct)
        edges (-> nodes
                  count
                  size->edge-set)]
    (assoc {}
      :nodes (mapv #(assoc {} :label % :weight 0) nodes)
      :links (mapv (fn [[k v]]
                     (assoc {} :source k :target v)) edges))))

(defn syngraph [word]
  (-> word
      word->synmap
      nodes-and-links))

