(ns wheel-of-misfortune.components.complete
  (:require [clojure.edn :as edn]
            [reagent.session :as session]))

(defn page []
  (let [scenario-id (get-in (session/get :route) [:route-params :scenario-id])
        won-query   (get-in (session/get :route) [:query-params :won])
        won         (if (#{"true"} won-query) true false)
        scenario    (-> (.-localStorage js/window)
                        (.getItem scenario-id)
                        edn/read-string)]
    (fn [] [:span.main
            [:h1 (if won "Crisis averted!" "Not quite")]
            [:h3 "Here is what happened..."]
            [:div
             [:h4#scenario-title {:style {:margin 0}} (:name scenario)]
             [:p (:output ((:start scenario) (:states scenario)))]
             ]])))
