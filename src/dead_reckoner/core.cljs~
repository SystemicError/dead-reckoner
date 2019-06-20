(ns dead-reckoner.core)

(println "Program started.")

(defn set-bearing [delta]
  (let [bearing (.getElementById js/document "bearing")
        new-value (mod (+ (js/parseInt (.-value bearing)) delta) 360)]
    (set! (.-value bearing) new-value)))

(defn set-paces [delta]
  (let [paces (.getElementById js/document "paces")
        new-value (max (+ (js/parseInt (.-value paces)) delta) 0)]
    (set! (.-value paces) new-value)))


(set! (.-onclick (.getElementById js/document "bearing-dec-30")) #(set-bearing -30))
(set! (.-onclick (.getElementById js/document "bearing-dec-5")) #(set-bearing -5))
(set! (.-onclick (.getElementById js/document "bearing-inc-5")) #(set-bearing 5))
(set! (.-onclick (.getElementById js/document "bearing-inc-30")) #(set-bearing 30))

(set! (.-onclick (.getElementById js/document "paces-dec-100")) #(set-paces -100))
(set! (.-onclick (.getElementById js/document "paces-dec-10")) #(set-paces -10))
(set! (.-onclick (.getElementById js/document "paces-dec-1")) #(set-paces -1))
(set! (.-onclick (.getElementById js/document "paces-inc-1")) #(set-paces 1))
(set! (.-onclick (.getElementById js/document "paces-inc-10")) #(set-paces 10))
(set! (.-onclick (.getElementById js/document "paces-inc-100")) #(set-paces 100))
