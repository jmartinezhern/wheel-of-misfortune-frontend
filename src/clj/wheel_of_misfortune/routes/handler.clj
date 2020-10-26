(ns wheel-of-misfortune.routes.handler
  (:require [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [ring.middleware.params :as params]
            [wheel-of-misfortune.routes
             [api :refer [api-routes]]
             [app :refer [app-routes]]]
            [wheel-of-misfortune.middleware :refer [middleware]]))

(def app
  (ring/ring-handler
   (ring/router
    (concat
     api-routes
     app-routes)
    {:data {:muuntaja   m/instance
            :middleware [params/wrap-params
                         muuntaja/format-middleware
                         coercion/coerce-exceptions-middleware
                         coercion/coerce-request-middleware
                         coercion/coerce-response-middleware]}})
   (ring/routes
    (ring/create-resource-handler {:path "/app/assets" :root "/public"})
    (ring/create-default-handler
     {:not-found (constantly {:status  404
                              :body    "Not found"
                              :headers {"Content-Type" "application/html"}})}))
   {:middleware middleware}))
