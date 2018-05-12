(ns sokoban.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::active-panel
  (fn [db _]
    (:active-panel db)))

(rf/reg-sub
  ::level
  (fn [db]
    (:level db)))
