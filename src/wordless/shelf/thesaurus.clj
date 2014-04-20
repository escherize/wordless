(ns wordless.thesaurus
  (:require [clojure.string :as string]
            [clojure.pprint :as pprint]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(defn thesarus-lines
  "reads the thesaurus file in, and splits it into a bunch of lines"
  []
  (->
   "resources/rogets.dat"
   (slurp)
   (string/split-lines)))

(def mini-thesaurus (take 50 (thesarus-lines)))

(defn is-source? [s]
  (not= "  "  (str (first s) (first s))))

(defn map-maker
  "returns a thesaurus map with ({word  [synonym ...] ...})"
  [t]
  (let [kvs (partition-by is-source? t)]
    (zipmap
     (map first  (take-nth 2 kvs))
     (map #(into [] (map string/triml %))  (take-nth 2 (rest kvs))))))

((map-maker (thesarus-lines)) "gay")
