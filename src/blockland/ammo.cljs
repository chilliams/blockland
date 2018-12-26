(ns blockland.ammo)

(defn transform [x y z]
  (let [origin (js/Ammo.btVector3. x y z)
        transform (js/Ammo.btTransform.)]
    (.setIdentity transform)
    (.setOrigin transform origin)
    transform))
