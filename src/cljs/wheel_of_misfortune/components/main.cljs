(ns wheel-of-misfortune.components.main
  "Primary components used by the web application."
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :refer [blank?]]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]
            [wheel-of-misfortune.http.client :as client]
            [wheel-of-misfortune.routes :refer [path-for]]))

(def query (r/atom ""))
(def results (r/atom []))
(def page-number (r/atom 1))

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
  (go (let [{:keys [body]} (<! (client/fetch-scenarios {:order-by "oldest"
                                                        :limit 10}))]
        (reset! results (vec body))))
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
               @results)]]))

(defn upload []
  (fn []
    [:div]))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About wheel-of-misfortune"]]))

(defn command-history [history]
  (fn []
    (r/after-render
     (fn []
       (when-let [elm (.getElementById js/document "command-history")]
         (set! (.-scrollTop elm) (.-scrollHeight elm)))))
    [:textarea {:id       "command-history"
                :class    "command-text"
                :value    @history
                :readOnly true}]))

(defn command-line [command history]
  (fn []
    [:input
     {:id        "command-line"
      :class     "command-text"
      :type      "text"
      :value     (str "> " @command)
      :on-change #(reset! command (subs (-> % .-target .-value) 2))
      :on-key-up #(when (= (.-which %) 13)
                    (reset! history
                            (str @history "\n" @command))
                    (reset! command ""))}]))

(def command-edit (with-meta command-line {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn scenario-page []
  (let [routing-data (session/get :route)
        scenario-id (get-in routing-data [:route-params :scenario-id])
        command (r/atom "")
        history (r/atom "")
        scenario (r/atom {})]
    (go (let [{{:keys [states start] :as body} :body} (<! (client/fetch-scenario scenario-id))]
          (reset! scenario body)
          (reset! history (:output (start states)))))
    (fn []
      [:span
       [:h2#scenario-title (:name @scenario)]
       [command-history history]
       [:div [command-edit command history]]])))
