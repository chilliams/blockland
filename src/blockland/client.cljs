(ns blockland.client)

(defn start-worker [callback]
  (let [worker (js/Worker. "/js/worker.js")]
    (js/console.log worker)
    (set! (.-onmessage worker) callback)
    worker))
