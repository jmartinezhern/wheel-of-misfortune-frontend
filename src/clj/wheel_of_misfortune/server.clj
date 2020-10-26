(ns wheel-of-misfortune.server
  (:require
   [wheel-of-misfortune.routes.handler :refer [app]]
   [config.core :refer [env]]
   [ring.adapter.jetty :refer [run-jetty]]
   [toucan
    [db :refer [set-default-db-connection!]]
    [models :refer [set-root-namespace!]]])
  (:gen-class))

(defn init []
  (set-default-db-connection!
   (merge {:classname   "org.postgresql.Driver"
           :subprotocol "postgresql"
           :subname     (format "//%s:%s/%s"
                                (or (env :db-host) "localhost")
                                (or (env :db-port) "5432")
                                (or (env :db-name) "jmartinezhern"))}
          (when-let [user (env :db-user)]
            {:user user})
          (when-let [password (env :db-pass)]
            {:password password})))

  (set-root-namespace! 'wheel-of-misfortune.repositories.scenarios.models))

(defn -main [& _]
  (init)
  (let [port (or (env :port) 3000)]
    (run-jetty #'app {:port port :join? false})))
