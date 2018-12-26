(ns blockland.firstperson
  (:require [blockland.entities :as entities]
            [three :as three]))

(defn create-bullet-world []
  (let [config (js/Ammo.btDefaultCollisionConfiguration.)
        dispatcher (js/Ammo.btCollisionDispatcher. config)
        broadphase (js/Ammo.btDbvtBroadphase.)
        ;; broadphase (js/Ammo.btAxisSweep3.
        ;;             (js/Ammo.btVector3. -1000 -1000 -1000)
        ;;             (js/Ammo.btVector3. 1000 1000 1000))
        solver (js/Ammo.btSequentialImpulseConstraintSolver.)
        world (js/Ammo.btDiscreteDynamicsWorld.
               dispatcher broadphase solver config)
        ghost-pair-callback (js/Ammo.btGhostPairCallback.)]
    (-> world
        (.getPairCache)
        (.setInternalGhostPairCallback ghost-pair-callback))
    (.setGravity world (js/Ammo.btVector3. 0 -0.5 0))
    world))

(defn init-game []
  (let [camera (three/PerspectiveCamera.
                70
                (/ js/window.innerWidth js/window.innerHeight)
                1
                300)
        scene (three/Scene.)
        renderer (three/WebGLRenderer. #js {:antialias true})
        world (create-bullet-world)
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
