(ns sokoban.views
  (:require [re-frame.core :as rf]
            [sokoban.events :as events]
            [sokoban.game-util :refer [elem-center]]
            [sokoban.subs :as subs]))

(def cell-size 36)

(defn fa-icon [icon-class size color & [id]]
  [:span.icon {:id    id
               :style {:font-size size}}
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
     "@" [fa-icon "fa-bug" "30px" "#013243" "player"]
     "#" nil
     cell-type)])

(defn move-along-major-axis [dx dy]
  (if (< (Math/abs dx) (Math/abs dy))
    (if (pos? dy)
      (rf/dispatch [::events/make-move :down])
      (rf/dispatch [::events/make-move :up]))
    (if (pos? dx)
      (rf/dispatch [::events/make-move :right])
      (rf/dispatch [::events/make-move :left]))))

(def drag-threshold 50)

(defn handle-touch [delta]
  (when-let [board-rect (some-> (js/document.getElementById "board")
                                .getBoundingClientRect)]
    (let [board-bottom (+ (.-y board-rect) (.-height board-rect))]
      (when-let [{:keys [delta-x delta-y end-x end-y dist]} @delta]
        (when (< end-y board-bottom)
          (if (< dist drag-threshold)
            (let [[px py] (elem-center "player")
                  [dx dy] [(- end-x px) (- end-y py)]]
              (move-along-major-axis dx dy))
            (move-along-major-axis delta-x delta-y)))
        (rf/dispatch [::events/clear-touch])))))

(defn- copy-touch-event [event]
  (doall (for [touch (.-changedTouches event)]
           {:id (.-identifier touch)
            :page-x (.-pageX touch)
            :page-y (.-pageY touch)})))

(defn board []
  (let [level (rf/subscribe [::subs/level])
        delta (rf/subscribe [::subs/touch-delta])]
    (handle-touch delta)
    [:div#board
     {:on-touch-start #(rf/dispatch [::events/touch-start (copy-touch-event %)])
      :on-touch-end   #(rf/dispatch [::events/touch-end (copy-touch-event %)])}
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
    [:div
     {:style {:visibility (when-not @current-level-id "hidden")
              :width      "280px"}}
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
  [:div {:style {:width "540px"}}
   [catalog-dropdown]
   [level-dropdown]])

(defn game []
  [:div#game
   [board]
   [move-history]
   [level-selection]
   [level-complete-screen]])

(defn main-panel []
  [game])
