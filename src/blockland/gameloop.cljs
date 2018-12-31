(ns blockland.gameloop
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.events.EventType :as EventType]))

(defonce keys-pressed (atom #{}))
(defonce mouse-move (atom {}))

(defn handle-mousemove [e]
  (when js/document.pointerLockElement
    (let [e (.getBrowserEvent e)
          dx (.-movementX e)
          dy (.-movementY e)]
      (reset! mouse-move {:delta-x dx :delta-y dy}))))

(defn bind-events-to-canvas! [canvas {:keys [add-block! remove-block!]}]
  (events/listen canvas
                 EventType/MOUSEDOWN
                 (fn [e]
                   (when (= canvas (.-pointerLockElement js/document))
                     (case (.-button e)
                       0 (remove-block!)
                       2 (add-block!)
                       nil))
                   (.requestPointerLock canvas)
                   (.preventDefault e)))

  (events/listen canvas
                 EventType/MOUSEMOVE
                 (fn [e]
                   (handle-mousemove e))))

(defn setup-input-events! [canvas events]
  (bind-events-to-canvas! canvas events)

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

(defonce stats
  (let [stats (js/Stats.)]
    (.appendChild (.-body js/document) (.-dom stats))
    stats))

(defn run-game! [loop-fn]
  (let [clock (js/THREE.Clock.)]
    (letfn [(animate! []
              (.begin stats)
              (loop-fn {:delta-time (.getDelta clock)
                        :input {:keys-pressed @keys-pressed
                                :mouse @mouse-move}})
              (reset! mouse-move {:delta-x 0 :delta-y 0})
              (.end stats)
              (js/requestAnimationFrame animate!))]
      (js/requestAnimationFrame animate!))))

(comment

  @mouse-move

  )
