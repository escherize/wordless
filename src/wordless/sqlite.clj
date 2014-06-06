(ns wordless.sqlite
  (:use [clojure.java.jdbc :as j]
        [clojure.java.shell :only [sh]]
        [clojure.string :as str :refer [split]]
        [wordless.util :as u]
        [print.foo]))

(def db-config
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/sqlite_wordnet_data.db"})

(defn start-db! []
  (sh "./resources/sqlite3" "./db/wordnet-sql.db"))

(defn lemma->word-id [lemma]
  (first (j/query db-config
                  ["select * from words where lemma = ?" lemma])))

(defn word-id->lemma [word-id]
  (j/query db-config
           ["select * from words where wordid = ?" (str word-id)]))

(defn lemma->sensesXsynsets [lemma]
  (let [wid (-> lemma lemma->word-id :wordid)]
    (j/query db-config
             ["select * from sensesXsynsets where wordid = ?" wid])))

(defn lemma->wordsXsensesXsynsets [lemma]
  (let [wid (-> lemma lemma->word-id :wordid)]
    (j/query db-config
             ["select * from wordsXsensesXsynsets where wordid = ?" wid])))

(defn synsetids->lemmas [ssids]
  (j/query db-config
           ["select * from wordsXsensesXsynsets where synsetid in ?"
            (clojure.string/join ", " ssids)]))


(defn defs->words [definitions]
  (->> definitions
       (map #(str % " "))
       (apply str)
       (#(str/split % #" "))))

(defn sql-related-words [lemma]
  (->> lemma
       lemma->wordsXsensesXsynsets
       (map :definition)
       defs->words
       (filter (partial u/longer-than 3))
       u/sanitize
       (sort-by (comp - count))))
