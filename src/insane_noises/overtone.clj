(ns insane-noises.overtone
	(:use [overtone.live]))

(demo (sin-osc))

(demo ()'overtone-sketchbook.synths/pluck-saw)

(definst foo [] (saw 220))
(foo)
(stop)

(definst bar [freq 220] (saw freq))
(bar 110)

(definst baz [freq 440] (* 0.3 (saw freq)))
(baz 1100)

(foo)
(bar)
(baz)
(stop)

(definst quux [freq 440] (* 0.3 (saw freq)))
(quux)

(definst trem [freq 440 depth 10 rate 6 length 3]
   (* 0.3
      (line:kr 0 1 length FREE)
      (saw (+ freq (* depth (sin-osc:kr rate))))))
(trem)

(demo 7 (lpf (mix (saw [50 (line 100 1600 5) 101 100.5]))
              (lin-lin (lf-tri (line 2 20 5)) -1 1 400 4000)))
