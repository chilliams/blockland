(ns blockland.main
  (:require [blockland.ammo :refer [ammo]]
            [three :as three]))

(defn init []
  (let [camera (three/PerspectiveCamera.
                70
                (/ js/window.innerWidth js/window.innerHeight)
                0.01
                10)
        scene (three/Scene.)
        geometry (three/BoxGeometry. 0.2 0.2 0.2)
        material (three/MeshNormalMaterial.)
        mesh (three/Mesh. geometry material)
        renderer (three/WebGLRenderer. #js {:antialias true})]
    (set! (-> camera (.-position) (.-z)) 1)
    (.add scene mesh)
    (.setSize renderer js/window.innerWidth js/window.innerHeight)
    (.appendChild (.-body js/document) (.-domElement renderer))
    (letfn [(animate []
              (js/requestAnimationFrame animate)
              (let [rot (.-rotation mesh)
                    x (.-x rot)
                    y (.-y rot)]
                (set! (.-x rot) (+ x 0.01))
                (set! (.-y rot) (+ y 0.02)))
              (.render renderer scene camera))]
      (animate))))
