(ns snake.dom)

(defn by-id [id]
  (.getElementById js/document id))
