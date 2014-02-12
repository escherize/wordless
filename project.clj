(defproject wordless "0.1.0-SNAPSHOT"
  :description "A rest service for helping you find new words."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [org.clojure/data.json "0.2.3"]
                 [ring/ring-json "0.2.0"]
                 ;; elastic search library                      DEPRECIATED
                 [clojurewerkz/elastisch "1.4.0"]
                 ;; enlive for scraping thesaurus.com into es.  DEPRECIATED
                 [enlive "1.1.5"]
                 ;; korma + mysql driver
                 [korma "0.3.0-RC5"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.clojure/data.csv "0.1.2"]
                 [ring-cors "0.1.0"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler wordless.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [slamhound "1.5.0"]]}})
