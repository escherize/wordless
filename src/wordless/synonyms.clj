(ns wordless.synonyms
  (:require
   [clojure.string :as str]
   [wordless.redis :as r]
   [wordless.util :as u]))

(defn links-for-map
  "returns edges for graph"
  [m]
  (first (for [[k v] m]
           (if (map? v)
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
    {:nodes (mapv #(assoc {} :label % :weight 0) nodes)
     :links (mapv (fn [[k v]] {:source k :target v}) edges)}))

(defn singularize [word]
  (if (.endsWith word "s")
    (apply str (drop-last word))))

(defn related-words [word]
  (or (r/related-words word)
      (r/related-words (.toLowerCase word))))

(defn word->nodes [word]
  (->> word
       related-words
       (into [word])
       (take 15)))

(defn syngraph [word]
  (-> word
      word->nodes
      nodes-and-links))
