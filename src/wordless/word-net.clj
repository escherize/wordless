(ns wordless.word-net
  (:use [korma.db])
  (:require [clojure.pprint :as pp]))

(def db  (mysql {:db "wordnet31_snapshot"
                 :user "root"
                 :password ""
                 :host "localhost"
                 :port 3306}))

(defdb db-mysql w )

(use 'korma.core)

(select words
        (limit 10))
