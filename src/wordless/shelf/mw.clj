(ns wordless.mw
  (:require [net.cgrand.enlive-html :as html]
            [clojure.data.json :as json]
            [wordless.util :as util]
            [clojure.pprint :as pp]
            [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defn url-str [url]
  (->> url
      (java.net.URL.)
      (html/html-resource)
      (apply str)))

(defn trim-extra-chars [word-seq]
   (map #(clojure.string/replace % #"\(\"|\"\)|\newline|also" "") word-seq))

(defn syn-map [word]
  (as-> word x
        (str "http://www.merriam-webster.com/thesaurus/" x)
        (url-str x)
        (re-seq #"\(\"[a-zA-Z]*\"\)|Related Words|Near Antonyms" x)
        (trim-extra-chars x)
        (drop-last 9 x)
        (filter #(not= "" %) x)
        (filter #(not= "or" %) x)
        (partition-by #(get #{"Synonyms"
                              "Related Words"
                              "Antonyms"
                              "Near Antonyms"} %) x) ; (junk (Synonyms) (s1 s2) ...)
        (rest x)  ; ((Synonyms) (s1 s2 s3) (Related Words) (r1 r2) ...)
        (let [ks (map (comp keyword util/kb-case first) (take-nth 2 x))
              vs (take-nth 2 (rest x))]
          (into (util/zipmap-add ks vs) {:word word}))))

;; take syn-map map it over every word in the dictionary, and when theres

(defn safe-syn-map [word]
  (try (esd/put "wordless" "thesaurus" word (syn-map word))
       (catch Exception e (str "invalid: " word))))

(defn on-all-words [f]
  (let [words (->> "/usr/share/dict/words"
              (slurp)
              (clojure.string/split-lines))]
    (for [w words] (f w))))
