(ns sokoban.util)

(defn find-value
  "Find indices of value matching predicate in sequence."
  [pred coll]
  (map first
       (filter #(pred (second %))
               (map-indexed vector coll))))
