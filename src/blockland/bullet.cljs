(ns blockland.bullet
  (:require [blockland.ammo :refer [ammo]]))

(defn create-bullet-world []
  (let [config (js/Ammo.btDefaultCollisionConfiguration.)
        dispatcher (js/Ammo.btCollisionDispatcher. config)
        broadphase (js/Ammo.btAxisSweep3.
                    (js/Ammo.btVector3. -1000 -1000 -1000)
                    (js/Ammo.btVector3. 1000 1000 1000))
        solver (js/Ammo.btSequentialImpulseConstraintSolver.)
        world (js/Ammo.btDiscreteDynamicsWorld.
               dispatcher broadphase solver config)]
    (.setGravity world (js/Ammo.btVector3. 0 -0.5 0))
    world))

(def max-sub-steps 5)
(def fixed-time-step (/ 1 60))
(defn bullet-system! [world]
  (.stepSimulation world delta-time max-sub-steps fixed-time-step))

(comment

  (create-bullet-world)

  )
