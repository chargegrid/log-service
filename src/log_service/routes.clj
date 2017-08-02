(ns log-service.routes
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok unprocessable-entity]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.reload :refer [wrap-reload]]
            [log-service.es :as es]
            [schema.core :as s]))

(def options {:format
              {:formats [:json]}})

(def api-routes
  (api options
       (POST "/ocpp-logs" []
         :header-params [x-tenant-id]
         ; TODO: Add query schema according to ElasticSearch queries.
         :body [client-query s/Any]
         (let [{:keys [ok? data]} (es/search-ocpp-log client-query x-tenant-id)]
           (if ok?
             (ok data)
             (unprocessable-entity data))))))

(def app (-> #'api-routes
             wrap-with-logger
             wrap-reload))
