(ns snake.components
  (:require [clojure.string :as string]
            [reagent.core :as r]))

(def scale 10)
(def snake-part-size 8)
(def food-piece-size 4)
(def snake-head-color "#366")
(def snake-eye-color "#000")
(def snake-tail-color "#9FF")
(def food-color "#C00")

(defn- translate [coord]
  (+ (* coord scale) 1))

(defn- list-key [type x y]
  (string/join "-" [type x y]))

(defn score [score]
  [:p (str "Score: " @score)])

(defn instructions []
  [:p "Hit enter or space to begin. The arrow keys control the snake."])

(defn header [game-state]
  (let [score-cursor (r/cursor game-state [:score])]
    [:div.header
     [:h4 "Snake Game"]
     [score score-cursor]
     [instructions]]))

(defn- eye-offset [[x y] direction]
  (case direction
    :up [[2 2] [6 2]]
    :down [[2 6] [6 6]]
    :left [[2 6] [2 2]]
    :right [[6 2] [6 6]]))

(defn snake-head [[x y] direction]
  (let [[[lx ly] [rx ry]] (eye-offset [x y] @direction)]
    [:g
     [:rect {:x (translate x)
             :y (translate y)
             :height (str snake-part-size)
             :width (str snake-part-size)
             :fill snake-head-color}]
     [:circle {:cx (+ (translate x) lx)
               :cy (+ (translate y) ly)
               :r 1
               :fill snake-eye-color}]
     [:circle {:cx (+ (translate x) rx)
               :cy (+ (translate y) ry)
               :r 1
               :fill snake-eye-color}]]))

(defn snake-tail-part [[x y]]
  [:rect {:x (translate x)
          :y (translate y)
          :height (str snake-part-size)
          :width (str snake-part-size)
          :fill snake-tail-color}])

(defn snake [body direction]
  (let [[head & tail] @body]
    [:g
     [snake-head head direction]
     (for [[x y] tail]
       ^{:key (list-key "snake" x y)} [snake-tail-part [x y]])]))

(defn food-piece [[x y]]
  [:circle {:cx (+ (translate x) food-piece-size)
            :cy (+ (translate y) food-piece-size)
            :r (str food-piece-size)
            :fill food-color}])

(defn food [locations]
  [:g (for [[x y] @locations]
        ^{:key (list-key "food" x y)} [food-piece [x y]])])

(defn board [game-state]
  (let [dimensions (r/cursor game-state [:board-dimensions])
        [height width] @dimensions]
    [:svg {:class "board"
           :view-box (str "0 0 " (* width scale) " " (* height scale))}
     [:rect {:width "100%" :height "100%" :fill "#777"}]
     [food (r/cursor game-state [:food])]
     [snake
      (r/cursor game-state [:snake])
      (r/cursor game-state [:input-direction])]]))

(defn app [game-state]
  [:div.container
   [header game-state]
   [board game-state]])
