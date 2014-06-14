(ns wordless.redis
  (:require [print.foo :refer :all]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.java.shell :as sh :only [sh]]
            [clojure.string :as str]))

(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(def server1-conn {:pool {} :spec {}})

(defn start-redis! []
  (let [on-ubuntu (= "ubuntu"
                     (-> "whoami" sh/sh :out str/trim))
        resources-dir (-> "pwd" sh/sh :out str/trim (str "/resources/"))]
    (if on-ubuntu
      (sh/sh "redis-server" (str resources-dir "redis.conf"))
      (sh/sh "redis-server" (str resources-dir "redis-local.conf")))))

(defn related-words [word]
  (let [result (wcar* (car/zrange word 0 -1))]
    (if (= [] result) nil result)))

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
