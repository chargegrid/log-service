(ns log-service.queue
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.consumers :as lc]
            [log-service.settings :refer [config]]
            [clojure.data.json :as json]
            [clojurewerkz.support.json]
            [clojure.tools.logging :as log]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case_string]]
            [log-service.es :as es]))

(def wamp-type
  {2 "CALL"
   3 "CALLRESULT"
   4 "CALLERROR"})

; Wamp message schema
;+----------------+---------+-----------+-----------+---------------+
;| Elem #1        | Elem #2 | Elem #3   | Elem #4   | Elem #5       |
;+----------------+---------+-----------+-----------+---------------+
;| 2 (CALL)       |  msgId  | action    | {payload} | -             |
;+----------------+---------+-----------+-----------+---------------+
;| 3 (CALLRESULT) |  msgId  | {payload} | -         | -             |
;+----------------+---------+-----------+-----------+---------------+
;| 4 (CALLERROR)  |  msgId  | err_code  | err_descr | {err_details} |
;+----------------+---------+-----------+-----------+---------------+
(defn parse-ocpp-msg [msg]
  (log/info "Parsing OCPP message in a central-system envelope" msg)
  (let [{:keys [tenant-id charge-box-serial ocpp-message occurred-at direction response-to]} msg
        parsed-ocpp-msg (json/read-str ocpp-message :key-fn keyword)
        [wamp-call-type wamp-msg-id wamp-pos-3 wamp-pos-4 wamp-pos-5] parsed-ocpp-msg]
    {:tenant-id tenant-id
     :charge-box-serial charge-box-serial
     :ocpp-message ocpp-message
     :timestamp occurred-at
     :direction direction
     :response-to response-to
     :wamp-type (get wamp-type wamp-call-type)
     :wamp-msg-id wamp-msg-id
     :wamp-action (case wamp-call-type
                        2 wamp-pos-3
                        3 response-to
                        4 response-to
                        nil)
     :wamp-payload (case wamp-call-type
                         2 wamp-pos-4
                         3 wamp-pos-3
                         4 wamp-pos-5
                         nil)
     :wamp-error-code (case wamp-call-type
                         4 wamp-pos-3
                         nil)
     :wamp-error-message (case wamp-call-type
                               4 wamp-pos-4
                               nil)}))

;; Send and receive messages
(defn handle-message [channel meta ^bytes payload]
  (let [data (-> (String. payload "UTF-8")
                 (json/read-str :key-fn keyword))
        ocpp-msg (transform-keys ->kebab-case data)]
    (if-let [error (:error data)]
      (log/error "Cannot parse transaction " (pr-str error))
      (es/index-ocpp-msg! (parse-ocpp-msg ocpp-msg)))))

;; Basic RabbitMQ setup/subscribe/connect
(defn subscribe-declare-queue [ch handler]
  (let [conf (:amqp config)
        queue (:ocpp-msg-queue-name conf)]
    (lc/subscribe ch queue handler {:auto-ack true})))

(defn shutdown [ch conn]
  (rmq/close ch)
  (rmq/close conn))

(defn setup []
  (let [connection (rmq/connect {:uri (-> config :amqp :url)})
        channel (lch/open connection)]
    (subscribe-declare-queue channel handle-message)
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(shutdown channel connection)))))
