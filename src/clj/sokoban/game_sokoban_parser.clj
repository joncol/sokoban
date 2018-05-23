(ns sokoban.game-sokoban-parser
  (:require [clojure.string :as str]
            [clojure.zip :as z]
            [hickory.core :as h]
            [hickory.select :as s]))

(defn- parse-catalog-link
  "Parses a link to a catalog. Returns a map with keys `:id`, `:name`, `:size`,
  and `:url`."
  [link]
  (let [href    (->> link :attrs :href)
        cid     (Integer/parseInt (second (re-find #"cid=(\d+)" href)))
        content (-> link :content)
        name    (-> content first str/trim)
        size    (-> (s/select (s/descendant (s/and
                                             (s/tag :span)
                                             (s/attr :title)))
                              link)
                    first :content first (Integer/parseInt))]
    {:id   cid
     :name name
     :size size
     :url  (str "http://www.game-sokoban.com" href)}))

(defn extract-catalog-list
  "Parses the main page and returns a vector of all the catalogs."
  [body]
  (let [htree (-> body h/parse h/as-hickory)
        links (-> (s/select (s/descendant
                             (s/class :lblock)
                             (s/and (s/tag :a)
                                    (s/attr :href)
                                    (s/class :collection-link)))
                            htree))]
    (mapv parse-catalog-link links)))

(defn- parse-level-link
  "Parses a link to a level (class: `catalog-item1`). Returns a map with keys
  `:id` and `:name`."
  [link]
  (let [name (-> (s/select (s/child (s/and
                                     (s/tag :div)
                                     (s/class :top1)))
                           link)
                 first :content first)
        href (-> (s/select (s/child (s/and
                                     (s/tag :a)
                                     (s/attr :href)))
                           link)
                 first :attrs :href)
        lid  (Integer/parseInt (second (re-find #"lid=(\d+)" href)))]
    {:id   lid
     :name name}))

(defn extract-catalog-page-count
  "Parses a catalog page for the number of pages."
  [body]
  (let [htree (-> body h/parse h/as-hickory)
        pager (-> (s/select (s/child
                             (s/class :rblock)
                             (s/class :main-block)
                             (s/class :pager))
                            htree)
                  first)]
    (-> (s/select (s/child
                   (s/and
                    (s/tag :a)
                    (s/attr :href)))
                  pager)
        count
        inc ;; Since current page is not counted.
        )))

(defn extract-catalog
  "Parses a catalog page for all level links. Returns a vector of maps, where
  each map has keys `:id` and `:name`."
  [body]
  (let [htree (-> body h/parse h/as-hickory)
        links (-> (s/select (s/child
                             (s/class :rblock)
                             (s/class :main-block)
                             (s/class :catalog-item1))
                            htree))]
    (mapv parse-level-link links)))

(defn extract-level
  "Parses a level page and returns a vector of strings, where each string is a
  row of the level."
  [body]
  (let [htree (-> body h/parse h/as-hickory)
        code  (-> (s/select (s/child (s/has-child
                                      (s/and
                                       (s/tag :div)
                                       (s/class :header)
                                       (s/find-in-text #"Code:")))
                                     s/last-child
                                     (s/tag :pre))
                            htree)
                  first :content first)]
    (->> (str/split code #"\n")
         (mapv str/trimr))))
