(ns wheel-of-misfortune.scenarios.spec
  (:require [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]))

(s/def ::id spec/string?)
(s/def ::name spec/string?)
(s/def ::title spec/string?)
(s/def ::description spec/string?)
(s/def ::transition spec/keyword?)
(s/def ::match spec/string?)
(s/def ::command (s/keys :req-un [::match ::transition]))
(s/def ::commands (s/coll-of ::command :kind vector?))
(s/def ::output spec/string?)
(s/def ::score spec/int?)
(s/def ::termination spec/keyword?)
(s/def ::state (s/keys :req-un [::description
                                ::output
                                ::score
                                ::title]
                       :opt-un [::commands]))
(s/def ::states (s/map-of spec/keyword? ::state))
(s/def ::tags (s/coll-of spec/keyword? :kind vector? distinct true))
(s/def ::created-at spec/int?)

(s/def ::request (s/keys :req-un [::name
                                  ::description
                                  ::states
                                  ::start
                                  ::termination]
                         :opt-un [::tags]))

(s/def ::details (s/keys :req-un [::id
                                  ::name
                                  ::description
                                  ::created-at
                                  ::score]))

(s/def ::scenario (s/merge (s/keys :req-un [::id ::created-at]) ::request))
