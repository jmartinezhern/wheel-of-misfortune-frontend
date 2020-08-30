(ns wheel-of-misfortune.prod
  (:require [wheel-of-misfortune.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
