(defproject wordless "0.1.0-SNAPSHOT"
  :description "A rest service for helping you find new words."
  :url "http://example.com/FIXME"
  :dependencies [
                 [compojure "1.1.6"]
                 [org.clojure/data.json "0.2.3"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [ring "1.2.1"]
                 [ring-cors "0.1.0"]
                 [ring/ring-json "0.2.0"]
                 [org.clojure/clojure "1.5.1"]
                 [print-foo "0.5.0"]
                 [com.taoensso/carmine "2.6.0"]
                 [inflections "0.9.7"]
                 ]
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler wordless.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [slamhound "1.5.0"]]}})
