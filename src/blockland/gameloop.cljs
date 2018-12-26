(ns blockland.gameloop
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.events.EventType :as EventType]
            [three :as three]))

(defonce keys-pressed (atom #{}))
(defonce mouse-move (atom {}))

(defn handle-mousemove [e]
  (when js/document.pointerLockElement
    (let [e (.getBrowserEvent e)
          dx (.-movementX e)
          dy (.-movementY e)]
      (reset! mouse-move {:delta-x dx :delta-y dy}))))

(defn bind-events-to-canvas! [canvas]
  (events/listen canvas
                 EventType/MOUSEDOWN
                 (fn [e]
                   (.requestPointerLock canvas)
                   (.preventDefault e)))

  (events/listen canvas
                 EventType/MOUSEMOVE
                 (fn [e]
                   (handle-mousemove e))))

(defn setup-input-events! []
  (let [canvas (dom/getElementByTagNameAndClass "canvas")]
    (bind-events-to-canvas! canvas))

  (events/listen js/document
                 EventType/KEYDOWN
                 (fn [e]
                   ;; (.preventDefault e)
                   (let [key (.-key e)]
                     (swap! keys-pressed #(conj % key)))))

  (events/listen js/document
                 EventType/KEYUP
                 (fn [e]
                   ;; (.preventDefault e)
                   (let [key (.-key e)]
                     (swap! keys-pressed #(disj % key))))))

(defn run-game! [loop-fn]
  (let [clock (three/Clock.)]
    (letfn [(animate! []
              (js/requestAnimationFrame animate!)
              (loop-fn {:delta-time (.getDelta clock)
                        :input {:keys-pressed @keys-pressed
                                :mouse @mouse-move}})
              (reset! mouse-move {:delta-x 0 :delta-y 0}))]
      (js/requestAnimationFrame animate!))))

(comment

  @mouse-move

  )
