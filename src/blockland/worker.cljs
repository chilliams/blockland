(ns blockland.worker
  "Runs as a Web Worker and hands off work to the C++ code"
  (:require [goog.object :as object]))

(enable-console-print!)

(js/self.addEventListener
 "message"
 (fn [^js e]
   (js/postMessage (.. e -data))))
