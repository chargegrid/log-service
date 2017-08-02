(ns log-service.settings
  (:require [cprop.core :refer [load-config]]))

(def config (load-config))