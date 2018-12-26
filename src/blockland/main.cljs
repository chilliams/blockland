(ns blockland.main
  (:require [blockland.basicdemo :as basicdemo]
            [blockland.gameloop :as gameloop]
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
    (.set (.-position camera) 30 40 30)
    (.lookAt camera 0 0 0)
    (doseq [{:keys [mesh body]} entities]
      (when body
        (.addRigidBody world body))
      (when mesh
        (.add scene mesh)))
    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    {:camera camera
     :scene scene
     :world world
     :entities entities
     :renderer renderer}))

(defn game-loop! [{:keys [delta-time keys-pressed]}]
  (let [{:keys [camera scene renderer] :as game} @game-state]
    (.render renderer scene camera)
    (bullet/bullet-system! game delta-time)))

(defn start-game! []
  (reset! game-state
          (create-game-state)
          ;; (basicdemo/init-game)
          )
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/run-game! (fn [data] (game-loop! data)))))

(defn init []
  (-> (js/Ammo)
      (.then start-game!)))

(defn reset-game! []
  (let [{:keys [renderer]} @game-state]
    (.remove (.-domElement renderer)))
  (reset! game-state
          (create-game-state)
          ;; (basicdemo/init-game)
          )
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))))

(defn body-xyz [v3]
  [(.x v3) (.y v3) (.z v3)])

(comment

  (reset-game!)

  (let [{:keys [world entities camera]} @game-state
        {:keys [mesh body]} (second entities)
        mesh-pos (.-position mesh)
        transform (js/Ammo.btTransform.)
        _ (-> body
              (.getMotionState)
              (.getWorldTransform transform))
        body-pos (-> transform
                       (.getOrigin)
                       (body-xyz))]
    (.set (.-position camera) 10 10 15)
    (.lookAt camera 0 0 0)
    (js/console.log mesh-pos)
    (print body-pos)
    (js/console.log body)
    (js/console.log world))


  )
