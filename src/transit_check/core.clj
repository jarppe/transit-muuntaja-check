(ns transit-check.core
  (:require [clojure.tools.logging :as log]

            [immutant.web :as immutant]
            [org.httpkit.server :as httpkit]
            [ring.adapter.jetty :as jetty]

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

(defn immutant-server []
  (log/info "start immutant server...")
  (let [server (immutant/run
                 ; immutant needs handler to pass fn?
                 (fn [request] (handler request))
                 {:host "127.0.0.1"
                  :port 8081
                  :path "/"})]
    (fn [] (immutant/stop server))))

(defn httpkit-server []
  (log/info "start httpkit server...")
  (httpkit/run-server handler {:port 8081}))

(defn jetty-server []
  (let [server (jetty/run-jetty handler {:port 8081, :join? false})]
    (fn [] (.stop server))))

(defn start-server [prev-server make-server]
  (when prev-server
    (log/info "stopping server...")
    (prev-server))
  (make-server))

(swap! server start-server
       #_immutant-server
       #_httpkit-server
       jetty-server
       )
