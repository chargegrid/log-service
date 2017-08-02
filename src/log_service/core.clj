(ns log-service.core
  (:require [log-service.routes :refer [app]]
            [log-service.queue :as queue]
            [org.httpkit.server :refer [run-server]]
            [clojure.tools.logging :as log]
            [log-service.settings :refer [config]]
            [log-service.es :as es])
  (:gen-class))

(defn -main [& args]
  (log/info "Starting...")
  (queue/setup)
  (es/setup!)
  (let [port (:port config)]
    (log/info "Server started at port " port)
    (run-server app {:port port})))