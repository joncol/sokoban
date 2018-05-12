(ns sokoban.board-util
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
       (apply concat)))

(defn- move-fn [dir]
  (case dir
    :left  (fn [[y x]] [y (dec x)])
    :right (fn [[y x]] [y (inc x)])
    :up    (fn [[y x]] [(dec y) x])
    :down  (fn [[y x]] [(inc y) x])
    identity))

(defn move-player [pos dir level movable-blocks]
  [((move-fn dir) pos) movable-blocks])
