(ns blockland.bullet)

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

(defn body-xyz [v3]
  [(.x v3) (.y v3) (.z v3)])

(defn body-xyzw [quat]
  [(.x quat) (.y quat) (.z quat) (.w quat)])

(defn apply-transform-to-mesh! [transform mesh]
  (let [[x y z] (-> transform
                    (.getOrigin)
                    (body-xyz))
        [qx qy qz qw] (body-xyzw (.getRotation transform))]
    (.set (.-position mesh) x y z)
    (.set (.-quaternion mesh) qx qy qz qw)))

(defn sync-physics-to-graphics! [entities]
  (let [transform (js/Ammo.btTransform.)]
    (doseq [{:keys [mesh body character]} entities]
      (when (and mesh character)
        (let [{:keys [ghost-object]} character
              transform (.getWorldTransform ghost-object)]
          (apply-transform-to-mesh! transform mesh)))
      (when (and mesh body)
        (-> body
            (.getMotionState)
            (.getWorldTransform transform))
        (apply-transform-to-mesh! transform mesh)))))

(def max-sub-steps 5)
(def fixed-time-step (/ 1 60))
(defn bullet-system! [{:keys [world entities]} delta-time]
  (.stepSimulation world delta-time max-sub-steps fixed-time-step)
  (sync-physics-to-graphics! entities))

(comment

  (create-body-world)

  )
