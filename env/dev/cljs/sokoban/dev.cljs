(ns ^:figwheel-no-load sokoban.dev
  (:require [devtools.core :as devtools]
            [sokoban.core :as core]))

(devtools/install!)

(enable-console-print!)

(core/init!)
