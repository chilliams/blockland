(ns blockland.main
  (:require [blockland.bullet :as bullet]
            [blockland.entities :as entities]
            [blockland.gameloop :as gameloop]
            [blockland.player :as player]
            [blockland.setup :as setup]
            [blockland.startup :as startup]
            [goog.array :as garray]
            [goog.dom :as dom]))

(defonce game-state (atom {}))

(defn find-first [f coll]
  (first (filter f coll)))

(defn chunk-match [entity chunk-ids]
  (let [entity-chunk-id (entity :chunk-id)
        result (chunk-ids entity-chunk-id)]
    result))

(defn remove-chunks-to-replace [entities chunk-ids]
  (filter (fn [entity] (not (chunk-match entity chunk-ids))) entities))

(defn add-chunks-to-game! [{:keys [scene world entities texture] :as game}
                           chunk-datas]

  (let [chunks (js->clj
                (garray/map chunk-datas
                            (fn [chunk-data]
                              (entities/create-chunk chunk-data texture))))
        chunk-ids (set (map :chunk-id chunks))
        game (update game :entities remove-chunks-to-replace chunk-ids)
        ripped (filter (fn [e] (chunk-match e chunk-ids)) entities)]

    (doseq [{:keys [body mesh chunk-id]} ripped]
      (.removeRigidBody world body)
      (js/Ammo.destroy body)
      (.remove scene mesh)
      (.dispose (.-geometry mesh))
      (.dispose (.-material mesh)))

    (doseq [{:keys [body mesh]} chunks]
      (when body
        (.addRigidBody world body))
      (when mesh
        (.add scene mesh)))

    (update game :entities concat chunks)))

(defn add-chunks! [chunks]
  (swap!
   game-state
   (fn [{:keys [texture] :as game}]
     (add-chunks-to-game! game chunks))))

(defn handle-worker-message! [e]
  (let [command (.-command (.-data e))
        data (.-data (.-data e))]
    (when (= command "mesh")
      (add-chunks! data))))

(defn focus-block! [block]
  (if block
    (swap! game-state assoc :focused-block block)
    (swap! game-state dissoc :focused-block)))

(defn game-loop! [{:keys [delta-time input]} events]
  (let [{:keys [camera scene renderer] :as game} @game-state]
    (.render renderer scene camera)
    (bullet/bullet-system! game delta-time)
    (player/player-system! game delta-time input events)))

(defn start-game! [{:keys [texture worker] :as dependencies}]
  (.remove (dom/getElement "loading"))

  (reset! game-state (merge (setup/init-game) dependencies))

  (.postMessage worker #js {:command "make-world" :data 10})

  (let [remove-block! (fn []
                        (when-let [block (@game-state :focused-block)]
                          (let [msg #js {:command "remove-block"
                                         :data (clj->js block)}]
                            (.postMessage worker msg))))

        events {:focus-block! focus-block!
                :remove-block! remove-block!}

        {:keys [renderer]} @game-state]

    (.appendChild (.-body js/document) (.-domElement renderer))
    (gameloop/setup-input-events! (.-domElement renderer) events)
    (gameloop/run-game! (fn [data] (game-loop! data events)))))

(defn init []
  (startup/start-dependencies!
   {:done-loading! start-game!
    :handle-worker-message! handle-worker-message!}))

(comment

  (let [{:keys [entities]} @game-state]
    (doseq [{:keys [character]} entities]
      (when-let [{:keys [controller]} character]
        (.setGravity controller 0))))

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
