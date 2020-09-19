(ns wheel-of-misfortune.components.main
  "Primary components used by the web application."
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clojure.string :refer [blank?]]
            [cljs.core.async :refer [<!]]
            [wheel-of-misfortune.http.client :as client]
            [wheel-of-misfortune.routes :refer [path-for]]))



;; -------------------------
;; Page components


(defn home-page []
  (fn []
    [:div#main-page
     [:div.main-page-item
      [:h1#main-title "Wheel of Misfortune"]]
     [:div.main-page-item
      [:a {:href (path-for :search)} "Find a scenario"]]
     [:div.main-page-item
      [:a {:href (path-for :upload)} "Upload a scenario"]]]))

(defn search []
  (let [query (r/atom "")
        results (r/atom [])]
    (go (reset! results (<! (client/fetch-scenarios
                             {:order-by "oldest"
                              :limit 10}))))
    (fn []
      [:span.main
       [:input
        {:on-change #(reset! query (-> % .-target .-value))}]
       [:button
        {:on-click (fn []
                     (when (not (blank? @query))
                       (go (let [{:keys [body]} (<! (client/fetch-scenarios {:tags     @query
                                                                             :order-by "oldest"
                                                                             :limit    10}))]
                             (reset! results (vec body))))))}
        "Search"]
       [:ul (map (fn [{:keys [id description name]}]
                   [:li {:key id}
                    [:p {:href (path-for :scenario {:scenario-id id})}
                     name]
                    [:p description]
                    [:a
                     {:href (path-for :scenario {:scenario-id id})} "Run"]])
                 @results)]])))

(defn upload []
  (fn []
    [:div]))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About wheel-of-misfortune"]]))

(defn complete-page []
  (fn [] [:span.main
          [:h1 "Complete!"]]))
