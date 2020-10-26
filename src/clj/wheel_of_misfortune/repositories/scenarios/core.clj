(ns wheel-of-misfortune.repositories.scenarios.core)

(defprotocol ScenariosRepository
  (save! [repo scenario])
  (subset [repo page])
  (subset-by-tags [repo tags page])
  (retrieve [repo id])
  (inc! [repo id k v])
  (update! [repo id object])
  (assoc-tags! [repo scenario-id tags]))
