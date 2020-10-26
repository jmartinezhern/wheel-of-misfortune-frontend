(ns wheel-of-misfortune.middleware
  (:require
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.reload :refer [wrap-reload]]))

(def middleware
  [wrap-exceptions
   wrap-reload])
