(ns wheel-of-misfortune.components.scenario
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [accountant.core :as accountant]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]
            [wheel-of-misfortune.http.client :as c]))

(defonce ^:private history (r/atom ""))
(defonce ^:private scenario (r/atom {}))
(defonce ^:private state (r/atom nil))
(defonce ^:private won (r/atom false))

(defn- scenario-id []
  (get-in (session/get :route) [:route-params :scenario-id]))

(defn- load-scenario []
  (go
    (let
     [id                              (scenario-id)
      {:keys [states start] :as body} (<! (c/fetch-scenario id))]
      (reset! state (start states))
      (reset! scenario body)
      (reset! history (:output (start states)))
      (-> (.-localStorage js/window) (.setItem id body)))))

(defn- transition [cmd state]
  (:transition (some #(when (= (:match %) cmd) %) (:commands state))))

(defn- cmd-output [{:keys [terminates output] :as next}]
  (cond
    terminates "Scenario complete! Enter 'exit' to continue."
    (nil? next) "Invalid command"
    :else output))

(defn- command-history []
  (fn []
    (r/after-render
     (fn []
       (when-let [elm (.getElementById js/document "command-history")]
         (set! (.-scrollTop elm) (.-scrollHeight elm)))))
    [:textarea#history.cmd-text
     {:value    @history
      :readOnly true}]))

(defn- next-game-state! [cmd next]
  (cond
    (= cmd "exit")    (accountant/navigate!
                       (str "/scenarios/"
                            (scenario-id)
                            "/complete?won="
                            (if @won "true" "false")))
    (not (nil? next)) (reset! state next))
  (when (:terminates next)
    (reset! won true)))

(defn command-line []
  (let [command (r/atom "")]
    (fn []
      [:input#command-line.cmd-text
       {:type      "text"
        :value     (str "> " @command)
        :on-change #(reset! command (subs (-> % .-target .-value) 2))
        :on-key-up #(when (= (.-which %) 13)
                      (let [next   (get-in
                                    @scenario
                                    [:states (transition @command @state)])
                            output (cmd-output next)]
                        (next-game-state! @command next)
                        (reset! history (str @history
                                             "\n"
                                             (str "> " @command)
                                             "\n"
                                             output))

                        (reset! command "")))}])))

(def command-edit (with-meta command-line
                    {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn page []
  (load-scenario)
  (fn []
    [:span
     [:h2#scenario-title (:name @scenario)]
     [command-history history]
     [:div [command-edit]]]))
