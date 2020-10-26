(ns ^{:clj-kondo/config '{:linters
                          {:unresolved-symbol
                           {:exclude [Scenario Tag this types]}}}}
 wheel-of-misfortune.repositories.scenarios.models
  (:require [toucan.models :refer [defmodel IModel]]))

(defmodel Scenario :scenarios
  IModel
  (types [this]
    {:upload-status :keyword}))

(defmodel Tag :tags)
