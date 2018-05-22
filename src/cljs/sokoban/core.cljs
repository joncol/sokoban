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
  (-seq [array] (array-seq array 0)))

(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

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

(defn- elem-pos
  "Return the center of the bounding rect of a DOM element with a given ID."
  [id]
  (let [elem (js/document.getElementById id)
        rect (.getBoundingClientRect elem)
        x    (+ (.-x rect) (/ (.-width rect) 2))
        y    (+ (.-y rect) (/ (.-height rect) 2))]
    [x y]))

(defn- touch-handler [event]
  (let [board-rect (-> "board"
                       js/document.getElementById
                       .getBoundingClientRect)
        touch      (-> event .-touches (.item 0))
        [px py]    (elem-pos "player")
        tx         (.-pageX touch)
        ty         (.-pageY touch)
        [dx dy]    [(- tx px) (- ty py)]]
    (when (< ty (+ (.-y board-rect) (.-height board-rect)))
      (.preventDefault event)
      (if (< (Math/abs dx) (Math/abs dy))
        (if (pos? dy)
          (rf/dispatch [::events/make-move :down])
          (rf/dispatch [::events/make-move :up]))
        (if (pos? dx)
          (rf/dispatch [::events/make-move :right])
          (rf/dispatch [::events/make-move :left]))))))

(defn ^:export init! []
  (routes/app-routes)
  (rf/dispatch-sync [::events/download-catalogs])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (setup-keys)
  (let [body (-> js/document
                 (.getElementsByClassName "body-container")
                 first)]
    (.addEventListener body "touchstart" touch-handler))
  (dev-setup)
  (mount-root))
