(ns insane-noises.giorgio
  (:use overtone.live))

(defsynth saw-wave [freq 440 attack 0.01 sustain 0.03 release 0.1 amp 0.8 out-bus 0]
  (let [env  (env-gen (env-lin attack sustain release) 1 1 0 1 FREE)
        src  (mix (saw [freq (* 1.01 freq)]))
        src  (lpf src (mouse-y 100 2000))
        sin  (sin-osc (* 2 freq))
        sin2 (sin-osc freq)
        src  (mix [src sin sin2])]
    (out out-bus (* src env amp))))

(defonce saw-b (audio-bus))

(defsynth fx [in-bus 0]
  (out 0 (pan2 (g-verb (in in-bus) 20 0.5))))

(def fx-s (fx [:head (foundation-safe-post-default-group)] :in-bus saw-b))
(kill fx-s)

(defn saw2 [music-note]
  (saw-wave (midi->hz music-note) :out-bus saw-b))

(definst tick [freq 560 dur 0.1 width 0.5]
  (let [freq-env (* freq (env-gen (perc 0 (* 0.99 dur))))
        env      (env-gen (perc 0.01 dur) 1 1 0 1 FREE)
        sqr      (* (env-gen (perc 0 0.001)) (pulse (* 2 freq) width))
        src      (sin-osc freq-env)
        drum     (+ sqr (* env src))]
    (hpf (compander drum drum 0.2 0.3 0.01 0.1 0.01) (mouse-x 200 1000))))

(tick)

(def repetition-sub-a (map note [:C5, :A3, :B4, :A3, :C5, :E5, :A3, :A4, :C5, :A3, :B4, :A3, :C5, :A4]))
(def repetition-a (concat (map note [:A4, :A3]) repetition-sub-a (map note [:A3, :A4]) repetition-sub-a))

(def repetition-b  (map note [:F4, :F4, :A4, :F4, :G4, :F4, :A4, :C5, :F4, :F4, :A4, :F4, :G4, :F4, :A4, :F4]))

;; slight variation of the above with different distances between the 2nd and 3rd note
(def repetition-b3 (map note [:E4, :E4, :G4, :E4, :F#3, :E4, :G4, :B4, :E4, :E4, :G4, :E4, :F#3, :E4, :G4, :E4]))

(defn transpose [updown notes]
  (map #(+ updown %1) notes))

(def theme  (concat
              repetition-a
              (transpose -5 repetition-a)
              repetition-a
              (transpose -5 repetition-a)
              repetition-b
              (transpose 2 repetition-b)
              (transpose -2 repetition-b3)
              repetition-b3
              repetition-b
              (transpose 2 repetition-b)
              repetition-b3
              repetition-b3))

(def score (concat
            (concat (drop-last theme) [(note :A4)])
            theme
            (concat (drop-last theme) [(note :A4)])
            (concat (drop-last theme) [(note :A4)])))

(def metro (metronome (* 4 113)))
(metro)

(defn saw-player
  [beat notes multiplier]
  (let [n     (first notes)
        notes (next notes)
        next-beat (inc beat)]
    (when n
      (at (metro beat)
          (saw2 n))
      (apply-at  (metro next-beat) #'saw-player [next-beat notes multiplier])
      )))

(defn tick-player
  [beat multiplier count]
  (at (metro beat) (tick))
  (when (> count 0 ) (apply-at (metro (+ multiplier beat)) #'tick-player [(+ multiplier beat) multiplier (dec count)]))
  )

(saw-player (metro) repetition-a 1)
(saw-player (metro) repetition-b 1)
(saw-player (metro) (transpose 12 repetition-a) 1)

(tick-player (metro) 4 8)
(saw-player (metro) score 1)

(defn startplay []
  (tick-player (metro) 4 8)
  (saw-player (+ (metro) 32) score 1))

(startplay)
(stop)
(show-graphviz-synth fx)

(definst kick [amp 0.5 decay 0.6 freq 65]
  (* (sin-osc freq (* Math/PI 0.5))
     (env-gen (perc 0 decay) 1 1 0 1 FREE)
          amp))


(kick)


(demo 15
      (let [freqs [220 440 660 880 1110 1320]
            muls  [1   1/2 1/3 1/4 1/5  1/6]
            mk-sin #(* (sin-osc %1) (max 0 (lf-noise1 12)) %2)
            sins  (map mk-sin freqs muls)]
        (* (mix sins) 0.3)))

(definst saw-wave [freq 440 attack 0.01 sustain 0.4 release 0.1 vol 0.4]
  (* (env-gen (env-lin attack sustain release) 2 3 3 4 FREE)
     (saw freq)
     vol))

(defn saw2 [music-note]
  (saw-wave (midi->hz (note music-note))))

(defn play-chord [a-chord]
  (doseq [note a-chord] (saw2 note)))

(defn chord-progression-beat [m beat-num]
  (at (m (+ 0 beat-num)) (play-chord (chord :C4 :major)))
  (at (m (+ 4 beat-num)) (play-chord (chord :G3 :major)))
  (at (m (+ 8 beat-num)) (play-chord (chord :A3 :minor)))
  (at (m (+ 12 beat-num)) (play-chord (chord :F3 :major)))
  (apply-at (m (+ 16 beat-num)) chord-progression-beat m (+ 16 beat-num) []))

(defonce metro (metronome 120))
(chord-progression-beat metro (metro))
(stop)

(def scale-degrees [:vi :vii :i+ :_ :vii :_ :i+ :vii :vi :_ :vii :_])
(def pitches (degrees->pitches scale-degrees :dorian :C4))

(defn play [time notes sep]
  (let [note (first notes)]
    (when note
      (at time (saw (midi->hz note))))
    (let [next-time (+ time sep)]
      (apply-at next-time play [next-time (rest notes) sep]))))

