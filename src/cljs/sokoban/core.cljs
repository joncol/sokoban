(ns sokoban.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [sokoban.events :as events]
            [sokoban.routes :as routes]
            [sokoban.views :as views]
            [sokoban.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:export init! []
  (routes/app-routes)
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
