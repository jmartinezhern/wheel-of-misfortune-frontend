(ns wheel-of-misfortune.components.complete
  (:require [clojure.edn :as edn]
            [reagent.session :as session]))

(defonce ^:private green "#2AC940")

(defn- box [color] [:div.square-box {:style {:background-color color}}])

(defn local-storage->edn [k]
  (-> (.-localStorage js/window)
      (.getItem k)
      edn/read-string))

(defn- step [title description]
  (fn []
    [:div.step-container
     [box green]
     [:h3.step-title title]
     [:p.step-description description]]))

(defn- scenario-solution [])

(defn page []
  (let [scenario-id (get-in (session/get :route) [:route-params :scenario-id])
        won-query   (get-in (session/get :route) [:query-params :won])
        won         (if (#{"true"} won-query) true false)
        scenario    (local-storage->edn scenario-id)
        transitions (local-storage->edn (str scenario-id "-transitions"))]
    (fn []
      [:span.main
       [:h1 (if won "Crisis averted!" "Not quite")]
       [:h2 "Here is what happened..."]
       [:div
        [:h3#scenario-title {:style {:margin-bottom 0}} (:name scenario)]
        [:div
         (for [transition transitions
               :let [{:keys [title description]} (get-in scenario [:states
                                                 transition])]]
           ^{:key transition} [step
                               title
                               description
                               (get-in scenario [:states
                                                 transition
                                                 :description])])]]])))
