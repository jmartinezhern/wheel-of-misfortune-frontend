(ns wheel-of-misfortune.components.main
  "Primary components used by the web application."
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clojure.string :refer [blank?]]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]
            [wheel-of-misfortune.http.client :as client]
            [wheel-of-misfortune.routes :refer [path-for]]))

(def query (r/atom ""))
(def results (r/atom []))

;; -------------------------
;; Page components


(defn home-page []
  (fn []
    [:span.main
     [:h1 "Wheel of Misfortune"]
     [:div
      [:a {:href (path-for :scenarios)} "Browse scenarios"]]
     [:div
      [:a {:href (path-for :search)} "Find a scenario"]]
     [:div
      [:a {:href (path-for :upload)} "Upload a scenario"]]]))

(defn search []
  [:span.main
   [:input
    {:on-change #(reset! query (-> % .-target .-value))}]
   [:button
    {:on-click (fn []
                 (when (not (blank? @query))
                   (go (let [{:keys [body]} (<! (client/fetch-scenarios {:tags @query}))]
                         (reset! results (vec body))))))}
    "Search"]
   [:ul (map (fn [{:keys [id name description]}]
               [:div [:p id] [:p name] [:p description]]) @results)]])

(defn upload []
  (fn []
    [:div]))

(defn scenarios-page []
  (fn []
    [:span.main
     [:h1 "Browse Scenarios"]
     [:ul (map (fn [scenario-id]
                 [:li {:name (str "scenario-" scenario-id)
                       :key (str "scenario-" scenario-id)}
                  [:a {:href (path-for :scenario {:scenario-id scenario-id})}
                   "Scenario: " scenario-id]])
               (range 1 60))]]))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About wheel-of-misfortune"]]))

(defn command-history [history]
  (add-watch history :area-update
             (fn [_ _ _ _]
               (let [elm
                     (.getElementById js/document "command-history")
                     scroll-height
                     (.-scrollHeight elm)]
                 (set! (.-scrollTop elm) scroll-height))))
  (fn []
    [:div
     [:textarea {:id       "command-history"
                 :class    "command-text"
                 :value    @history
                 :readOnly true}]]))

(defn command-line [command history]
  [:div [:span "> "] [:input
                      {:id        "command-line"
                       :class     "command-text"
                       :type      "text"
                       :value     @command
                       :on-change #(reset! command (-> % .-target .-value))
                       :on-key-up #(when (= (.-which %) 13)
                                     (reset! history
                                             (str @history "\n" @command))
                                     (reset! command ""))}]])

(defn scenario-page []
  (let [routing-data (session/get :route)
        scenario-id (get-in routing-data [:route-params :scenario-id])
        command (r/atom "")
        history (r/atom "")]
    [:div
     [command-history history]
     [command-line command history]]))
