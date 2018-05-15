(ns sokoban.events
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [sokoban.game-util :refer [block-positions make-move pad-vec]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(rf/reg-event-db
  ::level-changed
  (fn [db [_ level]]
    (let [width        (count (apply max-key count level))
          static-level (mapv (fn [row]
                               (pad-vec (str/replace row #"[@$]" " ")
                                        width))
                             level)]
      (-> db
          (assoc :static-level static-level
                 :target-positions (block-positions level ".")
                 :player-position-history [(first (block-positions level "@"))]
                 :movable-blocks-history [(block-positions level "$")]
                 :current-move 0)))))

(rf/reg-event-db
  ::make-move
  (fn [{:keys [static-level
               player-position-history
               movable-blocks-history
               current-move] :as db}
       [_ dir]]
    (let [pos (get player-position-history current-move)
          blocks (get movable-blocks-history current-move)
          [new-pos new-blocks] (make-move pos dir static-level blocks)]
      (if (= new-pos pos)
        db
        (-> db
            (update :player-position-history
                    #(conj (subvec % 0 (inc current-move)) new-pos))
            (update :movable-blocks-history
                    #(conj (subvec % 0 (inc current-move)) new-blocks))
            (update :current-move inc))))))

(rf/reg-event-db
  ::set-current-move
  [(rf/path [:current-move])]
  (fn [_ [_ value]]
    value))

(rf/reg-event-db
  ::update-current-move
  (fn [{:keys [player-position-history current-move] :as db} [_ f]]
    (let [i (f current-move)]
      (assoc db :current-move
             (if (< -1 i (count player-position-history))
               i
               current-move)))))

(rf/reg-event-db
  ::show-congratulations-screen
  [(rf/path [:show-congratulations-screen])]
  (fn [_ [_ show]]
    show))

(rf/reg-event-fx
  ::download-catalogs
  (fn [cofx _]
    {::download-catalogs-fx nil}))

(rf/reg-fx
  ::download-catalogs-fx
  (fn []
    (go (let [response (<! (http/get "/catalogs"))
              body     (-> response :body)]
          (if (:success response)
            (rf/dispatch [::download-catalogs-succeeded body])
            (rf/dispatch [::download-catalogs-failed body]))))))

(rf/reg-event-db
  ::download-catalogs-succeeded
  (fn [db [_ catalogs]]
    (assoc db
           :catalogs (zipmap (map :id catalogs) catalogs)
           :catalog-order (map :id catalogs)
           :current-catalog (first catalogs))))

(rf/reg-event-fx
  ::download-level
  (fn [_ [_ id]]
    {::download-level id}))

(rf/reg-fx
  ::download-level
  (fn [id]
    (go (let [response (<! (http/get (str "/level/" id)))
              body     (-> response :body)]
          (if (:success response)
            (rf/dispatch [::level-changed body])
            (rf/dispatch [::download-level-failed body]))))))
