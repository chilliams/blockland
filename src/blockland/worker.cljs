(ns blockland.worker
  "Runs as a Web Worker and hands off work to the C++ code"
  (:require [goog.object :as object]))

(enable-console-print!)

(js/importScripts "/js/chunkworker.js")

(defn handle-message [e]
  (let [size 3]
    (doseq [[x z] (sort-by (fn [p] (reduce + (map #(* % %) p)))
                           (for [x (range (- size) size)
                                 z (range (- size) size)]
                             [x z]))]
      (let [result (js/Module.make_chunk #js [x 0 z])]
        (.postMessage js/self
                      #js {:command "mesh"
                           :data result})))))

(defn add-event-listener []
  (js/self.addEventListener "message" handle-message))

(object/set js/Module "onRuntimeInitialized" add-event-listener)
