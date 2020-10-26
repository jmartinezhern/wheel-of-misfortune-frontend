(ns wheel-of-misfortune.routes.app
  (:require [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]
            reitit.coercion.spec
            ))

(def mount-target
  [:div#app
   [:h2 "Welcome to wheel-of-misfortune"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")]))

(defn index-handler
  [_request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (loading-page)})


(def app-routes
  [["/app"
    ["/" {:get {:handler index-handler}}]
    ["/search" {:get {:handler index-handler}}]
    ["/browse" {:get {:handler index-handler}}]
    ["/upload" {:get {:handler index-handler}}]
    ["/scenarios"
     ["" {:get {:handler index-handler}}]
     ["/:scenario-id/complete" {:get {:handler    index-handler
                                      :parameters {:path  {:scenario-id string?}
                                                   :query {:won boolean?}}}}]
     ["/:scenario-id" {:get {:handler    index-handler
                             :parameters {:path {:scenario-id string?}}}}]]
    ["/about" {:get {:handler index-handler}}]]])
