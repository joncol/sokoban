(ns sokoban.subs
  (:require [re-frame.core :as rf]
            [clojure.set :as set]))

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
  ::current-catalog-id
  (fn [db]
    (:current-catalog-id db)))

(rf/reg-sub
  ::current-catalog-name
  (fn [_]
    [(rf/subscribe [::current-catalog-id])
     (rf/subscribe [::catalogs])])
  (fn [[id catalogs]]
    (get-in catalogs [id :name])))

(rf/reg-sub
  ::catalog-levels
  (fn [db]
    (:catalog-levels db)))

(rf/reg-sub
  ::current-catalog-levels
  (fn [_]
    [(rf/subscribe [::catalog-levels])
     (rf/subscribe [::current-catalog-id])])
  (fn [[catalog-levels catalog-id]]
    (get catalog-levels catalog-id)))

(rf/reg-sub
  ::current-level-id
  (fn [db]
    (:current-level-id db)))

(rf/reg-sub
  ::current-level-name
  (fn [_]
    [(rf/subscribe [::current-level-id])
     (rf/subscribe [::current-catalog-levels])])
  (fn [[id catalog]]
    (->> catalog
         (filter #(= id (:id %)))
         first
         :name)))

(rf/reg-sub
  ::next-unfinished-catalog-level-id
  (fn [_]
    [(rf/subscribe [::catalog-levels])
     (rf/subscribe [::current-catalog-id])])
  (fn [[catalog-levels catalog-id]]
    (->> (get catalog-levels catalog-id)
         (filter #(not (:finished %)))
         first
         :id)))

(rf/reg-sub
  ::catalog-dropdown-active
  (fn [db]
    (:catalog-dropdown-active db)))

(rf/reg-sub
  ::level-dropdown-active
  (fn [db]
    (:level-dropdown-active db)))

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
    (count (set/difference (set target-positions)
                           (set movable-blocks)))))

(rf/reg-sub
  ::level-finished-first-time
  (fn [db]
    (:level-finished-first-time db)))

(rf/reg-sub
  ::new-level-record
  (fn [db]
    (:new-level-record db)))

(rf/reg-sub
  ::level-finished
  (fn [_]
    [(rf/subscribe [::level-finished-first-time])
     (rf/subscribe [::new-level-record])])
  (fn [[finished record]]
    (or finished record)))

(rf/reg-sub
  ::level-state
  (fn [db]
    (:level-state db)))

(rf/reg-sub
  ::current-level-record
  (fn [_]
    [(rf/subscribe [::current-level-id])
     (rf/subscribe [::level-state])])
  (fn [[level-id level-state]]
    (get-in level-state [level-id :move-count])))

(rf/reg-sub
  ::history-size
  (fn [_]
    (rf/subscribe [::player-position-history]))
  (fn [player-position-history]
    (count player-position-history)))

(rf/reg-sub
  ::touch-start
  (fn [db]
    (:touch-start db)))

(rf/reg-sub
  ::touch-end
  (fn [db]
    (:touch-end db)))

(rf/reg-sub
  ::touch-delta
  (fn [_]
    [(rf/subscribe [::touch-start])
     (rf/subscribe [::touch-end])])
  (fn [[start end]]
    (when-let [et (first end)]
      (let [st      (->> start
                         (filter #(= (:id et) (:id %)))
                         first)
            [ex ey] [(:page-x et) (:page-y et)]
            [sx sy] [(:page-x st) (:page-y st)]
            dx      (- ex sx)
            dy      (- ey sy)]
        {:start-x sx
         :start-y sy
         :end-x   ex
         :end-y   ey
         :delta-x dx
         :delta-y dy
         :dist    (Math/sqrt (+ (* dx dx) (* dy dy)))}))))
