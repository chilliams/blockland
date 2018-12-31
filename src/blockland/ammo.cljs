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


(defn rollup [side]
  (for [i (range 3)]
    (let [x (side i)
          rx (js/Math.round x)]
      (if (< (js/Math.abs (- x rx)) 0.0001)
        rx
        x))))


(defn hit-block [ray-test-cb]
  (let [side (xyz (.-m_hitNormalWorld ray-test-cb))
        pos (pos? (reduce + side))
        [sx sy sz] side
        hit-spot (xyz (.-m_hitPointWorld ray-test-cb))
        ;; correct for floating point
        hit-spot (rollup hit-spot)
        adjustment (if pos
                     #(-> % (js/Math.floor) (+ 0.5))
                     #(-> % (js/Math.ceil) (- 0.5)))
        [x y z] (map adjustment hit-spot)]
    {:add-pos [x y z]
     :remove-pos [(- x sx) (- y sy) (- z sz)]}))
