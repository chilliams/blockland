(ns blockland.gameloop
  (:require [goog.events :as events]
            [goog.events.EventType :as EventType]))

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
  (let [prev-time (atom 0)]
    (letfn [(animate! [time]
               (let [delta-time (- time @prev-time)]
                 (reset! prev-time time)
                 (js/requestAnimationFrame animate!)
                 (loop-fn {:delta-time delta-time
                           :keys-pressed @keys-pressed})))]
      (js/requestAnimationFrame animate!))))
