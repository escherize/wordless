(ns wordless.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.pprint :as pp]
            [wordless.util :as util]
            [wordless.mw :as mw]))


(defroutes app-routes
  (GET "/" [] "hey")
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
