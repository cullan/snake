(ns snake.game
  (:require [clojure.spec.alpha :as s :include-macros true]
            [reagent.core :as r]))

(s/def ::board-dimensions (s/tuple int? int?))
(s/def ::position (s/tuple int? int?))
(s/def ::snake (s/coll-of ::position :min-count 1))
(s/def ::direction #{:up :down :left :right})
(s/def ::last-direction ::direction)
(s/def ::input-direction ::direction)
(s/def ::growing boolean?)
(s/def ::game-state-spec (s/keys :req-un [::board-dimensions
                                          ::snake
                                          ::last-direction
                                          ::input-direction
                                          ::growing]))

(def initial-game-state
  {:board-dimensions [50 50]
   :snake [[25 25] [25 24] [25 23] [24 23] [24 22]]
   :last-direction :down
   :input-direction :down
   :growing false})

(def game-state (r/atom initial-game-state))
(set-validator! game-state (partial s/valid? ::game-state-spec))

(defn position-in-direction [[x y] direction]
  (case direction
    :up [x (- y 1)]
    :down [x (+ y 1)]
    :left [(- x 1) y]
    :right [(+ x 1) y]))

(defn valid-move?
  "Does the input direct the snake to move backwards."
  [new-position [_ first-tail]]
  (not= new-position first-tail))

(defn valid-input?
  "Did the user enter a valid direction to move?"
  [game-state]
  (let [[head :as snake] (:snake @game-state)
        direction (:input-direction @game-state)
        position (position-in-direction head direction)]
    (valid-move? position snake)))

(defn set-direction! [game-state valid?]
  (if valid? (swap! game-state
                    assoc :last-direction (:input-direction @game-state))))

(defn move-snake! [game-state]
  (let [[head & tail :as snake] (:snake @game-state)
        growing? (:growing @game-state)
        valid-direction? (valid-input? game-state)
        direction-key (if valid-direction? :input-direction :last-direction)
        direction (direction-key @game-state)
        next-pos (position-in-direction head direction)]
    (set-direction! game-state valid-direction?) ; flip input and last as needed
    (if growing?
      (swap! game-state
             assoc
             :snake (into [next-pos] snake)
             :growing false)
      (swap! game-state
             assoc :snake (into [next-pos] (pop snake))))))

(defn game-over?
  "If the head is in the same position as a part of the tail ya blew it."
  [game-state]
  (let [[head & tail] (:snake @game-state)]
    (boolean (some #{head} tail))))

(defn tick! []
  (move-snake! game-state))

(js/setInterval tick! 500)
