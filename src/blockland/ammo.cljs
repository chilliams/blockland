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

(defn hit-block [ray-test-cb]
  (let [side (xyz (.-m_hitNormalWorld ray-test-cb))
        pos (pos? (reduce + side))
        [sx sy sz] side
        adjustment (if pos
                     #(-> % (js/Math.floor) (+ 0.5))
                     #(-> % (js/Math.ceil) (- 0.5)))
        hit-spot (xyz (.-m_hitPointWorld ray-test-cb))
        [x y z] (map adjustment hit-spot)]
    [(- x sx) (- y sy) (- z sz)]))
