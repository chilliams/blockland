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
                           :data result})))))

(defmethod handle-msg
  "add-block"
  [msg]
  (js/console.dir js/Module)
  (let [data (object/get msg "data")
        position (object/get data "position")
        type (object/get data "type")
        material (object/getValueByKeys js/Module "Material" type)
        _ (print {:pos position
                  :mat material})
        results (js/Module.add_block position material)]
    (doseq [new-mesh results]
      (.postMessage js/self
                    #js {:command "mesh"
                         :data new-mesh}))))

(defmethod handle-msg
  "remove-block"
  [msg]
  (let [block (object/get msg "data")
        results (js/Module.remove_block block)]
    (js/console.log "removing a block")
    (doseq [new-mesh results]
      (.postMessage js/self
                    #js {:command "mesh"
                         :data new-mesh}))))

(defmethod handle-msg
  "ping"
  [msg]
  (.postMessage js/self #js {:command "ping"
                             :data #js {:echo msg}}))

(defn on-message [event]
  (let [msg (.-data event)]
    (js/console.log "handle a msg")
    (handle-msg msg)))

(set! (.-onmessage js/self) on-message)

(defn post-wakeup []
  (.postMessage js/self #js {:command "alive"}))

(object/set js/Module "onRuntimeInitialized" post-wakeup)
