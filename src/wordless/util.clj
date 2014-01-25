(ns wordless.util
  (:require [clojurewerkz.elastisch.rest :as esr :only connect!]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println (str "dbg: " '~body " = " x#))
     x#))

(defn connect-to-elastic-search []
  (esr/connect! "http://127.0.0.1:9200"))

(defn kb-case
  "makes a string of any style into kebab-case"
  [s]
  (-> s
      (.toLowerCase)
      (.replace " " "-")))

(defn zipmap-add
  "Returns a map with the keys mapped to the corresponding vals,
   and given duplicate keys combines the vals."
  [keys vals]
    (loop [map {}
           ks (seq keys)
           vs (seq vals)]
      (if (and ks vs)
        (if (get map (first ks))  ;; if the key already exists
          (let [new-ks (distinct
                        (flatten
                         (vector (first vs)
                                 (get map (first ks)))))]
            (recur (assoc map (first ks) new-ks)
                   (next ks)
                   (next vs)))
          (recur (assoc map (first ks) (first vs))
                 (next ks)
                 (next vs)))
        map)))
