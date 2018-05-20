(ns sokoban.main
  (:require [com.stuartsierra.component :as component]
            [sokoban.system :refer [new-system]])
  (:gen-class))

(defn -main [& args]
  (component/start-system (new-system)))
