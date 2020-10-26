(ns wheel-of-misfortune.repositories.files.core)

(defprotocol FilesRepository
  (retrieve [repo id])
  (save! [repo file]))
