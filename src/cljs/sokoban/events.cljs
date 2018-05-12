(ns sokoban.events
  (:require [re-frame.core :as rf]
            [sokoban.db :as db]
            [sokoban.board-util :refer [move-player]]))

(rf/reg-event-db
  ::initialize-db
  (fn [_ _]
   db/default-db))

(rf/reg-event-db
  ::set-active-panel
  (fn [db [_ active-panel]]
    (assoc db :active-panel active-panel)))

(rf/reg-event-db
  ::set-name
  (fn [db [_ name]]
    (assoc db :name name)))

(rf/reg-event-db
  ::move-player
  (fn [{:keys [static-level player-pos movable-blocks] :as db} [_ dir]]
    (let [[pos blocks] (move-player player-pos dir static-level movable-blocks)]
      (-> db
          (assoc :player-pos pos)
          (assoc :movable-blocks movable-blocks)))))
