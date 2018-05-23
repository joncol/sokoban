(ns sokoban.game-util
  (:require [clojure.string :as str]
            [sokoban.util :refer [find-value-indices]]))

(defn pad-vec [v width]
  (let [n (count (take-while #(= " " %) v))]
    (vec (concat (repeat n "#") (drop n v) (repeat (- width (count v)) "#")))))

(defn block-positions [level block]
  (->> (for [y (find-value-indices #(str/includes? % block) level)]
         (map #(-> [y %]) (find-value-indices #(= % block) (get level y))))
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
        movable-index (first (find-value-indices #(= new-pos %)
                                                 movable-blocks))]
    (if (= "#" (get-in level new-pos))
      [pos movable-blocks]
      (if (not movable-index)
        [new-pos movable-blocks]
        (if (free-pos? level movable-blocks ((move-fn dir) new-pos))
          [new-pos (update movable-blocks movable-index (move-fn dir))]
          [pos movable-blocks])))))

(defn elem-center
  "Return the center of the bounding rect of a DOM element with a given ID."
  [id]
  (let [elem (js/document.getElementById id)
        rect (.getBoundingClientRect elem)
        x    (+ (.-x rect) (/ (.-width rect) 2))
        y    (+ (.-y rect) (/ (.-height rect) 2))]
    [x y]))
