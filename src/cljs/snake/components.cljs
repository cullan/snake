(ns snake.components
  (:require [reagent.core :as r]))

(enable-console-print!)

(def snake-head-color "#366")
(def snake-tail-color "#9FF")

(defn snake-part [[x y] color]
  [:rect {:x (+ (* x 10) 1)
          :y (+ (* y 10) 1)
          :height "8"
          :width "8"
          :fill color}])

(defn snake [body]
  (let [[head & tail] @body]
    [:g
     [snake-part head snake-head-color]
     (for [[x y] tail]
       ^{:key (str "snake-" x "-" y)} [snake-part [x y] snake-tail-color])]))

(defn board [game-state]
  (let [width (r/cursor game-state [:board-width])
        height (r/cursor game-state [:board-height])]
    [:div {:class "board-container"}
     [:svg {:class "board"
            :view-box (str "0 0 " (* @width 10) " " (* @height 10))}
      [:rect {:width "100%" :height "100%" :fill "#777"}]
      [snake (r/cursor game-state [:snake])]]]))
