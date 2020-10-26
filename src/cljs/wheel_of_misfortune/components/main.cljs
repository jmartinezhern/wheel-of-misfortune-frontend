(ns wheel-of-misfortune.components.main
  "Primary components used by the web application."
  (:require [wheel-of-misfortune.routes :refer [path-for]]))



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

(defn upload []
  (fn []
    [:div]))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About wheel-of-misfortune"]]))
