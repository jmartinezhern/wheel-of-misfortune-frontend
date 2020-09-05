(ns wheel-of-misfortune.http.client
  (:require [cljs-http.client :as http]
            [clojure.walk :refer [stringify-keys]]))

(defn fetch-scenarios
  [params]
  (http/get "http://localhost:3000/scenarios"
            {:with-credentials? false
             :headers {"Accept" "application/edn"}
             :query-params (stringify-keys params)}))


(defn fetch-scenario
  [id]
  (http/get (str "http://localhost:3000/scenarios/" id)
            {:with-credentials? false?
             :headers {"Accept" "application/edn"}}))
