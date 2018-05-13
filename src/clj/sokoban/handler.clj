(ns sokoban.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]
            [org.httpkit.client :as http-client]
            [ring.util.response :as resp]
            [sokoban.game-sokoban-parser :refer [extract-level]]
            [sokoban.middleware :refer [wrap-middleware]]))

(def mount-target
  [:div#app])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")]))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/level/:id" [id]
    (let [resp @(http-client/get (str "http://www.game-sokoban.com/"
                                      "index.php?mode=level_info&view=general")
                                 {:query-params {:ulid id}})]
      (resp/response (-> resp :body extract-level))))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
