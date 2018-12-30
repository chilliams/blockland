(ns blockland.client
  (:require [goog.object :as object]))

(defn start-worker! [{:keys [callback onmessage]}]
  (let [worker (js/Worker. "/js/worker.js")]
    (set!
     (.-onmessage worker)
     (fn [e]
       (let [command (object/getValueByKeys e "data" "command")]
         (if (= command "alive")
           (callback worker)
           (onmessage e)))))
    worker))
