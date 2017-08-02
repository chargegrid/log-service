(ns log-service.es
  (:require [log-service.settings :refer [config]]
            [org.httpkit.client :as http]
            [clojure.string :refer [join]]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [qbits.spandex :as s]
            [qbits.spandex.utils :as s-utils]))

(let [es-config (:elasticsearch config)
      hosts (:hosts es-config)
      username (:username es-config)
      password (:password es-config)
      client (delay (s/client {:hosts       hosts
                        :http-client {:basic-auth {:user username :password password}}}))
      index-name "ocpp-logs"
      type-name "ocpp-logs"
      ;TODO: Create-automatically for new tenant
      space-x-tenant "5pVvHNXhpi0h06lcKqwGxi"
      tesla-tenant "kOKcPQlWrFBVYLCV5DCPF"
      mappings {type-name {:properties {:charge-box-serial {:type "keyword"}
                                        :ocpp-message      {:type "text"}
                                        :timestamp         {:type "date"}
                                        :direction         {:type "keyword"}
                                        :response-to       {:type "keyword"}
                                        :wamp-type         {:type "keyword"}
                                        :wamp-msg-id       {:type "keyword"}
                                        :wamp-action       {:type "keyword"}
                                        :wamp-payload      {:type "object"}
                                        :tenant-id         {:type "keyword"}}}}
      aliases {space-x-tenant {:filter  {:term {:tenant-id space-x-tenant}}
                               :routing space-x-tenant}
               tesla-tenant   {:filter  {:term {:tenant-id tesla-tenant}}
                               :routing tesla-tenant}}]
  (defn- index-exists? []
    (let [url (s-utils/url [index-name])
          res (s/request @client {:url    url
                                 :method :HEAD})]
      (= (:status res) 200)))

  (defn- index-create! []
    (log/info "Creating index" index-name)
    (let [url (s-utils/url [index-name])
          payload {:mappings mappings
                   :aliases  aliases}
          res (s/request @client {:url    url
                                 :method :PUT
                                 :body   payload})]
      (when (not (= (:status res) 200))
        ; TODO: Send to Sentry
        (throw (Exception. (str "Cannot create ES index " res))))))

  (defn setup! []
    (if (index-exists?)
      (log/info "Index exists")
      (index-create!)))

  (defn index-ocpp-msg! [doc]
    (let [tenant-id (:tenant-id doc)
          url (s-utils/url [tenant-id type-name])
          res (s/request @client {:url    url
                                 :method :POST
                                 :body   doc})]
      (when (not (= (:status res) 201))
        ; TODO: Send to Sentry
        (throw (Exception. (str "Cannot index the doc " res))))))

  (defn search-ocpp-log [client-query tenant-id]
    (let [url (s-utils/url [tenant-id type-name "_search"])
          res (s/request @client {:url    url
                                 :method :GET
                                 :body   client-query})]
      (log/info "Searching OCPP logs on url " url " with query " client-query)
      {:data (:body res)
       :ok?  (= (:status res) 200)})))



