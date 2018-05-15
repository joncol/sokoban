(ns sokoban.views
  (:require [re-frame.core :as rf]
            [sokoban.events :as events]
            [sokoban.subs :as subs]))

(def cell-size 36)

(defn fa-icon [icon-class size color]
  [:span.icon {:style {:font-size size}}
   [:i.fas {:class       icon-class
            :style       {:color color}
            :aria-hidden true}]])

(defn cell [cell-type]
  [:div.cell.button.is-static
   {:class (case cell-type
             "#" "wall"
             "@" "player"
             "." "target"
             "*" "target-complete"
             "$" "movable-block"
             nil)
    :style {:width cell-size
            :height cell-size}}
   (case cell-type
     "." [fa-icon "fa-expand" "20px" "#67809f"]
     "*" [fa-icon ["fa-trophy" "animated" "pulse"
                   "anim-forever"] "28px" "#f9bf3b"]
     "$" [fa-icon "fa-trophy" "28px" "#f9bf3b"]
     "@" [fa-icon "fa-bug" "30px" "#013243"]
     "#" nil
     cell-type)])

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
  (let [current-level-id (rf/subscribe [::subs/current-level-id])
        history-size     (rf/subscribe [::subs/history-size])
        current-move     (rf/subscribe [::subs/current-move])]
    [:div.has-text-centered
     {:style {:visibility (when-not @current-level-id "hidden")
              :width "280px"}}
     [:label (str "Current move: " @current-move " ")]
     [:div
      [:span.icon.button.is-medium.is-rounded
       {:on-click #(rf/dispatch [::events/set-current-move 0])}
       [:i.fas.fa-fast-backward {:aria-hidden true}]]
      [:span.icon.button.is-medium.is-rounded
       {:on-click #(rf/dispatch [::events/update-current-move dec])}
       [:i.fas.fa-step-backward {:aria-hidden true}]]
      [:span.icon.button.is-medium.is-rounded
       {:on-click #(rf/dispatch [::events/update-current-move inc])}
       [:i.fas.fa-step-forward {:aria-hidden true}]]
      [:span.icon.button.is-medium.is-rounded
       {:on-click #(rf/dispatch [::events/set-current-move
                                 (dec @history-size)])}
       [:i.fas.fa-fast-forward {:aria-hidden true}]]]
     [:input.slider.is-fullwidth.is-medium.is-circle.is-white
      {:type     "range"
       :min      0
       :max      (dec @history-size)
       :value    (or @current-move "")
       :on-input #(rf/dispatch [::events/set-current-move
                                (-> % .-target .-value int)])}]]))

(defn level-complete-screen []
  (let [completed (rf/subscribe [::subs/level-completed])
        show      (rf/subscribe [::subs/show-congratulations-screen])]
    [:div
     [:div
      {:style {:visibility (when (not @completed) "hidden")}}
      [:button.button
       {:on-click #(rf/dispatch [::events/show-congratulations-screen true])}
       "Congratulations!"]]
     [:div.modal.animated.fadeIn
      {:class (when (and @completed (not (false? @show))) "is-active")}
      [:div.modal-background
       {:on-click #(rf/dispatch [::events/show-congratulations-screen false])}]
      [:div#about-screen.modal-card.has-text-centered.is-rounded
       {:style {:width "400px"}}
       [:header.modal-card-head
        [:p.modal-card-title.is-centered.animated.fadeInLeft "Congratulations!"]
        [:button.delete.is-medium
         {:aria-label "close"
          :on-click #(rf/dispatch
                      [::events/show-congratulations-screen false])}]]
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

(defn catalog-dropdown []
  (let [catalogs (rf/subscribe [::subs/catalog-list])]
    (when (seq @catalogs)
      [:div.field.is-horizontal.control
       [:div.field-label.is-normal
        [:label.label {:style {:white-space "nowrap"}} "Level pack"]]
       [:div.field-body
        [:div.field.is-narrow
         [:div.control
          [:div.select
           [:select {:on-change #(rf/dispatch [::events/set-catalog
                                               (-> % .-target .-value)])}
            (for [c @catalogs]
              ^{:key (:id c)}[:option {:value (:id c)} (:name c)])]]]]]])))

(defn level-dropdown []
  (let [levels (rf/subscribe [::subs/current-catalog-levels])]
    [:div.field.is-horizontal.control
     [:div.field-label.is-normal
      [:label.label {:style {:white-space "nowrap"}} "Level"]]
     [:div.field-body
      [:div.field.is-narrow
       [:div.control
        [:div.select
         [:select {:on-change #(rf/dispatch [::events/download-level
                                             (-> % .-target .-value)])}
          (for [l @levels]
            ^{:key (:id l)}[:option {:value (:id l)} (:name l)])]]]]]]))

(defn- level-selection []
  [:div
   [catalog-dropdown]
   [level-dropdown]])

(defn game []
  [:div
   [board]
   [move-history]
   [level-selection]
   [level-complete-screen]])

(defn main-panel []
  [game])
