(ns sokoban.subs
  (:require [re-frame.core :as rf]))

;; This is a map.
(rf/reg-sub
  ::catalogs
  (fn [db]
    (:catalogs db)))

(rf/reg-sub
  ::catalog-order
  (fn [db]
    (:catalog-order db)))

(rf/reg-sub
  ::catalog-list
  (fn [_]
    [(rf/subscribe [::catalogs])
     (rf/subscribe [::catalog-order])])
  (fn [[catalogs order]]
    (mapv (partial get catalogs) order)))

(rf/reg-sub
  ::current-catalog
  (fn [db]
    (:current-catalog db)))

(rf/reg-sub
  ::static-level
  (fn [db]
    (:static-level db)))

(rf/reg-sub
  ::player-position-history
  (fn [db]
    (:player-position-history db)))

(rf/reg-sub
  ::movable-blocks-history
  (fn [db]
    (:movable-blocks-history db)))

(rf/reg-sub
  ::target-positions
  (fn [db]
    (:target-positions db)))

(rf/reg-sub
  ::current-move
  (fn [db]
    (:current-move db)))

(rf/reg-sub
  ::show-congratulations-screen
  (fn [db]
    (:show-congratulations-screen db)))

(rf/reg-sub
  ::player-pos
  (fn [_]
    [(rf/subscribe [::player-position-history])
     (rf/subscribe [::current-move])])
  (fn [[player-position-history current-move]]
    (get player-position-history current-move)))

(rf/reg-sub
  ::movable-blocks
  (fn [_]
    [(rf/subscribe [::movable-blocks-history])
     (rf/subscribe [::current-move])])
  (fn [[movable-bloc-history current-move]]
    (get movable-bloc-history current-move)))

(rf/reg-sub
  ::level
  (fn [_]
    [(rf/subscribe [::static-level])
     (rf/subscribe [::player-pos])
     (rf/subscribe [::movable-blocks])
     (rf/subscribe [::target-positions])])
  (fn [[static-level player-pos movable-blocks target-positions]]
    (-> static-level
        (as-> l
            (reduce (fn [l block-pos]
                      (assoc-in l block-pos
                                (if (some #(= block-pos %) target-positions)
                                  "*"
                                  "$")))
                    l movable-blocks))
        (assoc-in player-pos "@"))))

(rf/reg-sub
  ::remaining-count
  (fn [_]
    [(rf/subscribe [::movable-blocks])
     (rf/subscribe [::target-positions])])
  (fn [[movable-blocks target-positions]]
    (->> target-positions
         (remove #(some (fn [p] (= p %)) movable-blocks))
         count)))

(rf/reg-sub
  ::level-completed
  (fn [_]
    (rf/subscribe [::remaining-count]))
  (fn [remaining-count]
    (zero? remaining-count)))

(rf/reg-sub
  ::history-size
  (fn [_]
    (rf/subscribe [::player-position-history]))
  (fn [player-position-history]
    (count player-position-history)))
