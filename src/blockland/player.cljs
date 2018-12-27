(ns blockland.player
  (:require [blockland.ammo :as ammo]
            [three :as three]))

(defn update-movement! [{:keys [ghost-object controller]}
                        camera
                        delta-time
                        {:keys [mouse keys-pressed]}]

  ;; move camera
  (let [{:keys [delta-x delta-y]} mouse
        tmp (three/Vector3.)]
    (.rotateOnWorldAxis camera (.-up camera) (- (* 0.002 delta-x)))
    (.getWorldDirection camera tmp)
    (-> tmp
        (.cross (.-up camera))
        (.normalize))
    (.rotateOnWorldAxis camera tmp (- (* 0.002 delta-y))))

  ;; make physics object face correct direction
  (let [old-transform (.getWorldTransform ghost-object)
        translation (.getOrigin old-transform)
        tmp (.getWorldDirection camera (three/Vector3.))
        quat (js/Ammo.btQuaternion. (.-x tmp) (.-y tmp) (.-z tmp) 0)
        new-transform (js/Ammo.btTransform. quat translation)]
    (.setWorldTransform ghost-object new-transform))

  (let [walk-direction (js/Ammo.btVector3. 0 0 0)
        camera-direction (-> camera
                             (.getWorldDirection (three/Vector3.))
                             (.setY 0)
                             (.normalize)
                             (ammo/three-v3-to-bullet-v3))
        tmp (three/Vector3. 0 0 0)]
    (when (keys-pressed "w")
      (.op_add walk-direction camera-direction))
    (when (keys-pressed "s")
      (.op_sub walk-direction camera-direction))
    (when-not (and (keys-pressed "a") (keys-pressed "d"))
      (when (keys-pressed "a")
        (.getWorldDirection camera tmp)
        (-> tmp
            (.cross (.-up camera))
            (.normalize)
            (.multiplyScalar -1)))
      (when (keys-pressed "d")
        (.getWorldDirection camera tmp)
        (-> tmp
            (.cross (.-up camera))
            (.normalize)
            (.multiplyScalar 1))))
    (.op_add walk-direction (ammo/three-v3-to-bullet-v3 tmp))
    (.op_mul walk-direction (* 10 delta-time))
    (.setWalkDirection controller walk-direction)

    (when (keys-pressed " ")
      (.setJumpSpeed controller 15)
      (.jump controller)))

  ;; match camera position to physics simulation
  (let [[x y z] (-> ghost-object
                    (.getWorldTransform)
                    (.getOrigin)
                    (ammo/xyz))]
    (.set (.-position camera) x (+ y 40) z)))

(defn player-system! [{:keys [camera entities]} delta-time input]
  (doseq [{:keys [character player]} entities]
    (when player
      (update-movement! character camera delta-time input)
      )))
