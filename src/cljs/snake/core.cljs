(ns snake.core
  (:require [snake.dom :refer [by-id]]
            [snake.components.board :refer [board]]
            [reagent.core :as r :refer [render atom]]))

(enable-console-print!)

(render [board 50 50]
        (by-id "app"))
