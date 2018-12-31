(ns blockland.worker
  "Runs as a Web Worker and hands off work to the C++ code"
  (:require [goog.object :as object]))

(enable-console-print!)

(js/importScripts "/js/chunkworker.js")

(defmulti handle-msg #(object/get % "command"))
(defmethod handle-msg
  "make-world"
  [msg]
  (let [size (object/get msg "data")]
    (doseq [[x z] (sort-by (fn [p] (reduce + (map #(* % %) p)))
                           (for [x (range (- size) size)
                                 z (range (- size) size)]
                             [x z]))]
      (let [result (js/Module.make_chunk #js [x 0 z])]
        (.postMessage js/self
                      #js {:command "mesh"
                           :data #js [result]})))))

(defmethod handle-msg
  "add-block"
  [msg]
  (let [data (object/get msg "data")
        position (object/get data "position")
        type (object/get data "type")
        material (object/getValueByKeys js/Module "Material" type)
        results (js/Module.add_block position material)]
    (.postMessage js/self
                  #js {:command "mesh"
                       :data results})))

(defmethod handle-msg
  "remove-block"
  [msg]
  (let [block (object/get msg "data")
        results (js/Module.remove_block block)]
    (.postMessage js/self
                  #js {:command "mesh"
                       :data results})))

(defmethod handle-msg
  "ping"
  [msg]
  (.postMessage js/self #js {:command "ping"
                             :data #js {:echo msg}}))

(defn on-message [event]
  (let [msg (.-data event)]
    (handle-msg msg)))

(set! (.-onmessage js/self) on-message)

(defn post-wakeup []
  (.postMessage js/self #js {:command "alive"}))

(object/set js/Module "onRuntimeInitialized" post-wakeup)
