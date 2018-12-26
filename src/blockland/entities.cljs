(ns blockland.entities
  (:require [blockland.ammo :as ammo]
            [three :as three]))

(defn create-box-shape [geometry]
  (.computeBoundingBox geometry)
  (let [box (.-boundingBox geometry)
        mx (.-max box)
        mn (.-min box)
        width (- (.-x mx) (.-x mn))
        height (- (.-y mx) (.-y mn))
        depth (- (.-z mx) (.-z mn))]
    (js/Ammo.btBoxShape.
     (js/Ammo.btVector3. (/ width 2) (/ height 2) (/ depth 2)))))

(defn create-body [col-shape x y z]
  (let [body-info (js/Ammo.btRigidBodyConstructionInfo.
                   0 nil col-shape (js/Ammo.btVector3.))
        body (js/Ammo.btRigidBody. body-info)
        motion-state (js/Ammo.btDefaultMotionState. (ammo/transform x y z))]
    (.setMotionState body motion-state)
    body))

(defn create-static-entity [geometry x y z]
  (let [material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)
        col (create-box-shape geometry)
        body (create-body col x y z)]
    (.set (.-position mesh) x y z)
    {:mesh mesh
     :body body}))

(defn create-ground [x y z]
  (create-static-entity (three/BoxGeometry. 40 1 40) x y z))

(defn create-wall-horizontal [x y z]
  (create-static-entity (three/BoxGeometry. 40 20 1) x y z))

(defn create-wall-vertical [x y z]
  (create-static-entity (three/BoxGeometry. 1 20 40) x y z))

(defn create-character [world x y z]
  (let [geometry (three/CylinderGeometry. 2 2 6)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)
        ghost-shape (js/Ammo.btCapsuleShape. 2 2)
        ghost-object (js/Ammo.btPairCachingGhostObject.)]
    (.set (.-position mesh) x y z)
    (.setWorldTransform ghost-object (ammo/transform x y z))
    (.setCollisionShape ghost-object ghost-shape)
    (.setCollisionFlags ghost-object 16)
    (let [controller (js/Ammo.btKinematicCharacterController.
                      ghost-object
                      ghost-shape
                      0.35)]
      (.addCollisionObject world ghost-object 32 -1)
      (.addAction world controller)
      {:mesh mesh
       :character {:ghost-object ghost-object
                   :controller controller}})))

(defn create-mesh-shape []
  (let [quad [(js/Ammo.btVector3. 0 1 -1)
              (js/Ammo.btVector3. 0 1 1)
              (js/Ammo.btVector3. 0 -1 1)
              (js/Ammo.btVector3. 0 -1 -1)]
        mesh (js/Ammo.btTriangleMesh.)
        use-quantized-aabb-compression true]
    (.addTriangle mesh (quad 0) (quad 1) (quad 2) true)
    (.addTriangle mesh (quad 0) (quad 2) (quad 3) true)
    (js/Ammo.btBvhTriangleMeshShape. mesh use-quantized-aabb-compression)))

(defn create-mesh [x y z]
  (let [shape (create-mesh-shape)
        body (create-body shape x y z)

        ;; TODO: use correct mesh
        geometry (three/CylinderGeometry. 2 2 6)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) x y z)

    {:mesh mesh
     :body body}))

(comment

  (create-mesh 0 0 0)

  (let [geometry (three/BoxGeometry. 1 20 40)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) 1 2 3)
    (js/JSON.stringify (.-position mesh)))

  (js/console.log ammo)

  (js/console.log undefined)

  )
