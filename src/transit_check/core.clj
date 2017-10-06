(ns transit-check.core
  (:require [clojure.tools.logging :as log]
            [immutant.web :as immutant]
            [org.httpkit.server :as httpkit]
            [compojure.api.sweet :as sweet :refer [GET POST context]]
            [ring.util.http-response :as resp]
            [metosin.dates :as dates]
            metosin.edn.dates
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
  (-> {:formats muuntaja
       :swagger {:ui "/swagger"
                 :spec "/swagger.json"}}
      (sweet/api routes)))

(defonce server (atom nil))

(defn immutant-start-server [server-shutdown]
  (when server-shutdown
    (log/info "shutdown server...")
    (server-shutdown))
  (log/info "start immutant server...")
  (let [server (immutant/run
                 ; immutant needs handler to pass fn?
                 (fn [request] (handler request))
                 {:host "127.0.0.1"
                  :port 8081
                  :path "/"})]
    (fn [] (immutant/stop server))))

(defn httpkit-start-server [server-shutdown]
  (when server-shutdown
    (log/info "shutdown server...")
    (server-shutdown))
  (log/info "start httpkit server...")
  (httpkit/run-server handler {:port 8081}))

(def start-server
  httpkit-start-server
  #_ immutant-start-server)

(swap! server start-server)
