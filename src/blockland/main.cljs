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

  )
