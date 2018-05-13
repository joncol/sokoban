(ns sokoban.events
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [sokoban.game-util :refer [block-positions make-move pad-vec]]))

(rf/reg-event-db
  ::level-changed
  (fn [db [_ level]]
    (let [width        (count (apply max-key count level))
          static-level (mapv (fn [row]
                               (pad-vec (str/replace row #"[@$]" " ")
                                        width))
                             level)]
      (-> db
          (assoc :static-level static-level
                 :target-positions (block-positions level ".")
                 :player-position-history [(first (block-positions level "@"))]
                 :movable-blocks-history [(block-positions level "$")]
                 :current-move 0)))))

(rf/reg-event-db
  ::make-move
  (fn [{:keys [static-level
               player-position-history
               movable-blocks-history
               current-move] :as db}
       [_ dir]]
    (let [pos (get player-position-history current-move)
          blocks (get movable-blocks-history current-move)
          [new-pos new-blocks] (make-move pos dir static-level blocks)]
      (if (= new-pos pos)
        db
        (-> db
            (update :player-position-history
                    #(conj (subvec % 0 (inc current-move)) new-pos))
            (update :movable-blocks-history
                    #(conj (subvec % 0 (inc current-move)) new-blocks))
            (update :current-move inc))))))

(rf/reg-event-db
  ::set-current-move
  [(rf/path [:current-move])]
  (fn [_ [_ value]]
    value))

(rf/reg-event-db
  ::update-current-move
  (fn [{:keys [player-position-history current-move] :as db} [_ f]]
    (let [i (f current-move)]
      (assoc db :current-move
             (if (< -1 i (count player-position-history))
               i
               current-move)))))

(rf/reg-event-db
  ::show-congratulations-screen
  [(rf/path [:show-congratulations-screen])]
  (fn [_ [_ show]]
    show))
