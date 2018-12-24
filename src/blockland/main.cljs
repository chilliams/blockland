(ns blockland.main
  (:require [blockland.gameloop :as gameloop]
            [blockland.entities :as entities]
            [blockland.bullet :as bullet]
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
        world (bullet/create-bullet-world)
        entities [(entities/create-ground 0 0 0)
                  (entities/create-wall-horizontal 0 10 -20)
                  (entities/create-wall-horizontal 0 10 20)
                  (entities/create-wall-vertical 20 10 0)
                  (entities/create-wall-vertical -20 10 0)
                  (entities/create-character world 5 10 5)]]
    (set! (-> camera (.-position) (.-x)) 30)
    (set! (-> camera (.-position) (.-y)) 40)
    (set! (-> camera (.-position) (.-z)) 30)
    (.lookAt camera 0 0 0)
    (doseq [{:keys [model bullet]} entities]
      (when bullet
        (.addRigidBody world (:body bullet)))
      (when model
        (.add scene model)))
    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    {:camera camera
     :scene scene
     :world world
     :entities entities
     :renderer renderer}))

(defn game-loop! [{:keys [delta-time keys-pressed]}]
  (let [{:keys [camera scene renderer world]} @game-state]
    (.render renderer scene camera)
    (bullet/bullet-system! world)))

(defn init []
  (reset! game-state (create-game-state))
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/run-game! (fn [data] (game-loop! data)))))

(defn reset-game! []
  (let [{:keys [renderer]} @game-state]
    (.remove (.-domElement renderer)))
  (reset! game-state (create-game-state))
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))))

(comment

  (reset-game!)

  )
