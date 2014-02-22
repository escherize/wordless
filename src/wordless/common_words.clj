(ns wordless.common-words
  (:require [clojure.string :as string]
            [clojure.pprint :as p]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


(defn word-freq [word]
  (let [find-results  (fn [stream]
                        (for [record stream
                              :when (= (second record) word)] record))
        rdr (io/reader "resources/wordfreqs.csv")
        csv-stream (csv/read-csv rdr)
        results (doall (find-results csv-stream))
        [rank_ lemma_ pos_ freq disp_] (first results)]
    (.close rdr)
    freq))
