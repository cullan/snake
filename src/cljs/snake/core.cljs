(ns snake.core
  (:require [snake.game :as game]))

(enable-console-print!)

(defonce bind-key-handler
  (.addEventListener js/document "keydown" game/handle-keydown))

(defn on-reload []
  (game/stop-game)
  (game/render))

(defn init []
  (game/render))
