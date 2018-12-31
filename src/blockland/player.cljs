(ns blockland.player
  (:require [blockland.ammo :as ammo]))

(defn update-movement! [{:keys [ghost-object controller]}
                        {:keys [camera world highlighter]}
                        delta-time
                        {:keys [mouse keys-pressed]}
                        {:keys [focus-block!]}]

  ;; move camera
  (let [{:keys [delta-x delta-y]} mouse
        tmp (js/THREE.Vector3.)]
    (.rotateOnWorldAxis camera (.-up camera) (- (* 0.002 delta-x)))
    (.getWorldDirection camera tmp)
    (-> tmp
        (.cross (.-up camera))
        (.normalize))
    (.rotateOnWorldAxis camera tmp (- (* 0.002 delta-y))))

  (let [walk-direction (js/Ammo.btVector3. 0 0 0)
        camera-direction (-> camera
                             (.getWorldDirection (js/THREE.Vector3.))
                             (.setY 0)
                             (.normalize)
                             (ammo/three-v3-to-bullet-v3))
        tmp (js/THREE.Vector3. 0 0 0)]
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
    (.op_mul walk-direction (* 3 delta-time))
    (.setWalkDirection controller walk-direction)

    (when (keys-pressed " ")
      (.setJumpSpeed controller 10)
      (.jump controller))

    ;; match camera position to physics simulation
    (let [[x y z] (-> ghost-object
                      (.getWorldTransform)
                      (.getOrigin)
                      (ammo/xyz))]
      ;; y + 3 so camera is at the top of the object
      (.set (.-position camera) x (+ y 0.75) z))

    ;; raycast to hit block
    (let [ray-from (ammo/three-v3-to-bullet-v3 (.-position camera))
          ray-to (-> camera
                     (.getWorldDirection (js/THREE.Vector3.))
                     (ammo/three-v3-to-bullet-v3)
                     (.op_mul 3)
                     (.op_add ray-from))
          ray-test-cb (js/Ammo.btKinematicClosestNotMeRayResultCallback.
                       ghost-object)]
      (set! (.-m_rayFromWorld ray-test-cb) ray-from)
      (set! (.-m_rayToWorld ray-test-cb) ray-to)
      (.rayTest world ray-from ray-to ray-test-cb)
      (if (.hasHit ray-test-cb)
        (let [{:keys [remove-pos add-pos]} (ammo/hit-block ray-test-cb)
              [x y z] remove-pos]
          (.set (.-position highlighter) x y z)
          (focus-block! {:add-block (map #(- % 0.5) add-pos)
                         :remove-block (map #(- % 0.5) remove-pos)}))
        (do (.set (.-position highlighter) 0 0 0)
            (focus-block! nil))))))

(defn player-system! [{:keys [entities] :as game} delta-time input events]
  (doseq [{:keys [character player]} entities]
    (when player
      (update-movement! character game delta-time input events))))
