(ns blockland.gameloop
  (:require [goog.events :as events]
            [goog.events.EventType :as EventType]
            [three :as three]))

(defonce keys-pressed (atom #{}))

(defonce key-events
  (do
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
                       (swap! keys-pressed #(disj % key)))))))

(defn run-game! [loop-fn]
  (let [clock (three/Clock.)]
    (letfn [(animate! []
              (js/requestAnimationFrame animate!)
              (loop-fn {:delta-time (.getDelta clock)
                        :keys-pressed @keys-pressed}))]
      (js/requestAnimationFrame animate!))))
