(ns blockland.ammo)

(defn three-v3-to-bullet-v3 [tv3]
  (js/Ammo.btVector3. (.-x tv3) (.-y tv3) (.-z tv3)))

(defn xyz [v3]
  [(.x v3) (.y v3) (.z v3)])

(defn xyzw [quat]
  [(.x quat) (.y quat) (.z quat) (.w quat)])

(defn transform [x y z]
  (let [origin (js/Ammo.btVector3. x y z)
        transform (js/Ammo.btTransform.)]
    (.setIdentity transform)
    (.setOrigin transform origin)
    transform))
