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

; add a listener for a specific path
(osc-handle server "/1/fader6" (fn [msg] (println msg)))

; define a sine wave
(definst foo [freq 440] (sin-osc freq))

; define the control
(defn control-foo
  [val]
  (let [val (scale-range val 0 1 50 1000)]
    (ctl foo :freq val)))

; compose listener, controller and sine wave
(osc-handle server "/1/fader6" (fn [msg] (control-foo (first (:args msg)))))

; trigger foo
(foo)

(stop)
