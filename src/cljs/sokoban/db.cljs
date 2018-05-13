(ns sokoban.db
  (:require [clojure.string :as str]
            [sokoban.slurp :include-macros true :refer [slurp]]
            [sokoban.game-util :refer [block-positions]]))

(defn- pad-vec [v width]
  (let [n (count (take-while #(= " " %) v))]
    (vec (concat (repeat n "#") (drop n v) (repeat (- width (count v)) "#")))))

(def default-db
  (let [level (str/split (slurp "resources/level01.txt") #"\n")
        width (count (apply max-key count level))]
    {:static-level            (mapv (fn [row]
                                      (pad-vec (str/replace row #"[@$]" " ")
                                               width))
                                    level)
     :target-positions        (block-positions level ".")
     :player-position-history [(first (block-positions level "@"))]
     :movable-blocks-history  [(block-positions level "$")]
     :current-move            0}))
