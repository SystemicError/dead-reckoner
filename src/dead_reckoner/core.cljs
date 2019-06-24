(ns dead-reckoner.core
  (:require [clojure.string :as str]))

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
  (let [segment-list (.-value (.getElementById js/document "segment-list"))
        lines (str/split-lines segment-list)]
    (for [line lines]
      (let [fields (str/split (str/trim line) #"\t")
            bearing (js/parseInt (nth fields 0))
            paces (js/parseInt (nth fields 1))
            comment-str (if (< 2 (count fields))
                          (nth fields 2)
                          "")]
        {:bearing bearing
         :paces paces
         :comment comment-str}))))

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
  (let [segment-list (.-value (.getElementById js/document "segment-list"))
        bearing (.-value (.getElementById js/document "bearing"))
        paces (.-value (.getElementById js/document "paces"))
        comment-string (.-value (.getElementById js/document "comment"))
        entry (str bearing "\t" paces "\t" comment-string)
        new-segment-list (if (empty? segment-list)
                           entry
                           (str segment-list "\n" entry))]
    (set! (.-value (.getElementById js/document "segment-list")) new-segment-list)
    (update-navigation (read-segments))))

(defn load-segment-file []
  "Loads a file specified by client."
  (js/alert "Sorry, loading saves not implemented yet.")
  )

(defn save-segment-file []
  "Saves segment list to client's computer."
  (let [segments (read-segments)]
    (js/alert "Sorry, saves not implemented yet.")))

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

(set! (.-onclick (.getElementById js/document "load-segment-file")) load-segment-file)
(set! (.-onclick (.getElementById js/document "save-segment-file")) save-segment-file)

(set! (.-oninput (.getElementById js/document "segment-list")) #(update-navigation (read-segments)))
;(set! (.-oninput (.getElementById js/document "segment-list")) #(js/alert "Edited!"))
