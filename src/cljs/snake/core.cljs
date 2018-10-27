(ns snake.core
  (:require [snake.dom :refer [by-id]]
            [snake.game :refer [game-state]]
            [snake.components :refer [board]]
            [reagent.core :as reagent]))

(enable-console-print!)

(defn init []
  (reagent/render-component [board game-state]
                            (by-id "app")))
