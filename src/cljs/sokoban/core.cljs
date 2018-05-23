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

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array)))

(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array)))

(extend-type js/TouchList
  ISeqable
  (-seq [array] (array-seq array)))

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
                   [{:which 74}] [{:which 40}]]]
     :always-listen-keys (when-not config/debug?
                           [{:which 72} {:which 37}
                            {:which 76} {:which 39}
                            {:which 75} {:which 38}
                            {:which 74} {:which 40}])
     :prevent-default-keys (when-not config/debug?
                             [{:which 72} {:which 37}
                              {:which 76} {:which 39}
                              {:which 75} {:which 38}
                              {:which 74} {:which 40}])}]))

(defn ^:export init! []
  (routes/app-routes)
  (rf/dispatch-sync [::events/download-catalogs])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (setup-keys)
  (mount-root))
