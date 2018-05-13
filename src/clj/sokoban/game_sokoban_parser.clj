(ns sokoban.game-sokoban-parser
  (:require [clojure.string :as str]
            [clojure.zip :as z]
            [hickory.core :as h]
            [hickory.select :as s]))

(defn- parse-catalog-link [link]
  (let [href    (->> link :attrs :href)
        cid     (Integer/parseInt (second (re-find #"cid=(\d+)" href)))
        content (-> link :content)
        name    (-> content first str/trim)
        size    (-> (s/select (s/descendant (s/and
                                             (s/tag :span)
                                             (s/attr :title)))
                              link)
                    first :content first)]
    {:id   cid
     :name name
     :size size
     :url  (str "http://www.game-sokoban.com" href)}))

(defn extract-catalogs [body]
  (let [body-htree (-> body h/parse h/as-hickory)
        links      (-> (s/select (s/descendant
                                  (s/class :lblock)
                                  (s/and (s/tag :a)
                                         (s/attr :href)
                                         (s/class :collection-link)))
                                 body-htree))]
    (map parse-catalog-link links)))

(defn extract-catalog [body]
  )

(defn extract-level [body]
  (let [body-htree (-> body h/parse h/as-hickory)
        code       (-> (s/select (s/child (s/has-child
                                           (s/and
                                            (s/tag :div)
                                            (s/class :header)
                                            (s/find-in-text #"Code:")))
                                          s/last-child
                                          (s/tag :pre))
                                 body-htree)
                       first :content first)]
    (->> (str/split code #"\n")
         (map str/trimr))))
