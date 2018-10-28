(ns snake.components
  (:require [clojure.string :as string]
            [reagent.core :as r]))

(def scale 10)
(def snake-part-size 8)
(def food-piece-size 4)
(def snake-head-color "#366")
(def snake-tail-color "#9FF")
(def food-color "#C00")

(defn- translate [coord]
  (+ (* coord scale) 1))

(defn- list-key [type x y]
  (string/join "-" [type x y]))

(defn snake-part [[x y] color]
  [:rect {:x (translate x)
          :y (translate y)
          :height (str snake-part-size)
          :width (str snake-part-size)
          :fill color}])

(defn snake [body]
  (let [[head & tail] @body]
    [:g
     [snake-part head snake-head-color]
     (for [[x y] tail]
       ^{:key (list-key "snake" x y)} [snake-part [x y] snake-tail-color])]))

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
    [:div {:class "board-container"}
     [:svg {:class "board"
            :view-box (str "0 0 " (* width scale) " " (* height scale))}
      [:rect {:width "100%" :height "100%" :fill "#777"}]
      [food (r/cursor game-state [:food])]
      [snake (r/cursor game-state [:snake])]]]))
