(ns wordless.handler
  (:require [compojure.core :refer :all]
            [clojure.data.json :as json]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [wordless.synonyms :as syns]
            [ring.middleware.cors :as cors]
            [ring.middleware.json :as rjson]))

(defroutes app-routes
  (GET "/" [] "<h1>Just started up?</h1>")

  (GET "/graph/" [word] (json/json-str (syns/syngraph word)))
  ;; {body :body} will destructure the body from the POST request.
  (POST "/graph/" {body :body} (json/json-str (syns/syngraph (body "word"))))

  (route/resources "")
  (route/not-found "Not Found"))

(defn allow-cross-origin
  "middleware function to allow crosss origin"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Access-Control-Allow-Origin"] "*"))))

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
      (println "request ----->                          ")
      (clojure.pprint/pprint request)
      (println "response ~-~-~->                        ")
      (clojure.pprint/pprint response)
      response)))

(def app
  (-> (handler/site app-routes)
      (options-200)
      (cors/wrap-cors :access-control-allow-origin #".*"
                      :access-control-allow-credentials "true"
                      :access-control-allow-headers ["Origin" "X-Requested-With" "Content-Type" "Accept"]
                      :access-control-allow-methods ["GET" "POST" "PUT"])
      (logger)
      (rjson/wrap-json-body)))
