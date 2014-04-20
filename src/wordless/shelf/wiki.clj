(ns wordless.wiki
  (:require [clojure.string :as string]
            [clojure.pprint :as p]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.response :as esrsp]))


;; btw, there is an exact match
;; http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html

(defn search-for
  "returns a esd result for the page with title word from wikipedia"
  [word]
  "TODO: look into what index this thing is (the "" "" stuff.)"
  (esd/search "" "" :query (q/term :title word)))

(defn get-first-n-matches
  "e.s. likes to return close matches as well as perfect matches.
   ex: "
  [res n]
  (as-> res r
        (esrsp/hits-from r)                            ; parse response for hits
        (filter #(= false ((% :_source) :redirect)) r) ; filter out redirects
        (take n r)
        (flatten r)))

(defn get-first-match [res]
  (get-first-n-matches res 1))

(defn neighbors-of
  "will return the total closest matches from es"
  [word & total]
  (let [res (search-for (string/lower-case word))
        n (if (empty? total) 1 total)]
    (get-first-n-matches res n)))

(defn link-map [word]
  {word (neighbors-of word)})

(defn related
  "words is a list of words that we want synonyms for"
  [words]
  (flatten (distinct (map neighbors-of words))))

(defn play [input-string]
  (let [words (string/split input-string #",|-")
        wordlist (related words)
        wordset (distinct wordlist)
        result (for [w wordset] [w (count (filter #(= % w) wordlist))])
        npr (filter #(Character/isLowerCase (first (first %))) result)]
    (sort #(> (second %) (second %2)) npr)))


;;; tf-idf stuff

(defn term-frequency [entry-map]
  (let [terms (-> entry-map
                  (first)
                  (:_source)
                  (:text)
                  (clojure.string/split  #" "))]
    terms))