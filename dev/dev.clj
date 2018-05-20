(ns dev
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application.

  Call `(reset)` to reload modified code and (re)start the system.

  The system under development is `system`, referred from
  `com.stuartsierra.component.repl/system`.

  See also https://github.com/stuartsierra/component.repl"
  (:require [clojure.java.io :as io]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.set :as set]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [clear refresh refresh-all]]
            [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl
             :refer [reset set-init start stop system]]
            [config.core :refer [env]]
            [sokoban.system :refer [new-system]]))

;; Do not try to load source code from 'resources' directory
(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

(defn dev-system
  "Constructs a system map suitable for interactive development."
  []
  (spec/check-asserts true)
  (new-system))

(set-init (fn [_] (dev-system)))
