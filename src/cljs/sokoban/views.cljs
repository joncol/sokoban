(ns sokoban.views
  (:require [re-frame.core :as rf]
            [sokoban.events :as events]
            [sokoban.subs :as subs]))

(def cell-size 40)

(defn cell [cell-type]
  [:span.button {:style {:width cell-size
                         :height cell-size}}
   cell-type])

(defn game []
  (let [level (rf/subscribe [::subs/level])]
    [:div
     (doall (for [y (range (count @level))]
              ^{:key y}
              [:div (doall (for [x (range (count (get @level y)))]
                             ^{:key x} [cell (get-in @level [y x])]))]))]))

(defn home-panel []
  [game])

(defn about-panel []
  [:div "This is the About Page."
   [:div [:a {:href "#/"} "go to Home Page"]]])

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
