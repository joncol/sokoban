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
     (doall
      (for [y (range (count @level))]
        ^{:key y}
        [:div
         (doall
          (for [x (range (count (get @level y)))]
            ^{:key x} [cell (get-in @level [y x])]))]))]))

(defn move-history []
  (let [history-size (rf/subscribe [::subs/history-size])
        current-move (rf/subscribe [::subs/current-move])]
    [:div
     {:style {:width "280px"}}
     [:label (str "Current move: " @current-move " ")]
     [:span.icon.button
      {:on-click #(rf/dispatch [::events/set-current-move 0])}
      [:i.fas.fa-fast-backward {:aria-hidden true}]]
     [:span.icon.button
      {:on-click #(rf/dispatch [::events/update-current-move dec])}
      [:i.fas.fa-step-backward {:aria-hidden true}]]
     [:span.icon.button
      {:on-click #(rf/dispatch [::events/update-current-move inc])}
      [:i.fas.fa-step-forward {:aria-hidden true}]]
     [:span.icon.button
      {:on-click #(rf/dispatch [::events/set-current-move
                                (dec @history-size)])}
      [:i.fas.fa-fast-forward {:aria-hidden true}]]
     [:br]
     [:input.slider.is-fullwidth.is-medium
      {:type     "range"
       :min      0
       :max      (dec @history-size)
       :value    @current-move
       :on-input #(rf/dispatch [::events/set-current-move
                                (-> % .-target .-value int)])}]]))

(defn level-complete-screen []
  (let [completed (rf/subscribe [::subs/level-completed])
        show      (rf/subscribe [::subs/show-congratulations-screen])]
    [:div
     [:div
      {:hidden (not @completed)}
      [:button.button
       {:on-click #(rf/dispatch [::events/show-congratulations-screen true])}
       "Congratulations"]]
     [:div.modal.animated.fadeIn
      {:class (when (and @completed (not (false? @show))) "is-active")}
      [:div.modal-background
       {:on-click #(rf/dispatch [::events/show-about false])}]
      [:div#about-screen.modal-card.has-text-centered.is-rounded
       {:style {:width "400px"}}
       [:header.modal-card-head
        [:p.modal-card-title.is-centered.animated.fadeInLeft "Congratulations!"]
        [:button.delete.is-medium
         {:aria-label "close"
          :on-click #(rf/dispatch [::events/show-congratulations-screen false])}]
        ]
       [:div.modal-card-body
        [:p
         " Level completed "
         [:i.fas.fa-heart.animated.pulse.anim-forever
          {:aria-hidden true
           :style {:color       "red"
                   :margin-left "3px"}}]]]
       [:footer.modal-card-foot
        [:div.buttons
         [:button.button "Next level"]
         [:button.button "Replay"]]]]]]))

(defn game []
  [:div
   [board]
   [:hr]
   [move-history]
   [level-complete-screen]])

(defn main-panel []
  [game])
