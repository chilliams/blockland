(ns blockland.main
  (:require [blockland.basicdemo :as basicdemo]
            [blockland.bullet :as bullet]
            [blockland.client :as client]
            [blockland.entities :as entities]
            [blockland.firstperson :as firstperson]
            [blockland.gameloop :as gameloop]
            [blockland.player :as player]
            [three :as three]))

(defonce game-state (atom {}))

(defn game-loop! [{:keys [delta-time input]}]
  (let [{:keys [camera scene renderer] :as game} @game-state]
    (.render renderer scene camera)
    (bullet/bullet-system! game delta-time)
    (player/player-system! game delta-time input)))

(defn start-game! []
  (reset! game-state
          (firstperson/init-game)
          ;; (basicdemo/init-game)
          )
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/setup-input-events!)
    (gameloop/run-game! (fn [data] (game-loop! data)))))


(defn add-entity-to-game! [{:keys [scene world] :as game}
                          {:keys [mesh body] :as entity}]
  (when body
    (.addRigidBody world body))
  (when mesh
    (.add scene mesh))
  (update game :entities conj entity))

(defn add-chunk! [data]
  (let [texture-loader (three/TextureLoader.)]
    (.load
     texture-loader
     "/texture.png"
     (fn [texture]
       (swap!
        game-state
        (fn [game]
          (add-entity-to-game! game (entities/create-chunk data texture))))))))

(defn handle-worker-message! [e]
  (let [command (.-command (.-data e))
        data (.-data (.-data e))]
    (when (= command "mesh")
      (js/console.log data)
      (add-chunk! data))))

(defonce worker
  (client/start-worker (fn [e] (handle-worker-message! e))))

(defn init []
  (-> (js/Ammo)
      (.then start-game!)))

(defn reset-game! []
  (let [{:keys [renderer]} @game-state]
    (.remove (.-domElement renderer)))
  (reset! game-state
          (firstperson/init-game)
          ;; (basicdemo/init-game)
          )
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/bind-events-to-canvas! (.-domElement renderer))))

(comment

  (let [{:keys [entities]} @game-state]
    (doseq [{:keys [character]} entities]
      (when-let [{:keys [controller]} character]
        (.setGravity controller 0))))

  (reset-game!)

  (swap!
   game-state
   (fn [game]
     (add-entity-to-game! game (entities/create-mesh 0 0 0))))

  (.postMessage worker "hello world")

  (let [{:keys [camera]} @game-state]
    (.set (.-position camera) 0 50 0)
    (.lookAt camera 0 20 0))


  )
