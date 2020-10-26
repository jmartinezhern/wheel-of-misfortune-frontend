(ns wheel-of-misfortune.scenarios.core
  (:require [wheel-of-misfortune.domain.core :as d]
            [wheel-of-misfortune.repositories.files
             [core :as frepo]
             [local :as files]]
            [wheel-of-misfortune.repositories.scenarios
             [core :as srepo]
             [sql :as sql]]))

(def scenarios-repo (sql/->SQLRepository))

(def files-repo (files/->FilesRepository
                 (or (System/getenv "ROOT_VOLUME_PATH")
                     "/tmp/wheel-of-misfortune")))

(defn- query->page [{:keys [order-by] :as page}]
  (when (not= order-by nil)
    (assoc page :order-by [(condp = order-by
                             "newest"     [:created-at :desc]
                             "oldest"     [:created-at :asc]
                             "popularity" [:score :desc])])))

(defn save!
  "Save a scenario and return information to be able to fetch it later"
  [request]
  (let [{:keys [id
                name
                created-at
                description
                tags]
         :as   scenario} (d/make-scenario request)]
    (frepo/save! files-repo (assoc scenario :path (str id ".edn")))
    (srepo/save! scenarios-repo {:id          id
                                 :name        name
                                 :description description
                                 :created-at  created-at})
    (when-not (nil? tags) (srepo/assoc-tags! scenarios-repo
                                             id
                                             tags))
    scenario))

(defn retrieve
  "Retrieve a scenario by id"
  [id]
  (frepo/retrieve files-repo (str id ".edn")))

(defn subset
  "Return a subset of scenarios based on a filter"
  [{:keys [tags] :as query}]
  (let [page (query->page query)]
    (if-not (nil? tags)
      (srepo/subset-by-tags scenarios-repo tags page)
      (srepo/subset scenarios-repo page))))

(defn inc-score!
  "Increment score for a scenario by one.
  Returns true if the scenario was modified,
  false otherwise"
  [id]
  (srepo/inc! scenarios-repo id :score 1))
