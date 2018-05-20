(ns sokoban.app-server
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ring.server.standalone :refer :all]))

(defrecord AppServer [handler-fn port server]
  component/Lifecycle
  (start [this]
    (if-not server
      (do (log/info "Starting app server component")
          (let [deps    (keys (component/dependencies this))
                handler (handler-fn (zipmap deps (map (partial get this) deps)))
                server  (serve handler
                               {:port         port
                                :auto-reload? true
                                :join?        false})]
            (assoc this :server server)))
      this))
  (stop [this]
    (if server
      (do (log/info "Stopping app server component")
          (.stop server)
          (.join server)
          (assoc this :server nil))
      this)))

(defn new-app-server
  "Creates a app server Component. The argument `handler-fn` is a function that
  will be called to create the Ring handler for the server. This function is
  expected to take a single argument, a map, with keys for all the dependencies
  of the AppServer component. So if you define the app server component as:
    `(component/using (app-server/new-app-server my-handler-fn 3000)
                      [:database :honeybadger])`,
  The function `my-handler-fn` will be invoked with a map containing the keys
  `:database` and `:honeybadger`, each being set to the corresponding
  component. This means that your handler functions can depend on an arbitrary
  number of other components"
  [handler-fn port]
  (map->AppServer {:handler-fn handler-fn, :port port}))
