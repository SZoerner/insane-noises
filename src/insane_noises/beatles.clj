(ns insane-noises.beatles
  (:use [overtone.live]))

;; We use a saw-wave that we defined in the oscillators tutorial
(definst saw-wave [freq 440 attack 0.01 sustain 0.4 release 0.7 vol 0.4]
  (* (env-gen (env-lin attack sustain release) 1 1 0 1 FREE)
     (saw freq)
     vol))

;; We can play notes using frequency in Hz
(saw-wave 440)
(saw-wave 523.25)
(saw-wave 261.62) ; This is C4

;; We can also play notes using MIDI note values
(saw-wave (midi->hz 69))
(saw-wave (midi->hz 72))
(saw-wave (midi->hz 60)) ; This is C4

;; We can play notes using standard music notes as well
(saw-wave (midi->hz (note :A4)))
(saw-wave (midi->hz (note :C5)))
(saw-wave (midi->hz (note :C4))) ; This is C4! Surprised?

;; Define a function for convenience
(defn note->hz [music-note]
  (midi->hz (note music-note)))

; Slightly less to type
(saw-wave (note->hz :C5))

;; Let's make it even easier
(defn saw2 [music-note]
  (saw-wave (midi->hz (note music-note))))

;; Great!
(saw2 :A4)
(saw2 :C5)
(saw2 :C4)

;; Let's play some chords


;; this is one possible implementation of play-chord
(defn play-chord [a-chord]
  (doseq [note a-chord] (saw2 note)))

;; We can play many types of chords.
;; For the complete list, visit https://github.com/overtone/overtone/blob/master/src/overtone/music/pitch.clj and search for "def CHORD"
(play-chord (chord :C4 :major))

;; We can play a chord progression on the synth
;; using times:
(defn chord-progression-time []
  (let [time (now)]
    (at time (play-chord (chord :C4 :major)))
    (at (+ 2000 time) (play-chord (chord :G3 :major 1)))
    (at (+ 4000 time) (play-chord (chord :A3 :minor 1)))
    (at (+ 6000 time) (play-chord '(60 65 69)))))

(chord-progression-time)
(stop)

;; or beats:
(defonce metro (metronome 120))
(metro)
(defn chord-progression-beat [m beat-num]
  (at (m (+ 0 beat-num)) (play-chord (chord :C4 :major)))
  (at (m (+ 4 beat-num)) (play-chord (chord :G3 :major)))
  (at (m (+ 8 beat-num)) (play-chord (chord :A3 :minor)))
  (at (m (+ 14 beat-num)) (play-chord (chord :F3 :major))))

(chord-progression-beat metro (metro))

;; We can use recursion to keep playing the chord progression
(defn chord-progression-beat [m beat-num]
  (at (m (+ 0 beat-num)) (play-chord (chord :C4 :major)))
  (at (m (+ 4 beat-num)) (play-chord (chord :G3 :major)))
  (at (m (+ 8 beat-num)) (play-chord (chord :A3 :minor)))
  (at (m (+ 12 beat-num)) (play-chord (chord :F3 :major)))
  (apply-at (m (+ 16 beat-num)) chord-progression-beat m (+ 16 beat-num) []))

(chord-progression-beat metro (metro))
