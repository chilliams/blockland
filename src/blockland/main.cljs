(ns blockland.main
  (:require [blockland.basicdemo :as basicdemo]
            [blockland.bullet :as bullet]
            [blockland.client :as client]
            [blockland.entities :as entities]
            [blockland.gameloop :as gameloop]
            [blockland.player :as player]
            [blockland.setup :as setup]))

(defonce game-state (atom {}))

(defn find-first [f coll]
  (first (filter f coll)))

(defn add-entity-to-game! [{:keys [scene world entities] :as game}
                           {:keys [chunk mesh body] :as entity}]

  (defn chunk-match [entity]
    (= chunk (entity :chunk)))

  (defn filter-chunk [entities chunk]
    (filter #(not (chunk-match %)) entities))

  (let [game (if chunk
               (update game :entities filter-chunk chunk)
               game)
        ripped (filter chunk-match entities)]
    (doseq [{:keys [body mesh]} ripped]
      (when body
        (.removeRigidBody world body))
      (when mesh
        (.remove scene mesh)))

    (when body
      (.addRigidBody world body))
    (when mesh
      (.add scene mesh))
    (update game :entities conj entity)))

(defonce texture
  (let [texture-loader (js/THREE.TextureLoader.)]
    (.load
     texture-loader
     "/texture.png"
     (fn [texture]
       (set! (.-minFilter texture) js/THREE.LinearFilter)
       (set! (.-magFilter texture) js/THREE.NearestFilter)))))

(defn add-chunk! [data]
  (swap!
   game-state
   (fn [game]
     (add-entity-to-game! game (entities/create-chunk data texture)))))

(defn handle-worker-message! [e]
  (let [command (.-command (.-data e))
        data (.-data (.-data e))]
    (when (= command "mesh")
      (add-chunk! data))))

(defonce worker
  (client/start-worker (fn [e] (handle-worker-message! e))))

(defn focus-block! [block]
  (if block
    (swap! game-state assoc :focused-block block)
    (swap! game-state dissoc :focused-block)))

(defn remove-block! []
  (when-let [block (@game-state :focused-block)]
    (let [msg #js {:command "remove-block"
                   :data (clj->js block)}]
      (.postMessage worker msg))))

(def events {:focus-block! focus-block!
             :remove-block! remove-block!})

(defn game-loop! [{:keys [delta-time input]}]
  (let [{:keys [camera scene renderer] :as game} @game-state]
    (.render renderer scene camera)
    (bullet/bullet-system! game delta-time)
    (player/player-system! game delta-time input events)))

(defn start-game! []
  (reset! game-state
          (setup/init-game)
          ;; (basicdemo/init-game)
          )
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/setup-input-events! (.-domElement renderer) events)
    (gameloop/run-game! (fn [data] (game-loop! data)))))

(defn init []
  (-> (js/Ammo)
      (.then start-game!)))

(defn reset-game! []
  (let [{:keys [renderer]} @game-state]
    (.remove (.-domElement renderer)))
  (reset! game-state
          (setup/init-game)
          ;; (basicdemo/init-game)
          )
  (let [{:keys [renderer]} @game-state]
    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/bind-events-to-canvas!
     (.-domElement renderer)
     {:remove-block! remove-block!})))

(comment

  (let [{:keys [entities]} @game-state]
    (doseq [{:keys [character]} entities]
      (when-let [{:keys [controller]} character]
        (.setGravity controller 0))))

  (reset-game!)

  (swap!
   game-state
   (fn [game]
     (add-entity-to-game! game (entities/create-mesh 0 40 0))))

  (.postMessage worker "hello world")

  (let [{:keys [camera]} @game-state]
    (.set (.-position camera) 0 50 0)
    (.lookAt camera 0 20 0))

  (let [{:keys [renderer]} @game-state]
    (.setClearColor renderer 0x87ceeb))


  (let [{:keys [scene]} @game-state
        ambientLight (js/THREE.AmbientLight. 0xbbbbbb)
        directionalLight (js/THREE.DirectionalLight. 0xffffff 0.5)]
    (.add scene ambientLight)
    (-> (.-position directionalLight)
        (.set 1 1 0.6)
        (.normalize))
    (.add scene directionalLight))


  (let [{:keys [scene]} @game-state]
    (set! (.-background scene) (js/THREE.Color. 0xccffff))
    (set! (.-fog scene) (js/THREE.FogExp2. 0xccffff 0.01)))

  (print (:focused-block @game-state))

  (-> @game-state
      (:entities)
      (nth 9))

  )
