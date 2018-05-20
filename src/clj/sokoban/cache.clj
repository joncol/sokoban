(ns sokoban.cache
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defrecord Cache []
  component/Lifecycle
  (start [this]
    (if-not (:catalog-list this)
      (do (log/info "Starting cache component")
          (assoc this
                 :catalog-list (atom [])
                 :catalogs (atom {})
                 :levels (atom {})))
      this))
  (stop [this]
    (if (:catalog-list this)
      (do (log/info "Stopping cache component")
          (assoc this
                 :catalog-list nil
                 :catalogs nil
                 :levels nil))
      this)))

(defn new-cache []
  (Cache.))
