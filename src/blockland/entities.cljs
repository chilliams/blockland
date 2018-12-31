(ns blockland.entities
  (:require [blockland.ammo :as ammo]))

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
  (let [material (js/THREE.MeshNormalMaterial.)
        mesh (js/THREE.Mesh. geometry material)
        col (create-box-shape geometry)
        body (create-body col x y z)]
    (.set (.-position mesh) x y z)
    {:mesh mesh
     :body body}))

(defn create-highlighter []
  (let [geometry (js/THREE.BoxGeometry. 1.01 1.01 1.01)
        material (js/THREE.MeshBasicMaterial. #js {"wireframe" true
                                                   "color" 0x000000})
        mesh (js/THREE.Mesh. geometry material)]
    {:mesh mesh}))

(defn create-ground [x y z]
  (create-static-entity (js/THREE.BoxGeometry. 4 1 4) x y z))

(defn create-wall-horizontal [x y z]
  (create-static-entity (js/THREE.BoxGeometry. 40 20 1) x y z))

(defn create-wall-vertical [x y z]
  (create-static-entity (js/THREE.BoxGeometry. 1 20 40) x y z))

(defn create-character [world x y z]
  (let [ghost-shape (js/Ammo.btCapsuleShape. 0.25 0.75)
        ghost-object (js/Ammo.btPairCachingGhostObject.)]
    (.setWorldTransform ghost-object (ammo/transform x y z))
    (.setCollisionShape ghost-object ghost-shape)
    (.setCollisionFlags ghost-object 16)
    (let [controller (js/Ammo.btKinematicCharacterController.
                      ghost-object
                      ghost-shape
                      0.35)]
      (.addCollisionObject world ghost-object 32 -1)
      (.addAction world controller)
      {:character {:ghost-object ghost-object
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
        geometry (js/THREE.CylinderGeometry. 2 2 6)
        material (js/THREE.MeshNormalMaterial.)
        mesh (js/THREE.Mesh. geometry material)]
    (.set (.-position mesh) x y z)

    {:mesh mesh
     :body body}))

(defn create-chunk-shape [position indices]
  (let [shape (js/Ammo.btTriangleMesh.)]
    (doseq [i (range 0 (.-length position) 3)]
      (.findOrAddVertex shape
                        (js/Ammo.btVector3. (aget position i)
                                            (aget position (+ 1 i))
                                            (aget position (+ 2 i)))
                        false))
    (doseq [i (range 0 (.-length indices) 3)]
      (.addTriangleIndices shape
                           (aget indices i)
                           (aget indices (+ 1 i))
                           (aget indices (+ 2 i))))
    (js/Ammo.btBvhTriangleMeshShape. shape true)))

(defn three-mesh [x y z]
  (let [geometry (js/THREE.BufferGeometry.)
        vertices (js/Float32Array. #js [-1.0, -1.0,  1.0,
	                                      1.0, -1.0,  1.0,
	                                      1.0,  1.0,  1.0,

	                                      1.0,  1.0,  1.0,
	                                      -1.0,  1.0,  1.0,
	                                      -1.0, -1.0,  1.0])
        indices (js/Uint16Array. #js [1 2 3 4 5 6])
        shape (create-chunk-shape vertices indices)
        body (create-body shape x y z)]
    (.addAttribute geometry "position" (js/THREE.BufferAttribute. vertices 3))
    (let [material (js/THREE.MeshBasicMaterial. #js {"color" 0xff000})
          mesh (js/THREE.Mesh. geometry material)]
      (.set (.-position mesh) x y z)
      {:mesh mesh
       :body body})))

(defn create-chunk [data texture]
  (let [id (.-id data)
        indices (.-indices data)
        normal (.-normal data)
        position (.-position data)
        uv (.-uv data)
        geometry (js/THREE.BufferGeometry.)
        shape (create-chunk-shape position indices)
        body (create-body shape 0 0 0)]
    (.setIndex geometry (js/THREE.BufferAttribute. indices 1))
    (.addAttribute geometry "normal" (js/THREE.BufferAttribute. normal 3))
    (.addAttribute geometry "position" (js/THREE.BufferAttribute. position 3))
    (.addAttribute geometry "uv" (js/THREE.BufferAttribute. uv 2))
    (let [material (js/THREE.MeshLambertMaterial.
                    #js {"map" texture})
          mesh (js/THREE.Mesh. geometry material)]
      {:chunk-id (js->clj id)
       :mesh mesh
       :body body})))

(comment

  (create-mesh 0 0 0)

  (js/console.log (js/Ammo.btTriangleMesh.))

  (let [geometry (js/THREE.BoxGeometry. 1 20 40)
        material (js/THREE.MeshPhongMaterial. #js {"color" 0xffffff
                                                   "specular" 0x050505})
        mesh (js/THREE.Mesh. geometry material)]
    (.set (.-position mesh) 1 2 3)
    (js/JSON.stringify (.-position mesh)))

  (js/console.log ammo)

  (js/console.log undefined)

  )
