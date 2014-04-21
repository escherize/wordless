(ns wordless.word-net
  (:use [korma.db]
        [korma.core])
  (:require [clojure.pprint :as pp]
            [wordless.util :as u]
            [wordless.postgres :as pg]))

(declare adjpositions adjpositiontypes casedwords lexdomains
         lexlinks linktypes morphmaps morphs postypes samples
         semlinks senses synsets vframemaps vframes vframesentencemaps
         vframesentences words)

(defdb db-postgres
  (postgres {
             :user "bryanmaass"
             :password "idkmyBFFa"
             :port 5432
             :host "mydbinstance-wordnet.ci4y9y2twpdq.us-west-2.rds.amazonaws.com"
             :db "wordnet30"}))

(defentity casedword
  (pk :wordid)
  (table :casedword)
  (entity-fields :lemma))

(defentity word
  (pk :wordid)
  (table :word)
  (entity-fields :lemma))

(defentity sense ;; can probably grab more info from sense
  (table :sense)
  (pk :wordid)
  (entity-fields :synsetid))

(defn pg-wordid [w]
  (select word
          (fields :wordid)
          (where {:lemma w})))

(defn wordid [w] ;;must return 1.
  (-> (pg-wordid w)
      first
      :wordid))

(defn pg-word [wid]
  (select word
          (fields :lemma)
          (where {:wordid wid})))

(defn pg-synsetid [wid]
  (select sense
          (fields :synsetid)
          (where {:wordid wid})))

(defn synsets [wid]
  (map :synsetid (pg-synsetid wid)))

(defn pg-synwords [ssid]
  (select sense
          (where {:synsetid ssid})))

(defn  word-to-synsetids [wstr]
  (synsets (wordid wstr)))



(defn synsetid-to-words [ssid]
  (->> ssid
       (pg-synwords)
       (map :wordid )
       (map pg-word )
       (map (comp :lemma first) )))


(defn synonyms [word]
  (->> word
      (word-to-synsetids )
      (map synsetid-to-words )
      (map (fn [coll] (filter #(not= word %) coll)))))

(defn word-to-wordid [word]
  (map :wordid (select words (where {:lemma word}))))


(defn define [word]
  "takes a word, returns a set of strings which represent its definition"
  (-> word
      (word-to-wordid)
      (wordid-to-definitions)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;     begin explore
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def empty-words #{"into" "yours" "than" "all" "against" "each" "during" "once" "but" "both" "herself" "down" "whatever" "anything" "or" "nobody" "another" "what" "either" "in" "outside" "through" "with" "underneath" "although" "but also" "because" "themselves" "one" "where" "that" "ours" "between" "near" "its" "without" "they" "yet" "I" "for" "throughout" "from" "within" "it" "everyone" "my" "any" "him" "around" "whomever" "about" "anybody" "we" "out" "whom" "off" "beyond" "how" "she" "below" "nor" "while" "past" "as" "someone" "his" "at" "mine" "everything" "neither" "over" "some" "till" "her" "whether" "somebody" "ourselves" "by" "of" "and" "itself" "toward" "like" "himself" "me" "under" "myself" "none" "few" "not only" "except" "theirs" "beside" "onto" "anyone" "behind" "their" "upon" "when" "since" "whichever" "though" "to" "up" "beneath" "despite" "after" "whoever" "them" "so" "among" "several" "inside" "he" "if" "which" "along" "yourselves" "everybody" "above" "us" "until" "who" "hers" "you" "across" "on" "yourself" "many" "before" "the" "a" "an" "is" "having" "be" "have" "" "s" "not" "act" "used" "especially" "something" "usually"})

(defn shave-useless-words [lst & fxn]
  (let [f (or fxn identity)]
    "use after frequency"
    (filter #(not (empty-words (f %))) lst)))


(defn mapify [f words]
  (let [fwords (map f words)]
    (zipmap (map keyword words) fwords)))

(defn flat-def [word]
  (as-> word x
        (define x)
        (flatten x)
        (map #(clojure.string/split % #"\W") x)
        (flatten x)
        (shave-useless-words x)))

(defn flat-syn [word]
  (-> word
      (synonyms)
      (flatten)))

(defn sort-by-freq [lst]
  (reverse (sort-by val (frequencies lst)))) ;largest first

(defn def-def [word]
  (->> word
      (flat-def)
      (map flat-def)
      (flatten)))

(defn def-defs [& words]
  (->> words
        (map def-def)
        (flatten)
        (sort-by-freq)))

(defn synmap [word]
  "returns a map like {word (syn1, syn2, ...)}
   note this is different than {word ((synset1)(synset2)(synset3))}"
  (assoc {} word (-> word
                     (synonyms)
                     (flatten)
                     (distinct))))

(defn synsyn [word]
  (->> word
      (synonyms)
      (flatten)
      (map synonyms)
      (flatten)
      (distinct)))

(defn synsyn-graph [word]
  (->> word
      (synonyms)))

(defn related-words [& words]
  (->> words
       (map def-defs)
       (map #(into {} %))
       (apply merge-with + )
       (into [])
       (map penalize-word )
       (sort-by second)
       (reverse)))

(defn related-words-2 [words]
  (->> words
       (map def-defs)
       (map #(into {} %))
       (apply merge-with + )
       (into [])
       (map penalize-word )
       (sort-by second)
       (reverse)))

(defn map-idxs
  [sm]
  (let [items (conj (first (vals sm)) (first (keys sm)))
        list-of-entries (map (comp vec reverse)
                             (map-indexed vector items))]
    (into {} list-of-entries)))

(defn links-for-map
  "returns edges for graph"
  [m]
  (first (for [[k v] m] (if (map? v)
                          (links-for-map v)
                          (for [i v] [k i])))))

(defn nodes-and-links [syn-map]
  (let [nodes (distinct (flatten (seq syn-map)))
        word-links (links-for-map syn-map)
        links (->> word-links
                   (flatten)
                   (map (map-idxs syn-map))
                   (partition 2)
                   (map vec))
        prep-nodes (map #(assoc {} :label %) nodes)
        prep-links (map (fn [[k v]]
                          (assoc {} :source k :target v :value 1)) links)]
    (assoc {}
        :nodes prep-nodes
        :links prep-links)))

(defn syngraph [word]
  (nodes-and-links (synmap word)))

(defn words-to-list
  "used when we take arguments like a,b,c ==> [a b c]"
  [words-str]
  (clojure.string/split words-str #","))

(defn summary-lst [words-str]
  (let [words (words-to-list words-str)
        related (mapify related-words words)
        synonyms       (mapify synonyms words)
        definitions    (mapify define words)]
    (into {} [[:related related]
              [:synonyms synonyms]
              [:definitions definitions]])))

(defn related-words-lst [words-str]
  (->> words-str
      (words-to-list)
      (related-words-2)
      (filter #(> 2 (second %)))))
