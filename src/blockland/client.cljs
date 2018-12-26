(ns blockland.client)

(defonce worker
  (let [worker (js/Worker. "/js/worker.js")]
    (js/console.log worker)
    (set! (.-onmessage worker) (fn [e] (js/console.log e)))
    worker))

(.postMessage worker "hello world")
