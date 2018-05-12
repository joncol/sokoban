(ns sokoban.events
  (:require [re-frame.core :as rf]
            [sokoban.db :as db]))

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
