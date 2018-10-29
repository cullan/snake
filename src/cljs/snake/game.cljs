(ns snake.game
  (:require [clojure.spec.alpha :as s :include-macros true]
            [reagent.core :as reagent]
            [snake.components :as components]
            [snake.dom :as dom]))

(enable-console-print!)

(def number-of-food 5)
(def game-speed 200)

(s/def ::board-dimensions (s/tuple int? int?))
(s/def ::position (s/tuple int? int?))
(s/def ::snake (s/coll-of ::position :min-count 1))
(s/def ::food (s/coll-of ::position))
(s/def ::direction #{:up :down :left :right})
(s/def ::last-direction ::direction)
(s/def ::input-direction ::direction)
(s/def ::growing? boolean?)
(s/def ::running? boolean?)
(s/def ::timer (s/or :not-running nil? :running? int?))
(s/def ::first-turn? boolean?)
(s/def ::score int?)
(s/def ::game-state-spec (s/keys :req-un [::board-dimensions
                                          ::snake
                                          ::food
                                          ::last-direction
                                          ::input-direction
                                          ::growing?
                                          ::running?
                                          ::timer
                                          ::first-turn?
                                          ::score]))

(def initial-game-state
  {:board-dimensions [20 20]
   :snake [[10 10]]
   :food []
   :last-direction :down
   :input-direction :down
   :growing? false
   :running? false
   :timer nil
   :first-turn? true
   :score 0})

(defonce game-state (reagent/atom initial-game-state))
(set-validator! game-state (partial s/valid? ::game-state-spec))

(defn render []
  (reagent/render-component [components/app game-state]
                            (dom/by-id "app")))

(defn valid-food-position?
  "Food must fit on the board and not overlap other food or the snake."
  [[x y :as position]]
  (let [[height width] (:board-dimensions @game-state)
        food (:food @game-state)
        snake (:snake @game-state)]
    (and (< x width)
         (< y height)
         (not (some #{position} food))
         (not (some #{position} snake)))))

(defn board-full?
  "Detect the unlikely case that the board is full."
  []
  (let [[height width] (:board-dimensions @game-state)
        snake (:snake @game-state)
        food (:food @game-state)
        num-positions (* height width)
        filled (+ (count snake) (count food))]
    (< num-positions filled)))

(defn possible-food-positions
  "Make a lazy seq of possible food positions."
  []
  (let [[height width] (:board-dimensions @game-state)]
    (filter valid-food-position?
            (repeatedly #(vector (rand-int width)
                                 (rand-int height))))))

(defn add-food-piece!
  "Add a piece of food to the board."
  []
  (let [food (:food @game-state)]
    (when-not (board-full?)
    (swap! game-state
           assoc
           :food (conj food (first (possible-food-positions)))))))

(defn add-food!
  "Add n pieces of food to the game."
  [n]
  (dotimes [_ n]
    (add-food-piece!)))

(defn eating-food? []
  (let [[head] (:snake @game-state)
        food (:food @game-state)]
    (boolean (some #{head} food))))

(defn remove-food-at-head! []
  (let [[head] (:snake @game-state)
        food (:food @game-state)]
    (swap! game-state assoc :food (remove #{head} food))))

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
  []
  (let [[head :as snake] (:snake @game-state)
        direction (:input-direction @game-state)
        position (position-in-direction head direction)]
    (valid-move? position snake)))

(defn set-direction! [valid?]
  (if valid? (swap! game-state
                    assoc :last-direction (:input-direction @game-state))))

(defn move-snake! []
  (let [[head & tail :as snake] (:snake @game-state)
        growing? (:growing? @game-state)
        valid-direction? (valid-input?)
        direction-key (if valid-direction? :input-direction :last-direction)
        direction (direction-key @game-state)
        next-pos (position-in-direction head direction)]
    (set-direction! valid-direction?) ; flip input and last as needed
    (if growing?
      (swap! game-state
             assoc
             :snake (into [next-pos] snake)
             :growing? false)
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
  []
  (let [[head & tail] (:snake @game-state)
        dimensions (:board-dimensions @game-state)]
    (or (boolean (some #{head} tail))
        (leaving-board? head dimensions))))

(declare tick!)

(defn clear-timer []
  (if-let [timer (:timer @game-state)]
    (js/clearInterval timer)))

(defn start-game []
  (when (:first-turn? @game-state)
    (add-food! number-of-food)
    (swap! game-state assoc :first-turn? false))
  (swap! game-state
         assoc
         :running? true
         :timer (js/setInterval tick! game-speed)))

(defn stop-game []
  (clear-timer)
  (reset! game-state initial-game-state))

(defn maybe-start-game! []
  (let [running? (:running? @game-state)]
    (when-not running?
      (start-game))))

(defn game-over! []
  (let [score (:score @game-state)]
    (stop-game)
    (js/alert (str "Game over! Your score is " score "."))))

(defn tick! []
  (if (game-over?)
    (game-over!)
    (do
      (when (eating-food?)
        (swap! game-state assoc :growing? true)
        (swap! game-state assoc :score (inc (:score @game-state)))
        (remove-food-at-head!)
        (add-food-piece!))
      (move-snake!))))

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
      (maybe-start-game!)
      (swap! game-state assoc :input-direction key-name))))
