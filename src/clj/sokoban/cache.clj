(ns sokoban.cache
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(declare load-catalog-list)
(declare populate-cache-from-disk)

(defrecord Cache []
  component/Lifecycle
  (start [this]
    (if-not (:catalog-list this)
      (do (log/info "Starting cache component")
          (assoc this
                 :catalog-list (atom (load-catalog-list))
                 :catalogs (atom (populate-cache-from-disk :catalogs))
                 :levels (atom (populate-cache-from-disk :levels))))
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

(defn get-cached-item
  "Helper function to return a (possibly delayed) item from the cache."
  [cache cache-name id]
  (let [x (-> cache (get cache-name) deref (get id))]
    (cond
      (nil? x)   nil
      (delay? x) (do (log/debug (str "Loading and returning cached "
                                     (name cache-name) ": " id))
                     @x)
      :else      (do (log/debug (str "Returning cached " (name cache-name)
                                     ": " id))
                     x))))

(defn populate-cache-from-disk [cache-name]
  (reduce (fn [items f]
            (if-let [[_ id] (and (.isFile f)
                                 (re-matches #"(\d+)\.edn" (.getName f)))]
              (assoc items
                     (Integer/parseInt id)
                     (delay (edn/read-string (slurp f))))
              items))
          {}
          (.listFiles (clojure.java.io/file (str "cache/" (name cache-name))))))

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

(defn add-catalog! [cache id catalog]
  (log/debug "Caching catalog, ID:" id)
  (swap! (:catalogs cache) assoc id catalog)
  (with-open [w (io/writer (str "cache/catalogs/" id ".edn"))]
    (.write w (str catalog))))

(defn get-catalog [cache id]
  (get-cached-item cache :catalogs id))

(defn add-level! [cache id level]
  (log/debug "Caching level, ID:" id)
  (swap! (:levels cache) assoc id level)
  (with-open [w (io/writer (str "cache/levels/" id ".edn"))]
    (.write w (str level))))

(defn get-level [cache id]
  (get-cached-item cache :levels id))
