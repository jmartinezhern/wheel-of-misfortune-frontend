(ns wheel-of-misfortune.components.complete
  (:require [clojure.edn :as edn]
            [reagent.session :as session]))

(defonce ^:private green "#2AC940")
(defonce ^:private yellow "#F3CA3E")

(defn- box [color] [:div.square-box {:style {:background-color color}}])

(defn local-storage->edn [k]
  (-> (.-localStorage js/window)
      (.getItem k)
      edn/read-string))

(defn- step [title description color]
  (fn []
    [:div.step-container
     [box color]
     [:h3.step-title title]
     [:p.step-description description]]))

(defn- update-dists
  [node
   neighbor
   dist-from-source
   dist-from-node
   current-dist-in-path]
  (let [dist-through-node (+ dist-from-source dist-from-node)]
    (when (or  (nil? current-dist-in-path)
               (< dist-through-node current-dist-in-path))
      {neighbor {:dist dist-through-node :prev node}})))

(defn- process-node [graph node path]
  (let [neighbors   (get graph node)
        source-dist (if-let [node-in-path (get path node)]
                      (get node-in-path :dist)
                      0)
        new-path    (->>
                     neighbors
                     (map (fn [[neighbor dist-to-cur-node]]
                            (update-dists node
                                          neighbor
                                          source-dist
                                          dist-to-cur-node
                                          (get (get path neighbor) :dist))))
                     (into path))]
    new-path))

(defn- dijkstra [graph]
  (loop [remaining (keys graph)
         path      {}]
    (if (seq remaining)
      (recur (rest remaining)
             (process-node graph (first remaining) path))
      path)))

(defn- optimal-solution [graph source target]
  (let [shortest-paths (dijkstra graph)]
    (loop [new-target (get shortest-paths target)
           path       [target]]
      (let [next-target (get new-target :prev)]
        (cond (nil? next-target)     :error
              (= next-target source) (into [source] path)
              :else                  (recur (get shortest-paths
                                                 next-target)
                                            (into [next-target] path)))))))

(defn- state-scores [states]
  (into
   {}
   (for [[k v] states]
     [k (into
         {}
         (for [{:keys [transition]} (:commands v)]
           [transition (get-in states [transition :score])]))])))

(defn- transitions->steps [states ts color]
  (for [t    ts
        :let [{:keys [title description]} (t states)]]
    ^{:key t} [step
               title
               description
               color]))

(defn- won? []
  (if (#{"true"} (get-in (session/get :route) [:query-params :won])) true false))

(defn page []
  (let [scenario-id           (get-in (session/get :route) [:route-params :scenario-id])
        {:keys [name
                states
                termination]} (local-storage->edn scenario-id)
        transitions           (local-storage->edn (str scenario-id "-transitions"))
        remaining             (if (not (won?))
                                (-> (state-scores states)
                                    (optimal-solution (last transitions) termination)
                                    rest)
                                [])]
    (fn []
      [:span.main
       [:h1 (if won? "Crisis averted!" "Not quite")]
       [:h2 "Here is what happened..."]
       [:div
        [:h3#scenario-title {:style {:margin-bottom 0}} name]
        [:div
         (transitions->steps states transitions green)
         (transitions->steps states remaining yellow)]]])))
