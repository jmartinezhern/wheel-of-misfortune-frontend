(ns wheel-of-misfortune.routes.api
  (:require [clojure.spec.alpha :as s]
            [clojure.string :refer [split starts-with?]]
            reitit.coercion.spec
            [spec-tools
             [core :as st]
             [spec :as sts]]
            [wheel-of-misfortune.scenarios
             [core :as scenarios]
             [spec :as ss]]))

(s/def ::limit sts/int?)
(s/def ::order-by (st/spec #{"newest" "oldest" "popularity"}))
(s/def ::tags sts/string?)

(defn- tags-param->keywords
  [param]
  (let [mapping (group-by #(starts-with? % "-") (split param #"\+"))]
    {:included (mapv keyword (get mapping false))
     :excluded (mapv #(keyword (subs % 1)) (get mapping true))}))

(defn- handle-create-req
  [{{:keys [body]} :parameters}]
  {:status 201
   :body   (scenarios/save! body)})

(defn- handle-subset-req
  [{{{:keys [tags] :as page} :query} :parameters}]
  {:status 200
   :body   (scenarios/subset
            (if-not (empty? tags)
              (assoc page :tags (tags-param->keywords tags))
              (dissoc page :tags)))})

(defn- handle-inc-score-req
  [{{{:keys [scenario-id]} :path} :parameters}]
  {:status (if (scenarios/inc-score! scenario-id) 200 404)})

(defn- handle-retrieve-req
  [{{{:keys [id]} :path} :parameters}]
  (let [scenario (scenarios/retrieve id)]
    (if-not (nil? scenario)
      {:status 200 :body scenario}
      {:status 400})))

(def api-routes
  [["/api" {:coercion reitit.coercion.spec/coercion}
    ["/scenarios" {:coercion reitit.coercion.spec/coercion}
     ["" {:post {:summary    "create a scenario"
                 :parameters {:body ::ss/request}
                 :response   {201 {:body ::ss/scenario}}
                 :handler    handle-create-req}
          :get  {:summary    "list scenarios"
                 :parameters {:query (s/keys :req-un [::order-by ::limit]
                                             :opt-un [::tags])}
                 :response   {200 {:body (s/coll-of ::ss/details :into [])}}
                 :handler    handle-subset-req}}]
     ["/:id" {:parameters {:path (s/keys :req-un [::ss/id])}}
      ["" {:get {:summary  "retrieve a scenario by id"
                 :response {200 {:body ::ss/scenario}}
                 :handler  handle-retrieve-req}}]

      ["/score" {:put {:summary "increment score"
                       :handler handle-inc-score-req}}]]]]])
