(defproject log-service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [metosin/compojure-api "1.0.1"]
                 [com.novemberain/langohr "3.5.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring-cors "0.1.7"]
                 [cprop "0.1.6"]
                 [http-kit "2.2.0"]
                 [camel-snake-kebab "0.3.2"]
                 [cc.qbits/spandex "0.3.10"]
                 ;; logging
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.6"]
                 [ring.middleware.logger "0.5.0" :exclusions [org.slf4j/slf4j-log4j12]]
                 [org.slf4j/log4j-over-slf4j "1.7.18"]]

  :profiles {:uberjar {:aot :all}}

  :target-path "target/%s/"

  :main log-service.core
  :uberjar-name "log-service.jar")
