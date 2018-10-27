(ns snake.game
  (:require [clojure.spec.alpha :as s :include-macros true]
            [reagent.core :as r]))

(s/def ::board-dimensions (s/tuple int? int?))
(s/def ::position (s/tuple int? int?))
(s/def ::snake (s/coll-of ::position :min-count 1 :distinct true))
(s/def ::game-state-spec (s/keys :req-un [::board-height
                                          ::board-width
                                          ::snake]))

(def initial-game-state
  {:board-dimensions [50 50]
   :snake [[25 25] [25 24] [25 23] [24 23]])

(def game-state (r/atom initial-game-state))
(set-validator! game-state (partial s/valid? ::game-state-spec))
