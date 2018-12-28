(ns blockland.basicdemo
  (:require [blockland.ammo :as ammo]))

(defn create-empty-world []
  (let [config (js/Ammo.btDefaultCollisionConfiguration.)
        dispatcher (js/Ammo.btCollisionDispatcher. config)
        broadphase (js/Ammo.btDbvtBroadphase.)
        solver (js/Ammo.btSequentialImpulseConstraintSolver.)
        world (js/Ammo.btDiscreteDynamicsWorld.
               dispatcher broadphase solver config)]
    (.setGravity world (js/Ammo.btVector3. 0 -10 0))
    world))

(defn create-rigid-body [{:keys [mass transform shape color]
                          :or {color (js/Ammo.btVector4. 1 0 0 1)}}]
  (let [is-dynamic (not= mass 0)
        local-inertia (js/Ammo.btVector3. 0 0 0)
        _ (when is-dynamic
            (.calculateLocalInertia shape mass local-inertia))
        motion-state (js/Ammo.btDefaultMotionState. transform)
        cinfo (js/Ammo.btRigidBodyConstructionInfo.
               mass motion-state shape local-inertia)
        body (js/Ammo.btRigidBody. cinfo)]
    body))

(defn create-box-shape [x y z]
  (js/Ammo.btBoxShape. (js/Ammo.btVector3. x y z)))

(defn create-box-mesh [w h d x y z]
  (let [geometry (js/THREE.BoxGeometry. w h d)
        material (js/THREE.MeshNormalMaterial.)
        mesh (js/THREE.Mesh. geometry material)]
    (.set (.-position mesh) x y z)
    mesh))

(defn create-ground []
  {:mesh (create-box-mesh 100 100 100
                          0 -56 0)
   :body (create-rigid-body {:mass 0
                             :transform (ammo/transform 0 -56 0)
                             :shape (create-box-shape 50 50 50)
                             :color (js/Ammo.btVector4. 0 0 1 1)})})

(defn create-blocks []
  (let [box-shape (create-box-shape 1 1 1)
        start-transform (js/Ammo.btTransform.)
        mass 1
        local-inertia (js/Ammo.btVector3. 0 0 0)]
    (.setIdentity start-transform)
    (.calculateLocalInertia box-shape mass local-inertia)
    (for [k (range 5)
          i (range 5)
          j (range 5)]
      (let [x (* 2 i)
            y (* 10 k)
            z (* 2 j)]
        (.setOrigin start-transform (js/Ammo.btVector3. x y z))
        {:mesh (create-box-mesh 2 2 2
                                  x y z)
         :body (create-rigid-body {:mass mass
                                     :transform start-transform
                                     :shape box-shape})}))))

(defn init-game []
  (let [camera (js/THREE.PerspectiveCamera.
                90
                (/ js/window.innerWidth js/window.innerHeight)
                1
                3000)
        scene (js/THREE.Scene.)
        renderer (js/THREE.WebGLRenderer. #js {:antialias true})
        world (create-empty-world)
        entities (conj (create-blocks) (create-ground))]
    (.set (.-position camera) 10 10 15)
    (.lookAt camera 0 0 0)
    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    (doseq [{:keys [mesh body]} entities]
      (when body
        (.addRigidBody world body))
      (when mesh
        (.add scene mesh)))
    {:camera camera
     :scene scene
     :renderer renderer
     :world world
     :entities entities}))

(comment

  (init-game)

  )
