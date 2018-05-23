(ns sokoban.events
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cljs.reader :refer [read-string]]
            [clojure.set :as set]
            [clojure.string :as str]
            [re-frame.core :as rf]
            [sokoban.db :as db]
            [sokoban.game-util :refer [block-positions make-move pad-vec]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def level-state-key "level-state")

(rf/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(rf/reg-event-db
  ::set-active-panel
  [(rf/path [:active-panel])]
  (fn [_ [_ new-value]]
    new-value))

(rf/reg-event-db
  ::load-level-state
  [(rf/path [:level-state])]
  (fn [_ _]
    (read-string (.getItem js/localStorage level-state-key))))

(rf/reg-event-fx
  ::make-move
  (fn [{:keys [db]} [_ dir]]
    (let [{:keys [static-level
                  level-finished-first-time
                  new-level-record
                  target-positions
                  player-position-history
                  movable-blocks-history
                  current-move
                  current-level-id
                  current-catalog-id
                  level-state]} db]
      (when (and (seq static-level)
                 (not level-finished-first-time)
                 (not new-level-record))
        (let [pos (get player-position-history current-move)
              blocks (get movable-blocks-history current-move)
              [new-pos new-blocks] (make-move pos dir static-level blocks)
              p-pos-h (-> db
                          :player-position-history
                          (as-> h
                              (conj (subvec h 0 (inc current-move)) new-pos)))
              m-b-h (-> db
                        :movable-blocks-history
                        (as-> h
                            (conj (subvec h 0 (inc current-move)) new-blocks)))
              remaining-count (count (set/difference (set target-positions)
                                                     (set new-blocks)))]
          (when-not (= new-pos pos)
            (merge {:db (-> db
                            (assoc :player-position-history p-pos-h)
                            (assoc :movable-blocks-history m-b-h)
                            (update :current-move inc))}
                   (when (zero? remaining-count)
                     {::level-finished [level-state current-level-id
                                        current-catalog-id
                                        p-pos-h m-b-h]}))))))))

(rf/reg-fx
  ::level-finished
  (fn [[level-state id catalog-id p-pos-h m-b-h]]
    (let [move-count (dec (count p-pos-h))
          status (get level-state id)
          congrats? (or (nil? status) (< move-count (:move-count status)))]
      (when congrats?
        (rf/dispatch [(if status ::new-level-record ::level-finished-first-time)
                      id move-count])
        (-> level-state
            (assoc id {:player-position-history p-pos-h
                       :movable-blocks-history  m-b-h
                       :move-count              move-count})
            str
            (as-> data
                (.setItem js/localStorage level-state-key data)))
        (rf/dispatch [::load-level-state])
        (rf/dispatch [::update-finished-catalog-levels catalog-id])))))

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
  ::level-finished-first-time
  [(rf/path [:level-finished-first-time])]
  (fn [_ [_ id move-count]]
    move-count))

(rf/reg-event-db
  ::new-level-record
  [(rf/path [:new-level-record])]
  (fn [_ [_ id move-count]]
    move-count))

(rf/reg-event-db
  ::close-congratulations-screen
  (fn [db [_ _]]
    (dissoc db :level-finished-first-time :new-level-record)))

(rf/reg-event-fx
  ::download-catalogs
  (fn [_ _]
    {::download-catalogs nil}))

(rf/reg-fx
  ::download-catalogs
  (fn []
    (go (let [response (<! (http/get "/catalogs"))
              body     (-> response :body)]
          (if (:success response)
            (rf/dispatch [::download-catalogs-succeeded body])
            (rf/dispatch [::download-catalogs-failed body]))))))

(rf/reg-event-fx
  ::download-catalogs-succeeded
  (fn [{:keys [db]} [_ catalogs]]
    {:db (assoc db
                :catalogs (zipmap (map :id catalogs) catalogs)
                :catalog-order (map :id catalogs))
     :dispatch [::set-catalog (-> catalogs first :id)]}))

(rf/reg-event-fx
  ::set-catalog
  (fn [{:keys [db]} [_ id]]
    (-> {:db (assoc db :current-catalog-id id)}
        (as-> fx
            (if-let [levels (get-in db [:catalog-levels id])]
              (assoc fx ::download-level (-> levels first :id))
              (assoc fx ::download-catalog-levels id))))))

(rf/reg-fx
  ::download-catalog-levels
  (fn [id]
    (go (let [response (<! (http/get (str "/catalog/" id)))
              body     (-> response :body)]
          (if (:success response)
            (rf/dispatch [::download-catalog-levels-succeeded id body])
            (rf/dispatch [::download-catalog-levels-failed id body]))))))

(rf/reg-event-fx
  ::download-catalog-levels-succeeded
  (fn [{:keys [db]} [_ catalog-id levels]]
    {:db (assoc-in db [:catalog-levels catalog-id] levels)
     ::download-level (-> levels first :id)
     :dispatch-n [[::toggle-catalog-dropdown-active false]
                  [::toggle-level-dropdown-active false]
                  [::update-finished-catalog-levels catalog-id]]}))

(rf/reg-event-db
  ::update-finished-catalog-levels
  (fn [db [_ catalog-id]]
    (update-in db [:catalog-levels catalog-id]
               (fn [levels]
                 (map #(assoc % :finished (get-in db [:level-state (:id %)]))
                      levels)))))

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
            (rf/dispatch [::download-level-succeeded id body])
            (rf/dispatch [::download-level-failed id body]))))))

(defn- get-level-state [level-state id]
  (-> level-state
      (get id)
      (set/rename-keys {:move-count :current-move})))

(rf/reg-event-fx
  ::download-level-succeeded
  (fn [{:keys [db]} [_ id level]]
    (let [width        (count (apply max-key count level))
          static-level (mapv (fn [row]
                               (-> row
                                   (str/replace #"[@$]" " ")
                                   (str/replace #"[*+]" ".")
                                   (pad-vec width)))
                             level)]
      {:db (-> db
               (assoc :current-level-id id
                      :static-level static-level
                      :target-positions (vec (block-positions static-level "."))
                      :player-position-history
                      [(or (first (block-positions level "@"))
                           (first (block-positions level "+")))]
                      :movable-blocks-history [(vec (concat
                                                     (block-positions level "$")
                                                     (block-positions level "*")))]
                      :current-move 0)
               (merge (get-level-state (:level-state db) id)))
       :dispatch-n [[::toggle-level-dropdown-active false]
                    [::toggle-catalog-dropdown-active false]]})))

(rf/reg-event-db
  ::toggle-catalog-dropdown-active
  (fn [db [_ value]]
    (update db :catalog-dropdown-active
            (fn [x] (if (some? value)
                      value
                      (not x))))))

(rf/reg-event-db
  ::toggle-level-dropdown-active
  (fn [db [_ value]]
    (update db :level-dropdown-active
            (fn [x] (if (some? value)
                      value
                      (not x))))))

(rf/reg-event-db
  ::touch-start
  (fn [db [_ event]]
    (-> db
        (assoc :touch-start event)
        (assoc :touch-end nil))))

(rf/reg-event-db
  ::touch-end
  [(rf/path [:touch-end])]
  (fn [_ [_ event]]
    event))

(rf/reg-event-db
  ::clear-touch
  (fn [db _]
    (-> db
        (assoc :touch-start nil)
        (assoc :touch-end nil))))
