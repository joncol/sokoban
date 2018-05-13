(ns sokoban.core
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [re-pressed.core :as rp]
            [reagent.core :as r]
            [sokoban.config :as config]
            [sokoban.events :as events]
            [sokoban.routes :as routes]
            [sokoban.slurp :include-macros true :refer [slurp]]
            [sokoban.views :as views]))

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
    {:event-keys [[[::events/make-move :left]
                   [{:which 72}] [{:which 37}]]
                  [[::events/make-move :right]
                   [{:which 76}] [{:which 39}]]
                  [[::events/make-move :up]
                   [{:which 75}] [{:which 38}]]
                  [[::events/make-move :down]
                   [{:which 74}] [{:which 40}]]]}]))

(defn ^:export init! []
  (routes/app-routes)
  (let [level (str/split (slurp "resources/level01.txt") #"\n")]
    (rf/dispatch-sync [::events/level-changed level]))
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (setup-keys)
  (dev-setup)
  (mount-root))
