(ns sokoban.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]
            [org.httpkit.client :as http-client]
            [ring.util.response :as resp]
            [sokoban.game-sokoban-parser :refer [extract-catalogs
                                                 extract-level]]
            [sokoban.middleware :refer [wrap-middleware]]))

(def catalog-list-cache (atom []))
(def level-cache (atom {}))

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
  (GET "/catalogs" _
    (if (seq @catalog-list-cache)
      (do
        (log/debug "Returning cached catalog list")
        (resp/response @catalog-list-cache))
      (let [resp @(http-client/get (str "http://www.game-sokoban.com/"
                                        "index.php?mode=catalog"))]
        (if (:error resp)
          (do (log/error (str "Failed to download catalogs from "
                              "http://www.game-sokoban.com: " (:error resp)))
              (resp/response resp))
          (let [catalogs (-> resp :body extract-catalogs)]
            (log/debug "Downloaded catalogs from http://www.game-sokoban.com")
            (reset! catalog-list-cache catalogs)
            (resp/response catalogs))))))
  (GET "/level/:id" [id]
    (if (contains? @level-cache id)
      (do
        (log/debug "Returning cached level:" id)
        (resp/response (@level-cache id)))
      (let [resp  @(http-client/get (str "http://www.game-sokoban.com/"
                                         "index.php?mode=level_info"
                                         "&view=general")
                                    {:query-params {:ulid id}})
            level (-> resp :body extract-level)]
        (log/debug "Downloaded level from http://www.game-sokoban.com:" id)
        (swap! level-cache assoc id level)
        (resp/response level))))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
