(ns blockland.bullet)

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
