(ns sokoban.cache
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(declare load-catalog-list)

(defrecord Cache []
  component/Lifecycle
  (start [this]
    (if-not (:catalog-list this)
      (do (log/info "Starting cache component")
          (assoc this
                 :catalog-list (atom (load-catalog-list))
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

(defn load-catalog-list []
  (let [filename "cache/catalog-list.edn"]
    (if (.exists (io/as-file filename))
      (edn/read-string (slurp (io/resource filename)))
      [])))

(defn set-catalog-list [cache catalog-list]
  (reset! (:catalog-list cache) catalog-list)
  (with-open [w (io/writer "resources/cache/catalog-list.edn")]
    (.write w (str catalog-list))))
