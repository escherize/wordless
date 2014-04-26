(defproject wordless "0.1.0-SNAPSHOT"
  :description "A rest service for helping you find new words."
  :url "http://example.com/FIXME"
  :dependencies [
                 ;; [clojurewerkz/elastisch "1.4.0"]
                 ;; [enlive "1.1.5"]
                 ;; [org.clojure/data.csv "0.1.2"]
                 [compojure "1.1.6"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [ring "1.2.1"]
                 [ring-cors "0.1.0"]
                 [ring/ring-json "0.2.0"]
                 [org.clojure/clojure "1.5.1"]
                 [print-foo "0.5.0"]
                 ]
  :uberjar-name "wordless-server.jar"

  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.8.10"]
            [lein-beanstalk "0.2.7"]]
  :ring {:handler wordless.handler/app
         :init wordless.sqlite/start-db!}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [slamhound "1.5.0"]]}
   :uberjar {:aot :all}})
