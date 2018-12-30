(ns blockland.startup
  (:require [blockland.client :as client]))

(defonce dependencies (atom {}))

(defn check-deps [{:keys [ammo texture worker]}]
  (and ammo texture worker))

(defn add-dep! [key value done-loading!]
  (swap! dependencies assoc key value)
  (let [deps @dependencies]
    (when (check-deps deps)
      (done-loading! deps))))

(defn start-dependencies! [{:keys [done-loading!
                                   handle-worker-message!]}]
  (-> (js/Ammo)
      (.then
       (fn []
         (print "loaded ammo")
         (add-dep! :ammo true done-loading!))))

  (let [texture-loader (js/THREE.TextureLoader.)]
    (.load
     texture-loader
     "/texture.png"
     (fn [texture]
       (print "loaded texture")
       (set! (.-minFilter texture) js/THREE.LinearFilter)
       (set! (.-magFilter texture) js/THREE.NearestFilter)
       (add-dep! :texture texture done-loading!))))

  (client/start-worker!
   {:callback (fn [worker]
                (print "started worker")
                (add-dep! :worker worker done-loading!))
    :onmessage handle-worker-message!}))
