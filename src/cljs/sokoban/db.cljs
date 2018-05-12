(ns sokoban.db
  (:require [clojure.string :as str]
            [sokoban.slurp :include-macros true :refer [slurp]]
            [sokoban.util :refer [find-value]]))

(defn block-positions [level block]
  (->> (for [y (find-value #(str/includes? % block) level)]
         (map #(-> [y %]) (find-value #(= % block) (get level y))))
       (apply concat)))

(def default-db
  (let [level (str/split (slurp "resources/level01.txt") #"\n")]
    {:name           "sokoban"
     :static-level   (mapv #(vec (str/replace % #"[@$]" " ")) level)
     :player-pos     (first (block-positions level "@"))
     :movable-blocks (block-positions level "$")}))
