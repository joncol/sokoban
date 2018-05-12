(ns sokoban.views
  (:require [re-frame.core :as rf]
            [sokoban.events :as events]
            [sokoban.subs :as subs]))

(defn name-form [name]
  [:div "Name: "
   [:input {:type "text"
            :value @name
            :on-change #(rf/dispatch [::events/set-name
                                      (-> % .-target .-value)])}]])

(defn home-panel []
  (let [name (rf/subscribe [::subs/name])]
    [:div (str "Hello from " @name ". This is the Home Page.")
     [name-form name]
     [:div [:a {:href "#/about"} "go to About Page"]]]))

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
