(ns wheel-of-misfortune.repositories.scenarios.sql
  (:require [honeysql
             [core :as hsql]
             [helpers :as h]]
            [toucan.db :as db]
            [wheel-of-misfortune.repositories.scenarios
             [core :as core]
             [models :as models]]))

(defn- append-table-to-keyword
  [prefix [k v]]
  [(keyword (str prefix (name k))) v])

(defn- keywords->strings [keywords]
  (mapv name keywords))

(defn- tags->insert-values [scenario-id tags]
  (h/values
   (h/insert-into :scenario_tags)
   (map (fn [tag] {:tag tag :scenario-id scenario-id})
        (keywords->strings tags))))

(defn- make-tags-where-clause
  [{:keys [included excluded]}]
  (let [inc-strs (keywords->strings included)
        exc-strs (keywords->strings excluded)]
    (cond
      (and (seq included)
           (seq excluded)) [:and [:in :t.tag inc-strs]
                            [:not-in :t.tag exc-strs]]
      (seq included)       [:in :t.tag inc-strs]
      (seq excluded)       [:not-in :t.tag exc-strs])))

(deftype SQLRepository []
  core/ScenariosRepository

  (save!
    [_ scenario]
    (db/insert! models/Scenario scenario))

  (update!
    [_ id item]
    (db/update! models/Scenario id item))

  (inc!
    [_ id k v]
    (db/update! models/Scenario id k (hsql/call :+ k v)))

  (retrieve
    [_ id]
    (into {} (models/Scenario id)))

  (subset
    [_ page]
    (vec (map (partial into {}) (db/select models/Scenario page))))

  (subset-by-tags
    [_ tags {:keys [limit order-by]}]
    (vec (db/query
          (h/limit
           (apply
            h/order-by
            (-> (h/select :s.* :t.tag)
                (h/from [:scenarios :s])
                (h/join [:scenario_tags :t]
                        [:= :t.scenario-id :s.id])
                (h/where (make-tags-where-clause tags)))
            (mapv (partial append-table-to-keyword "s.") order-by))))))

  (assoc-tags!
    [_ scenario-id tags]
    (db/execute! (tags->insert-values scenario-id tags))))
