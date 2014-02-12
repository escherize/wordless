(ns wordless.handler
  (:require [clojure.data.json :as json]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [wordless.word-net :as wn]
            [ring.middleware.cors :as cors]
            [ring.middleware.json :as rjson]))



(defroutes app-routes
  (GET "/" [] "Just started up?")

  ;; note: there are a lot of methods in wn/*

  ;; {body :body} will grab the body from the POST request.
  ;; the body should be {"word" <word-instance>}.
  (POST "/graph/" {body :body} (json/json-str (wn/syngraph (body "word"))))

  (route/resources "")
  (route/not-found "Not Found"))

(defn allow-cross-origin
  "middleware function to allow crosss origin"
  [handler]
  (fn [request]
   (let [response (handler request)]
    (assoc-in response [:headers "Access-Control-Allow-Origin"]
         "*"))))

(defn options-200
  "middleware function to always 200 an OPTIONS request"
  [handler]
  (fn [request]
    (if (= :options (:request-method request))
      {:headers {"Access-Control-Allow-Origin" "*"
                 "Access-Control-Allow-Methods" "GET, POST, PUT, OPTIONS"}
       :body ""
       :status 204}
      (handler request))))

(defn logger
  [handler]
  (fn [request]
    (let [response (handler request)]
      (println "request:~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      (clojure.pprint/pprint request)
      (println "response:===============================")
      (clojure.pprint/pprint response)
      response)))

(def app
  (-> (handler/site app-routes)
      (options-200)
      (cors/wrap-cors :access-control-allow-origin #".*"
                      :access-control-allow-credentials "true"
                      :access-control-allow-headers ["Origin"
                                                     "X-Requested-With"
                                                     "Content-Type"
                                                     "Accept"]
                      :access-control-allow-methods ["GET"
                                                     "POST"
                                                     "PUT"
                                                     "DELETE"
                                                     "OPTIONS"])
      (logger)
      (rjson/wrap-json-body)))
