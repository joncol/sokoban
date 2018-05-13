(ns sokoban.game-util
  (:require [clojure.string :as str]))

(defn- find-value
  "Find indices of value matching predicate in sequence."
  [pred coll]
  (map first
       (filter #(pred (second %))
               (map-indexed vector coll))))

(defn block-positions [level block]
  (->> (for [y (find-value #(str/includes? % block) level)]
         (map #(-> [y %]) (find-value #(= % block) (get level y))))
       (apply concat)
       vec))

(defn- move-fn [dir]
  (case dir
    :left  (fn [[y x]] [y (dec x)])
    :right (fn [[y x]] [y (inc x)])
    :up    (fn [[y x]] [(dec y) x])
    :down  (fn [[y x]] [(inc y) x])
    identity))

(defn free-pos? [level movable-blocks pos]
  (and (not= "#" (get-in level pos))
       (not (some #(= pos %) movable-blocks))))

(defn make-move [pos dir level movable-blocks]
  (let [new-pos       ((move-fn dir) pos)
        movable-index (first (find-value #(= new-pos %) movable-blocks))]
    (if (= "#" (get-in level new-pos))
      [pos movable-blocks]
      (if (not movable-index)
        [new-pos movable-blocks]
        (if (free-pos? level movable-blocks ((move-fn dir) new-pos))
          [new-pos (update movable-blocks movable-index (move-fn dir))]
          [pos movable-blocks])))))
