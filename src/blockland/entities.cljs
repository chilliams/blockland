(ns blockland.entities
  (:require [three :as three]))

(defn create-box-shape [geometry]
  (.computeBoundingBox geometry)
  (let [box (.-boundingBox geometry)
        mx (.-max box)
        mn (.-min box)
        width (- (.-x mx) (.-x mn))
        height (- (.-y mx) (.-y mn))
        depth (- (.-z mx) (.-z mn))]
    (js/Ammo.btBoxShape.
     (js/Ammo.btVector3. width height depth))))

(defn create-static-entity [geometry x y z]
  (let [material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)
        col (create-box-shape geometry)
        body-info (js/Ammo.btRigidBodyConstructionInfo.
                   0 nil col (js/Ammo.btVector3.))
        body (js/Ammo.btRigidBody. body-info)
        transform (js/Ammo.btTransform.)]
    (.setIdentity transform)
    (.setOrigin transform (js/Ammo.btVector3. x y z))
    (.setMotionState body (js/Ammo.btDefaultMotionState. transform))
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
        transform (js/Ammo.btTransform.)
        ghost-shape (js/Ammo.btCapsuleShape. 2 2)
        ghost-object (js/Ammo.btPairCachingGhostObject.)]
    (.set (.-position mesh) x y z)
    (.setOrigin transform (js/Ammo.btVector3. x y z))
    (.setWorldTransform ghost-object transform)
    (.setCollisionShape ghost-object ghost-shape)
    (.setCollisionFlags ghost-object 16)
    (let [controller (js/Ammo.btKinematicCharacterController.
                      ghost-object
                      ghost-shape
                      0.35)]
      (.addCollisionObject world ghost-object 32 -1)
      (.addAction world controller)
      {:mesh mesh
       :character {:transform transform
                   :ghost-shape ghost-shape
                   :ghost-object ghost-object
                   :controller controller}})))

(comment

  (let [geometry (three/BoxGeometry. 1 20 40)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) 1 2 3)
    (js/JSON.stringify (.-position mesh)))

  (js/console.log ammo)

  (js/console.log undefined)

  )
