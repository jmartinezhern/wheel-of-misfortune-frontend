(ns wheel-of-misfortune.repositories.files.local
  (:require [clojure.edn :as edn]
            [wheel-of-misfortune.repositories.files.core :as core]))

(deftype FilesRepository [root-volume-path]
  core/FilesRepository

  (save! [_ file]
    (let [path (:path file)]
      (spit (str root-volume-path "/" path) (pr-str (dissoc file :path)))))

  (retrieve [_ path]
    (edn/read-string (slurp (str root-volume-path "/" path)))))
