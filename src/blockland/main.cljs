(ns blockland.main
  (:require [blockland.ammo :refer [ammo]]
            [blockland.entities :as entities]
            [three :as three]))

(defonce game-state (atom {}))

(defn create-game-state []
  (let [camera (three/PerspectiveCamera.
                70
                (/ js/window.innerWidth js/window.innerHeight)
                1
                300)
        scene (three/Scene.)
        renderer (three/WebGLRenderer. #js {:antialias true})
        entities [(entities/create-ground 0 0 0)
                  (entities/create-wall-horizontal 0 10 -20)
                  (entities/create-wall-horizontal 0 10 20)
                  (entities/create-wall-vertical 20 10 0)
                  (entities/create-wall-vertical -20 10 0)]]
    (set! (-> camera (.-position) (.-x)) 30)
    (set! (-> camera (.-position) (.-y)) 40)
    (set! (-> camera (.-position) (.-z)) 30)
    (.lookAt camera 0 0 0)
    (doseq [{:keys [model]} entities]
      (when model
        (.add scene model)))
    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    {:camera camera
     :scene scene
     :entities entities
     :renderer renderer}))

(defn game-loop! []
  (let [{:keys [camera scene renderer]} @game-state]
    (.render renderer scene camera)))

(defn animate! []
  (js/requestAnimationFrame animate!)
  (game-loop!))

(defn init []
  (reset! game-state (create-game-state))
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (animate!)))

(defn reset-game! []
  (let [{:keys [renderer]} @game-state]
    (.remove (.-domElement renderer)))
  (reset! game-state (create-game-state))
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))))

(comment

  (reset-game!)

  )
