(ns wordless.util
  (:require [clojure.string :as str]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println (str "dbg: " '~body " = " x#))
     x#))

(defn kb-case
  "makes a string of any style into kebab-case"
  [s]
  (-> s
      (.toLowerCase)
      (.replace " " "-")))

(defn longer-than [n word]
  (< n (count word)))

(defn sanitize [words]
  (map #(str/replace % #"\(|\)|\;" "") words))
