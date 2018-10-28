(ns snake.game
  (:require [clojure.spec.alpha :as s :include-macros true]
            [reagent.core :as reagent]
            [snake.components :as components]
            [snake.dom :as dom]))

(enable-console-print!)

(s/def ::board-dimensions (s/tuple int? int?))
(s/def ::position (s/tuple int? int?))
(s/def ::snake (s/coll-of ::position :min-count 1))
(s/def ::direction #{:up :down :left :right})
(s/def ::last-direction ::direction)
(s/def ::input-direction ::direction)
(s/def ::growing boolean?)
(s/def ::running boolean?)
(s/def ::game-state-spec (s/keys :req-un [::board-dimensions
                                          ::snake
                                          ::last-direction
                                          ::input-direction
                                          ::growing
                                          ::running]))

(def initial-game-state
  {:board-dimensions [50 50]
   :snake [[25 25] [25 24] [25 23] [24 23] [24 22]]
   :fruit [[25 30]]
   :last-direction :down
   :input-direction :down
   :growing false
   :running false
   :timer nil})

(defonce game-state (reagent/atom initial-game-state))
(set-validator! game-state (partial s/valid? ::game-state-spec))

(defn render []
  (reagent/render-component [components/board game-state]
                            (dom/by-id "app")))

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

(defn leaving-board? [[x y] [height width]]
  (or (>= x width)
      (< x 0)
      (>= y height)
      (< y 0)))

(defn game-over?
  "If the head is in the same position as a part of the tail ya blew it.
  Also don't try to leave the game board."
  [game-state]
  (let [[head & tail] (:snake @game-state)
        dimensions (:board-dimensions @game-state)]
    (or (boolean (some #{head} tail))
        (leaving-board? head dimensions))))

(declare tick!)

(defn clear-timer []
  (if-let [timer (:timer @game-state)]
    (js/clearInterval timer)))

(defn start-game []
  (swap! game-state
         assoc
         :running true
         :timer (js/setInterval tick! 500)))

(defn stop-game []
  (clear-timer)
  (reset! game-state initial-game-state))

(defn pause-game []
  (clear-timer)
  (swap! game-state
         assoc
         :running false
         :timer nil))

(defn toggle-pause []
  (if (:running @game-state)
    (pause-game)
    (start-game)))

(defn game-over []
  (stop-game)
  (js/alert "Game over!"))

(defn tick! []
  (if (game-over? game-state)
    (game-over)
    (move-snake! game-state)))

(def key-names
  {38 :up
   40 :down
   37 :left
   39 :right
   13 :enter
   32 :space})

(defn handle-keydown [e]
  (when-let [key-name (key-names (.-keyCode e))]
    (.preventDefault e)
    (if (#{:enter :space} key-name)
      (toggle-pause)
      (swap! game-state assoc :input-direction key-name))))
