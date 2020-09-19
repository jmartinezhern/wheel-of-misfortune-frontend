(ns wheel-of-misfortune.components.scenario
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [accountant.core :as accountant]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]
            [wheel-of-misfortune.http.client :as client]))

(def history (r/atom ""))
(def scenario (r/atom {}))
(def state (r/atom nil))

(defn- scenario-id []
  (get-in (session/get :route) [:route-params :scenario-id]))

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
                       (str "/scenarios/" (scenario-id) "/complete"))
    (not (nil? next)) (reset! state next)))

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
                                             @command
                                             "\n"
                                             output))

                        (reset! command "")))}])))

(def command-edit (with-meta command-line
                    {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn page []
  (go
    (let [{:keys [states start] :as body} (<! (client/fetch-scenario (scenario-id)))]
      (reset! state (start states))
      (reset! scenario body)
      (reset! history (:output (start states)))))
  (fn []
    [:span
     [:h2#scenario-title (:name @scenario)]
     [command-history history]
     [:div [command-edit]]]))
