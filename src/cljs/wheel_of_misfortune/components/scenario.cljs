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
(defonce ^:private transitions (r/atom []))

(defn- navigate-to-complete-page [scenario-id]
  (accountant/navigate!
   (str "/scenarios/"
        (scenario-id)
        "/complete?won="
        (if @won "true" "false"))))

(defn- storage-transition-key [id]
  (str id "-transitions"))

(defn- scenario-id []
  (get-in (session/get :route) [:route-params :scenario-id]))

(defn- load-scenario [id]
  (go
    (let
     [{:keys [states start] :as body} (<! (c/fetch-scenario id))]
      (reset! transitions [start])
      (reset! state start)
      (reset! scenario body)
      (reset! history (:output (start states)))
      (-> (.-localStorage js/window) (.setItem id body)))))

(defn- transition [cmd state]
  (:transition (some #(when (= (:match %) cmd) %) (:commands state))))

(defn- cmd-output [t]
  (let [next   (get-in
                @scenario
                [:states t])
        output (:output next)]
    (cond
      (= (:termination @scenario) t) (str
                                      output
                                      "\n\nScenario complete! Enter 'exit' to continue.")
      (nil? next) "Invalid command"
      :else output)))

(defn- command-history []
  (fn []
    (r/after-render
     (fn []
       (when-let [elm (.getElementById js/document "command-history")]
         (set! (.-scrollTop elm) (.-scrollHeight elm)))))
    [:textarea#history.cmd-text
     {:value    @history
      :readOnly true}]))

(defn- next-game-state! [cmd t]
  (cond
    (= cmd "exit") (navigate-to-complete-page scenario-id)
    (not (nil? t)) (reset! state t))
  (when (= (:terminiation @scenario) t)
    (reset! won true)))

(defn command-line []
  (let [command (r/atom "")]
    (fn []
      [:input#command-line.cmd-text
       {:type      "text"
        :value     (str "> " @command)
        :on-change #(reset! command (subs (-> % .-target .-value) 2))
        :on-key-up #(when (= (.-which %) 13)
                      (let [t      (transition @command (get-in
                                                         @scenario
                                                         [:states @state]))]
                        (next-game-state! @command t)
                        (swap! transitions conj t)
                        (reset! history (str @history
                                             "\n"
                                             (str "> " @command)
                                             "\n"
                                             (cmd-output t)))

                        (reset! command "")))}])))

(def command-edit (with-meta command-line
                    {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn page []
  (let [scenario-id                              (scenario-id)]
    (load-scenario scenario-id)
    (add-watch transitions :state-transition
               (fn [_ _ new _]
                 (-> (.-localStorage js/window)
                     (.setItem (storage-transition-key scenario-id) new))))
    (fn []
      [:span
       [:h2#scenario-title (:name @scenario)]
       [command-history history]
       [:div [command-edit]]])))
