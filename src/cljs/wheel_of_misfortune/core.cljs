(ns wheel-of-misfortune.core
  (:require [accountant.core :as accountant]
            [clerk.core :as clerk]
            [reagent.core :as reagent]
            [reagent.dom :as rdom]
            [reagent.session :as session]
            [reitit.frontend :as reitit]
            [wheel-of-misfortune.components.main
             :refer
             [about-page home-page upload]]
            [wheel-of-misfortune.components.complete
             :as complete]
            [wheel-of-misfortune.components.scenario
             :as scenario]
            [wheel-of-misfortune.components.scenarios :as scenarios]
            [wheel-of-misfortune.routes :refer [router]]))

;; ------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index    #'home-page
    :upload   #'upload
    :search   #'scenarios/search
    :about    #'about-page
    :complete #'complete/page
    :scenario #'scenario/page))

;; -------------------------
;; Page mounting component


(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [page]
       [:footer
        [:p "About Wheel of Misfortune "
         [:a {:href "https://github.com/jmartinezhern/wheel-of-misfortune"} "Source Code"] "."]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match        (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)
            query-params (:query-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :query-params query-params
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
