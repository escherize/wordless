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

(defn- wordid->lemma [wordid]
  (j/query db ["select * from words where wordid = ?" (str wordid)]))

(defn- lemma->wordid [lemma]
  (first (j/query db ["select * from words where lemma = ?" (str lemma)])))

(defn- lemma->sensesXsynsets [lemma]
  (let [wid (-> lemma lemma->wordid :wordid)]
    (j/query db ["select * from sensesXsynsets where wordid = ?" wid])))

(defn- lemma->wordsXsensesXsynsets [lemma]
  (let [wid (-> lemma lemma->wordid :wordid)]
    (j/query db ["select * from wordsXsensesXsynsets Where wordid = ?" wid])))

(defn- synsetids->lemmas [ssids]
  (let [opts (clojure.string/join ", " ssids)]
    (j/query db [(format  "select * from wordsXsensesXsynsets where synsetid in (%s)" opts)])))

(defn synonyms [lemma]
  (->>
   lemma
   lemma->wordsXsensesXsynsets
   (map :synsetid)
   synsetids->lemmas
   (map :lemma)
   distinct))

(defn synmap [word]
  "returns a map like {word (syn1, syn2, ...)}
   note this is different than {word ((synset1)(synset2)(synset3))}"
  (assoc {} word (-> word
                     (synonyms)
                     (flatten)
                     (distinct))))
(defn map-idxs
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

(defn nodes-and-links [syn-map]
  (let [nodes (distinct (flatten (seq syn-map)))
        word-links (links-for-map syn-map)
        links (->> word-links
                   (flatten)
                   (map (map-idxs syn-map))
                   (partition 2)
                   (map vec))
        prep-nodes (map #(assoc {} :label %) nodes)
        prep-links (map (fn [[k v]]
                          (assoc {} :source k :target v :value 1)) links)]
    (assoc {}
      :nodes prep-nodes
      :links prep-links)))

(defn syngraph [word]
  (nodes-and-links (synmap word)))
