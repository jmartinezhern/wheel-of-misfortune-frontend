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

(defn update-dist-of-neighbor
  [node
   neighbor
   dist-from-source
   dist-from-node
   current-dist-in-path]
  (let [dist-through-node (+ dist-from-source dist-from-node)]
    (when (or  (nil? current-dist-in-path)
               (and  (not (nil? current-dist-in-path))
                     (< dist-through-node current-dist-in-path)))
      {neighbor {:dist dist-through-node :prev node}})))

(defn process-node
  [graph
   current-node
   path]
  (let [neighbors        (get graph current-node)
        dist-from-source (if-let [curnode-in-path (get path current-node)]
                           (get curnode-in-path :dist)
                           0)
        ;; This is the source node, so we'll give it 0 as distance 
        new-path         (->> neighbors ;; for each of its neighbors
                              (map (fn [[neighbor dist-to-cur-node]]
                                     (update-dist-of-neighbor current-node
                                                              neighbor
                                                              dist-from-source
                                                              dist-to-cur-node
                                                              (get (get path neighbor) :dist))))
                      ;; we'll compute the new distance and previous node
                      ;; using the utility function (update-dist-of-neighbor)
                              (into path))]
    ;; which we'll return as new-path 
    new-path))

(defn dijkstra
  [graph]
  (loop [remaining-vertices (keys graph)
         path               {}]
    (if (seq remaining-vertices)
      ;; for every node we process its neighbors
      ;; using process-node
      (recur (rest remaining-vertices)
             (process-node graph (first remaining-vertices) path))
      path)))

(defn- optimal-solution
  [graph source target]
  (let [dijkstra-shortest-paths (dijkstra graph)]
    ;; we compute the shortest paths 
    (loop [new-target (get dijkstra-shortest-paths target)
           ;; We begin from target
           path       [target]]
      (let [next-new-target (get new-target :prev)]
        ;; and get back to the previous node
        (cond (nil? next-new-target)     :error
              ;; we did not find a :prev reference
              (= next-new-target source) (into [source] path)
              ;; we are done,
              ;; we return the path
              :else                      (recur (get dijkstra-shortest-paths
                                                     next-new-target)
                           ;; we recur using the previous node
                           ;; and adding it to the result path
                                                (into [next-new-target] path)))))))

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
        remaining-steps       (when (not won?)
                                (-> (state-scores states)
                                    (optimal-solution (last transitions) termination)
                                    rest))]
    (fn []
      [:span.main
       [:h1 (if won? "Crisis averted!" "Not quite")]
       [:h2 "Here is what happened..."]
       [:div
        [:h3#scenario-title {:style {:margin-bottom 0}} name]
        [:div
         (transitions->steps states transitions green)
         (transitions->steps states remaining-steps yellow)]]])))
