(ns sokoban.views
  (:require [re-frame.core :as rf]
            [sokoban.events :as events]
            [sokoban.subs :as subs]))

(def cell-size 40)

(defn cell [cell-type]
  [:span.button {:style {:width cell-size
                         :height cell-size}}
   cell-type])

(defn board []
  (let [level (rf/subscribe [::subs/level])]
    [:div
     (doall (for [y (range (count @level))]
              ^{:key y}
              [:div (doall (for [x (range (count (get @level y)))]
                             ^{:key x} [cell (get-in @level [y x])]))]))]))

(defn game []
  [:div [board]])

(defn main-panel []
  [game])
