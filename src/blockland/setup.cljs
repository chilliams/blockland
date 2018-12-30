(ns blockland.setup
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
                100000)
        scene (js/THREE.Scene.)
        renderer (js/THREE.WebGLRenderer. #js {:antialias true})
        world (create-empty-world)
        highlighter (entities/create-highlighter)
        entities [highlighter
                  (assoc (entities/create-character world 0 25 0)
                         :player true)]]

    (set! (.-background scene) (js/THREE.Color. 0xccffff))
    (set! (.-fog scene) (js/THREE.FogExp2. 0xccffff 0.01))
    (let [ambientLight (js/THREE.AmbientLight. 0x999999)
          directionalLight (js/THREE.DirectionalLight. 0xffffff 0.7)]
      (.add scene ambientLight)
      (-> (.-position directionalLight)
          (.set 0.7 1 0.4)
          (.normalize))
      (.add scene directionalLight))

    (.set (.-position camera) 30 40 30)
    (.lookAt camera 0 25 0)

    (doseq [{:keys [mesh body]} entities]
      (when body
        (.addRigidBody world body))
      (when mesh
        (.add scene mesh)))

    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    (.setClearColor renderer 0xccffff 1)

    {:camera camera
     :scene scene
     :world world
     :entities entities
     :renderer renderer
     :highlighter (:mesh highlighter)}))
