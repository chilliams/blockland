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
               dispatcher broadphase solver config)]
    (.setGravity world (js/Ammo.btVector3. 0 -0.5 0))
    world))

(defn bullet-xyz [v3]
  [(.x v3) (.y v3) (.z v3)])

(defn bullet-xyzw [quat]
  [(.x quat) (.y quat) (.z quat) (.w quat)])

(defn sync-physics-to-graphics [entities]
  (let [transform (js/Ammo.btTransform.)]
    (doseq [{:keys [model bullet]} entities]
      (when (and model bullet)
        (-> bullet
            (.getMotionState)
            (.getWorldTransform transform))
        (let [[x y z] (-> transform
                          (.getOrigin)
                          (bullet-xyz))
              [qx qy qz qw] (bullet-xyzw (.getRotation transform))]
          (.set (.-position model) x y z)
          (.set (.-quaternion model) qx qy qz qw))))))

(def max-sub-steps 5)
(def fixed-time-step (/ 1 60))
(defn bullet-system! [{:keys [world entities]} delta-time]
  (.stepSimulation world delta-time max-sub-steps fixed-time-step)
  (sync-physics-to-graphics entities))

(comment

  (create-bullet-world)

  )
