(ns sokoban.cache
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(declare load-catalog-list)
(declare load-levels)

(defrecord Cache []
  component/Lifecycle
  (start [this]
    (if-not (:catalog-list this)
      (do (log/info "Starting cache component")
          (assoc this
                 :catalog-list (atom (load-catalog-list))
                 :catalogs (atom {})
                 :levels (atom (load-levels))))
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
      (edn/read-string (slurp filename))
      [])))

(defn set-catalog-list! [cache catalog-list]
  (log/debug "Caching catalog list")
  (reset! (:catalog-list cache) catalog-list)
  (with-open [w (io/writer "cache/catalog-list.edn")]
    (.write w (str catalog-list))))

(defn get-level [cache id]
  (let [level (get @(:levels cache) id)]
    (cond
      (nil? level)    nil
      (delay? level)  (do (log/debug "Loading and returning cached level:" id)
                          @level)
      (vector? level) (do (log/debug "Returning cached level:" id)
                          level))))

(defn add-level! [cache id level]
  (log/debug "Caching level, ID:" id)
  (swap! (:levels cache) assoc id level)
  (with-open [w (io/writer (str "cache/levels/" id ".edn"))]
    (.write w (str level))))

(defn load-levels []
  (reduce (fn [levels f]
            (if-let [[_ id] (and (.isFile f)
                                 (re-matches #"(\d+)\.edn" (.getName f)))]
              (assoc levels
                     (Integer/parseInt id)
                     (delay (edn/read-string (slurp f))))
              levels))
          {}
          (.listFiles (clojure.java.io/file "cache/levels"))))
