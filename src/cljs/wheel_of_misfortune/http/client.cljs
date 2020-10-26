(ns wheel-of-misfortune.http.client
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [>! <! chan go]]
            [clojure.walk :refer [stringify-keys]]))

(def ^:dynamic *base-url* "http://localhost:3000/api/scenarios")

(def ^:private base-ops {:with-credentials? false
                         :headers {"Accept" "application/edn"}})

(defn fetch-scenarios
  [params]
  (let [ops (assoc base-ops :query-params (stringify-keys params))
        out (chan)]
    (go (>! out (:body (<! (http/get *base-url* ops)))))
    out))

(defn fetch-scenario
  [id]
  (let [out (chan)]
    (go
      (>! out (:body (<! (http/get (str *base-url* "/" id) base-ops)))))
    out))
