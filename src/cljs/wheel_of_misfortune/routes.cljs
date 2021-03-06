(ns wheel-of-misfortune.routes
  "Route information for the web application."
  (:require
   [reitit.frontend :as reitit]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/app"
     ["/" :index]
     ["/search" :search]
     ["/upload" :upload]
     ["/scenarios"
      ["/:scenario-id/complete" :complete]
      ["/:scenario-id" :scenario]]
     ["/about" :about]]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))
