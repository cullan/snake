(ns snake.components.board
  (:require [reagent.core :as r :refer [atom]]))

(defn board [height width]
  [:div {:class "board-container"}
   [:svg {:class "board" :view-box (str "0 0 " (* width 10) " " (* height 10))}
   [:rect {:width "100%" :height "100%" :fill "#777"}]]])
