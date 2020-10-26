(ns wheel-of-misfortune.domain.core
  (:require [wheel-of-misfortune.utils :refer [current-time shortid]]))

(defn make-scenario
  "Generate a new domain model for scenarios."
  [description]
  (let [created-at (current-time)
        id         (shortid (str created-at))]
    (merge description {:id id :created-at created-at})))
