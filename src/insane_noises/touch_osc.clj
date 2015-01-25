(ns insane-noises.touch-osc
  (:use overtone.live))
; to control overtone with the Touch OSC iPad app
; following https://github.com/overtone/overtone/wiki/TouchOSC
; creating a server instance
(def server (osc-server 44100 "osc-clj"))
; broadcast the server's network address
(zero-conf-on)
(zero-conf-off)

; listen for incoming messages
(osc-listen server (fn [msg] (println msg)) :debug)

; remove the listener
(osc-rm-listener server :debug)

; add
