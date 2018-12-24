(ns blockland.entities
  (:require [three :as three]))

(defn create-ground [x y z]
  (let [geometry (three/BoxGeometry. 40 1 40)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) x y z)
    {:model mesh}))

(defn create-wall-horizontal [x y z]
  (let [geometry (three/BoxGeometry. 40 20 1)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) x y z)
    {:model mesh}))

(defn create-wall-vertical [x y z]
  (let [geometry (three/BoxGeometry. 1 20 40)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) x y z)
    {:model mesh}))

(comment

  (let [geometry (three/BoxGeometry. 1 20 40)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)]
    (.set (.-position mesh) 1 2 3)
    (js/JSON.stringify (.-position mesh)))

  )
