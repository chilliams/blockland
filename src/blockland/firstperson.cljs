(ns blockland.firstperson
  (:require [blockland.entities :as entities]))

(defn create-empty-world []
  (let [config (js/Ammo.btDefaultCollisionConfiguration.)
        dispatcher (js/Ammo.btCollisionDispatcher. config)
        broadphase (js/Ammo.btDbvtBroadphase.)
        solver (js/Ammo.btSequentialImpulseConstraintSolver.)
        world (js/Ammo.btDiscreteDynamicsWorld.
               dispatcher broadphase solver config)
        ghost-pair-callback (js/Ammo.btGhostPairCallback.)]
    (-> world
        (.getPairCache)
        (.setInternalGhostPairCallback ghost-pair-callback))
    (.setGravity world (js/Ammo.btVector3. 0 -1 0))
    world))

(defn init-game []
  (let [camera (js/THREE.PerspectiveCamera.
                90
                (/ js/window.innerWidth js/window.innerHeight)
                0.02
                300)
        scene (js/THREE.Scene.)
        renderer (js/THREE.WebGLRenderer. #js {:antialias true})
        world (create-empty-world)
        entities [(entities/create-ground 0 50 0)
                  ;; (entities/create-wall-horizontal 0 10 -20)
                  ;; (entities/create-wall-horizontal 0 10 20)
                  ;; (entities/create-wall-vertical 20 10 0)
                  ;; (entities/create-wall-vertical -20 10 0)
                  ;; (entities/three-mesh 1 1 1)
                  (assoc (entities/create-character world 0 65 0)
                         :player true)]]
    (.set (.-position camera) 30 40 30)
    (.lookAt camera 0 25 0)
    (doseq [{:keys [mesh body]} entities]
      (when body
        (.addRigidBody world body))
      (when mesh
        (.add scene mesh)))
    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    (.setClearColor renderer 0x87ceeb 1)
    {:camera camera
     :scene scene
     :world world
     :entities entities
     :renderer renderer}))
