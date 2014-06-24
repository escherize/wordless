(ns wordless.nlp
  (:require [clojure.string :as str]
            [clojure.edn :only [read-string]]
            [wordless.redis :as red]
            [clojure.java.shell :as sh :only [sh]]
            [clojure.java.io :as io]
            [print.foo :refer :all]))

(defn bi-line->s+t+score [line]
  (let [[count source target] (str/split line #"\t|\ ")]
    [source target (read-string count)]))

(defn populate-uni [bigram-file-path]
  (with-open [rdr (io/reader bigram-file-path)]
    (doseq [line (rest (line-seq rdr))]
      (let [[source target count] (bi-line->s+t+score line)
            source-freq (red/get-word-count source)
            target-freq (red/get-word-count target)]
        (red/insert-word-count source (+ source-freq count))
        (red/insert-word-count target (+ target-freq count))))))


(defn populate-redis [bigram-path]
  (let [line-num (atom 0)]
    (with-open [rdr (io/reader bigram-path)]
      (doseq [line (rest (line-seq rdr))]
        (swap! line-num inc)
        (when (= 0 (mod @line-num 20000))
          (println "line" @line-num ":"
                   (System/currentTimeMillis)
                   ":" line))
        (let [[source target count] (bi-line->s+t+score line)
              target-freq (red/get-word-count target)
              s->t-weight (double (/ (* count count) (inc (* target-freq target-freq))))]
          (when (< 100 count)
            (red/insert-related-words source target s->t-weight)))))))

(comment
  ;; run server with:
  ;; redis-server dir /Users/bryanmaass/dev/wordless/resources dbfilename nlp-wordless.rdb --loglevel verbose
  (def bigram-file-path "example/clean-bi.txt")
  (do
    ;; then populate it with all the data it needs using first:
    (time (do
            (println "populating unis...")
            (populate-uni bigram-file-path)
            (println "populated unis...")
            (Thread/sleep 10)))

    ;; then:
    (time (do
            (println "populating bis...")
            (populate-redis bigram-file-path)
            (println "populated bi...")))
    )

  ;; can dump the contents of the db to a file with save in a redis-cli.

  )
