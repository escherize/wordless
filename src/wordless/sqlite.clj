(ns wordless.sqlite
  (:use [clojure.java.jdbc :as j]
        [clojure.java.shell :only [sh]]
        [print.foo]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/sqlite_wordnet_data.db"})

(defn start-db! []
  (sh "./resources/sqlite3" "./db/wordnet-sql.db"))

(defn- lemma->word-id [lemma]
  (first
   (j/query db ["select * from words where lemma = ?" (str lemma)])))

(defn- word-id->lemma [word-id]
  (j/query db ["select * from words where wordid = ?" (str word-id)]))

(defn- lemma->sensesXsynsets [lemma]
  (let [wid (-> lemma lemma->word-id :wordid)]
    (j/query db ["select * from sensesXsynsets where wordid = ?" wid])))

(defn- lemma->wordsXsensesXsynsets [lemma]
  (let [wid (-> lemma lemma->word-id :wordid)]
    (j/query db ["select * from wordsXsensesXsynsets where wordid = ?" wid])))

(defn- synsetids->lemmas [ssids]
  (let [opts (clojure.string/join ", " ssids)]
    (j/query db [(format  "select * from wordsXsensesXsynsets where synsetid in (%s)" opts)])))

;(time  (->> "select * from words where lemma = 'oranges';" (j/prepare-statement conn) j/.executeQuery j/result-set-seq))

(defn synonyms [lemma]
  (->>   lemma
         lemma->wordsXsensesXsynsets
         (map :synsetid)
         synsetids->lemmas
         (map :lemma)
         distinct))

(defn synmap [word]
  "returns a map like {word (syn1, syn2, ...)}
   note this is different than {word ((synset1)(synset2)(synset3))}"
  (assoc {} word (-> word synonyms flatten distinct)))

#_(defn map-idxs
  [sm]
  (let [items (conj (first (vals sm)) (first (keys sm)))
        list-of-entries (map (comp vec reverse)
                             (map-indexed vector items))]
    (into {} list-of-entries)))

(defn links-for-map
  "returns edges for graph"
  [m]
  (first (for [[k v] m] (if (map? v)
                          (links-for-map v)
                          (for [i v] [k i])))))

(defn first-connector [size]
  (map #(vector %1 %2) (repeat 0) (range 1 size)))

(defn nodes-and-links [syn-map]
  (print-let [nodes (distinct (flatten (seq syn-map)))
              edges (first-connector (count nodes))]
             (assoc {}
               :nodes (mapv #(assoc {} :label % :weight 0) nodes)
               :links (mapv (fn [[k v]]
                              (assoc {} :source k :target v)) edges))))

(defn syngraph [word]
  (nodes-and-links (synmap word)))
