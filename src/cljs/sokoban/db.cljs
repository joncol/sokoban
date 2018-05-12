(ns sokoban.db
  (:require [clojure.string :as str]
            [sokoban.slurp :include-macros true :refer [slurp]]))

(def default-db
  {:name "sokoban"
   :level (str/split (slurp "resources/level01.txt") #"\n")})
