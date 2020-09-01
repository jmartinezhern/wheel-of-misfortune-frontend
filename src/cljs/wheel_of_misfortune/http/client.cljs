(ns wheel-of-misfortune.http.client
  (:require [cljs-http.client :as http]))

(defn fetch-scenarios
  [{:keys [tags]}]
  (http/get "http://localhost:3000/scenarios"
            {:with-credentials? false
             :headers {"Accept" "application/edn"}
             :query-params {"tags" tags
                            "order-by" "oldest"
                            "limit" 10}}))
