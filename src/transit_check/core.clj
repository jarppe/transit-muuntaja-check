(ns transit-check.core
  (:require [clojure.tools.logging :as log]
            [immutant.web :as immutant]
            [compojure.api.sweet :as sweet :refer [GET POST context]]
            [ring.util.http-response :as resp]
            [metosin.transit.dates :as transit-dates]
            [muuntaja.core :as muuntaja])
  (:import (org.joda.time DateTime)))

(def routes
  (GET "/" []
    (log/info "GET /")
    (resp/ok {:now (DateTime/now)})))

(def muuntaja
  (-> muuntaja/default-options
      (update-in [:formats "application/transit+json"] merge {:decoder-opts {:handlers transit-dates/readers}
                                                              :encoder-opts {:handlers transit-dates/writers}})
      (muuntaja/create)))

(def handler
  ; immutant is stupid
  (let [api (-> {:formats muuntaja
                 :swagger {:ui "/swagger"
                           :spec "/swagger.json"}}
                (sweet/api routes))]
    (fn [request]
      (api request))))

(defn start-server [server]
  (when server
    (log/info "shutdown server...")
    (immutant/stop server))
  (log/info "start server...")
  (immutant/run handler {:host "127.0.0.1"
                         :port 8081
                         :path "/"}))

(defonce server (atom nil))

(swap! server start-server)
