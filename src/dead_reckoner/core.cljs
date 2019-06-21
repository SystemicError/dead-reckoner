(ns dead-reckoner.core)

(println "Program started.")

(defn rotate-compass [rotation]
  (let [compass-img (.getElementById js/document "compass-img")]
    (set! (.-transform (.-style compass-img)) (str "rotate(" rotation "deg)"))))

(defn set-bearing [delta]
  (let [bearing (.getElementById js/document "bearing")
        new-value (mod (+ (js/parseInt (.-value bearing)) delta) 360)]
    (set! (.-value bearing) new-value)
    (rotate-compass new-value)
    ))

(defn set-paces [delta]
  (let [paces (.getElementById js/document "paces")
        new-value (max (+ (js/parseInt (.-value paces)) delta) 0)]
    (set! (.-value paces) new-value)))

(defn read-segments []
  "Returns a list of all segments"
  (let [segment-list (.-children (.getElementById js/document "segment-list"))]
    (for [i (range 1 (.-length segment-list))]
      (let [segment (.item segment-list i)
            children (.-children segment)
            bearing (js/parseInt (.-innerHTML (.item children 0)))
            paces (js/parseInt (.-innerHTML (.item children 1)))]
        {:bearing bearing
         :paces paces}))))

(defn update-map
  "Given lists of xs and ys, draw a map of the route."
  ([xs ys] (update-map xs ys 0 0))
  ([xs ys x y]
    (let [map-canvas (.getElementById js/document "map")
          ctx (.getContext map-canvas "2d")]
      (if (not (empty? xs))
        (do
          (.moveTo ctx x y)
          (.lineTo ctx (+ x (first xs)) (+ y (first ys)))
          (.stroke ctx)
          (recur (rest xs) (rest ys) (+ x (first xs)) (+ y (first ys))))))))

(defn update-navigation [segments]
  "Update the navigation readouts based on segment data."
  (let [total-paces (reduce + (map :paces segments))
        ys (map #(* (Math/cos (/ (* Math/PI (:bearing %)) 180.0)) (:paces %)) segments)
        y (reduce + ys)
        xs (map #(* (Math/sin (/ (* Math/PI (:bearing %)) 180.0)) (:paces %)) segments)
        x (reduce + xs)
        distance-to-start (Math/sqrt (+ (* x x) (* y y)))
        bearing-to-start (mod (+ 180.0 (/ (* 180.0 (Math/atan2 x y)) Math/PI)) 360)]
    (set! (.-innerHTML (.getElementById js/document "total-paces")) (str "Total paces:  " total-paces))
    (set! (.-innerHTML (.getElementById js/document "distance-to-start")) (str "Straight-line distance to start:  " distance-to-start))
    (set! (.-innerHTML (.getElementById js/document "bearing-to-start")) (str "Bearing to starting position:  " bearing-to-start))
    (update-map xs ys)
  ))

(defn add-segment []
  (let [segment-list (.getElementById js/document "segment-list")
        segment (.createElement js/document "tr")
        bearing (.-value (.getElementById js/document "bearing"))
        paces (.-value (.getElementById js/document "paces"))
        comment-string (.-value (.getElementById js/document "comment"))
        td-bearing (.createElement js/document "td")
        td-paces (.createElement js/document "td")
        td-comment (.createElement js/document "td")]
    (set! (.-innerHTML td-bearing) bearing)
    (set! (.-innerHTML td-paces) paces)
    (set! (.-innerHTML td-comment) comment-string)
    (.appendChild segment td-bearing)
    (.appendChild segment td-paces)
    (.appendChild segment  td-comment)
    (.appendChild segment-list segment)
    (set! (.-value (.getElementById js/document "comment")) "")
    (update-navigation (read-segments))))

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

(set! (.-onclick (.getElementById js/document "add-segment")) add-segment)
