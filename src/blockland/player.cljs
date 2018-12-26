(ns blockland.player
  (:require [three :as three]))

(defn xyz [v3]
  [(.x v3) (.y v3) (.z v3)])

(defn update-movement! [{:keys [ghost-object controller]}
                        camera
                        delta-time
                        {:keys [mouse keys-pressed]}]

  ;; handle user input
  (let [{:keys [delta-x delta-y]} mouse
        tmp (three/Vector3.)]
    (.rotateOnWorldAxis camera (.-up camera) (- (* 0.002 delta-x)))
    (.getWorldDirection camera tmp)
    (-> tmp
        (.cross (.-up camera))
        (.normalize))
    (.rotateOnWorldAxis camera tmp (- (* 0.002 delta-y))))

  ;; match position of physics simulation
  (let [[x y z] (-> ghost-object
                    (.getWorldTransform)
                    (.getOrigin)
                    (xyz))]
    (.set (.-position camera) x (+ y 1) z)))

(defn player-system! [{:keys [camera entities]} delta-time input]
  (doseq [{:keys [character player]} entities]
    (when player
      (update-movement! character camera delta-time input))))
