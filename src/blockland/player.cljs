(ns blockland.player)

(defn xyz [v3]
  [(.x v3) (.y v3) (.z v3)])

(defn update-movement! [{:keys [ghost-object controller]}
                        camera
                        delta-time
                        keys-pressed]
  ;; (js/console.log (.canJump controller))
  ;; (.setWalkDirection controller (js/Ammo.btVector3. 0.01 0 -0.01))
  (let [[x y z] (-> ghost-object
                    (.getWorldTransform)
                    (.getOrigin)
                    (xyz))]
    (.set (.-position camera) x (+ y 1) z)))

(defn player-system! [{:keys [camera entities]} delta-time keys-pressed]
  (doseq [{:keys [character]} entities]
    (when character
      (update-movement! character camera delta-time keys-pressed))))
