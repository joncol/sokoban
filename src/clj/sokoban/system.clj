(ns sokoban.system
  (:require [com.stuartsierra.component :as component]
            [config.core :refer [env]]
            [sokoban.app-server :refer [new-app-server]]
            [sokoban.cache :refer [new-cache]]
            [sokoban.handler :refer [app-fn]]))

(defn- port []
  (Integer/parseInt (or (some-> (:port env) str) "3000")))

(defn new-system []
  (component/system-map
   :cache (new-cache)
   :server (component/using (new-app-server app-fn (port)) [:cache])))
