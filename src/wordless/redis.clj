(ns wordless.redis
  (:require [print.foo :refer :all]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.java.shell :as sh :only [sh]]))

(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(def server1-conn {:pool {} :spec {}})

(defn related-words [word]
  (or (wcar* (car/zrange word 0 -1))
      (wcar* (car/zrange (str word) 0 -1))))

(defn start-redis! []
  (or 
   (sh/sh "redis-server" "resources/redis.conf"))
  "ok.")

(comment
  
  (defn insert-redis-word [word]
    (let [related (syn/related-words word)]
      (map #(wcar* (car/zadd word 0 %)) related)))

  (defn recreate-disk-db! [you-sure?]
    (when you-sure?
      (let [words-to-insert (-> "out.txt" slurp clojure.string/split-lines)]
        (map insert-redis-word words-to-insert))))


  (wcar* (car/set "k1" "v1")
         (car/get "k1"))

  )
