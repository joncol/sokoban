(ns sokoban.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-pressed.core :as rp]
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

(defn setup-keys []
  (rf/dispatch
   [::rp/set-keydown-rules
    {:event-keys [[[::events/move-player :left]
                   [{:which 72}] [{:which 37}]]
                  [[::events/move-player :right]
                   [{:which 76}] [{:which 39}]]
                  [[::events/move-player :up]
                   [{:which 75}] [{:which 38}]]
                  [[::events/move-player :down]
                   [{:which 74}] [{:which 40}]]]}]))

(defn ^:export init! []
  (routes/app-routes)
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (setup-keys)
  (dev-setup)
  (mount-root))
