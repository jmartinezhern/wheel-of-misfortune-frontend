(ns wheel-of-misfortune.components.scenarios
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<!]]
            [wheel-of-misfortune.http.client :as c]
            [wheel-of-misfortune.routes :refer [path-for]]))

(def ^:private ^:const page-size 10)

(defn result [id description name]
  (fn []
    [:li
     [:p {:href (path-for :scenario {:scenario-id id})}
      name]
     [:p description]
     [:a
      {:href (path-for :scenario {:scenario-id id})} "Run"]]))

(defn search []
  (let [query          (r/atom "")
        results        (r/atom [])
        order-by       (r/atom "newest")
        fetch-results! (fn [] (go (reset! results (<! (c/fetch-scenarios
                                                       {:order-by @order-by
                                                        :tags     @query
                                                        :limit    page-size})))))]
    (fetch-results!)

    (fn []
      [:span.main
       [:input
        {:on-change #(reset! query (-> % .-target .-value))}]
       [:select {:name      "order-by"
                 :on-change #(reset! order-by (-> % .-target .-value))}
        [:option {:value "newest"} "Newest"]
        [:option {:value "oldest"} "Oldest"]
        [:option {:value "popularity"} "Popularity"]]
       [:button {:on-click #(fetch-results!)} "Search"]
       [:ul (map (fn [{:keys [id description name]}]
                   ^{:key id} [result id description name])
                 @results)]])))
