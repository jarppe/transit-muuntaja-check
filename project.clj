(defproject transit-check "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]

                 [org.immutant/web "2.1.9"]
                 [http-kit "2.2.0"]
                 [ring/ring-jetty-adapter "1.6.2"]

                 [metosin/ring-http-response "0.9.0"]
                 [metosin/muuntaja "0.3.2"]
                 [metosin/compojure-api "2.0.0-20170926.130606-1"]
                 [metosin/metosin-common "0.4.0"]

                 [org.clojure/tools.logging "0.4.0"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]])
