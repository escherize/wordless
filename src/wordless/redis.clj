(ns wordless.redis
  (:require [print.foo :refer :all]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.java.shell :as sh :only [sh]]
            [clojure.string :as str]))

(defmacro wcar* [& body] `(car/wcar conn ~@body))
(def conn {:pool {} :spec {:port 6380}})
(def count-marker "c__")



;; word frequency
(defn insert-word-count
  [word count]
  (wcar* (car/set (str count-marker word) count)))

(defn get-word-count
  [word]
  (try
    (read-string (wcar* (car/get (str count-marker word))))
    (catch Exception e 0)))

(defn insert-related-words
  [word1 word2 count]
  (wcar* (car/zadd word1 count word2)))

(defn- unnormalized-score:word
  [word size]
  (->> (wcar* (car/zrange word 0 -1  "withscores"))
       reverse
       (take (* 2 size))
       (partition 2)
       (mapv reverse)))

(defn get-related-words
  [source]
  (let [result (unnormalized-score:word source 50)]
    (if (not= [] result) result nil)))

(comment

  ;; word counts
  (insert-word-count "apple" 3)
  (get-word-count "apple")

  ;; word relations
  ;; (insert-related-words "w1" "w2" count)
  (get-related-words "orange")

  )
