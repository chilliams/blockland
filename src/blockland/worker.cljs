(ns blockland.worker
  "Runs as a Web Worker and hands off work to the C++ code"
  (:require [goog.object :as object]))

(enable-console-print!)

(js/importScripts "/js/chunkworker.js")

(defn make-world []
  (let [size 6]
    (doseq [[x z] (sort-by (fn [p] (reduce + (map #(* % %) p)))
                           (for [x (range (- size) size)
                                 z (range (- size) size)]
                             [x z]))]
      (let [result (js/Module.make_chunk #js [x 0 z])]
        (.postMessage js/self
                      #js {:command "mesh"
                           :data result})))))

(defmulti handle-msg #(object/get % "command"))
(defmethod handle-msg
  "remove-block"
  [msg]
  (let [block (object/get msg "data")
        results (js/Module.remove_block block)]
    (doseq [new-mesh results]
      (.postMessage js/self
                    #js {:command "mesh"
                         :data new-mesh}))))

(defn on-message [event]
  (let [msg (.-data event)]
    (handle-msg msg)))

(set! (.-onmessage js/self) on-message)

(object/set js/Module "onRuntimeInitialized" make-world)
