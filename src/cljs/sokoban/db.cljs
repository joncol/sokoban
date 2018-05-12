(ns sokoban.db
  (:require [clojure.string :as str]
            [sokoban.slurp :include-macros true :refer [slurp]]
            [sokoban.board-util :refer [block-positions]]))

(def default-db
  (let [level (str/split (slurp "resources/level01.txt") #"\n")]
    {:static-level     (mapv #(vec (str/replace % #"[@$]" " ")) level)
     :target-positions (block-positions level ".")
     :player-pos       (first (block-positions level "@"))
     :movable-blocks   (block-positions level "$")}))
