(ns dead-reckoner.core
  (:require [clojure.string :as str]))

(println "Program started.")

(defn rotate-compass [rotation]
  (let [compass-img (.getElementById js/document "compass-img")]
    (set! (.-transform (.-style compass-img)) (str "rotate(" rotation "deg)"))))

(defn set-bearing [delta]
  (let [bearing (.getElementById js/document "bearing")
        new-value (mod (+ (js/parseInt (.-value bearing)) delta) 360)
        bearing-text (.getElementById js/document "bearing-text")]
    (set! (.-value bearing) new-value)
    (set! (.-innerHTML bearing-text) (str new-value "\u00B0"))
    (rotate-compass new-value)
    ))

(defn set-paces [delta]
  (let [paces (.getElementById js/document "paces")
        new-value (max (+ (js/parseInt (.-value paces)) delta) 0)
        paces-text (.getElementById js/document "paces-text")]
    (set! (.-value paces) new-value)
    (set! (.-innerHTML paces-text) (str new-value " paces"))))

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

(defn deltas-to-waypoints [xs ys]
  "Takes in vectors between waypoints and returns waypoints as absolute coordinates."
  (for [i (range (inc (count xs)))]
    {:x (reduce + (take i xs))
     :y (reduce + (take i ys))}))

(defn transform-waypoints [waypoints screen-center map-center zoom]
  "Transform waypoints to appear correctly on canvas."
  (for [waypoint waypoints]
    {:x (- (:x screen-center) (* zoom (- (:x waypoint) (:x map-center))))
     :y (- (:y screen-center) (* zoom (- (:y waypoint) (:y map-center))))}))

(defn draw-path [ctx waypoints]
  "Given scaled waypoints and 2d context, draw path."
  (if (> (count waypoints) 1)
    (do
      (.beginPath ctx)
      (.moveTo ctx (:x (first waypoints)) (:y (first waypoints)))
      (.lineTo ctx (:x (nth waypoints 1)) (:y (nth waypoints 1)))
      (.stroke ctx)
      (recur ctx (rest waypoints)))))

;   set bg, draw-path

(defn update-map [xs ys]
  "Update map of the route."
  (let [map-canvas (.getElementById js/document "map")
        zoom-slider (.getElementById js/document "zoom")
        zoom (Math/pow 1.1 (.-value zoom-slider))
        w (.-width map-canvas)
        h (.-height map-canvas)
        screen-center {:x (/ w 2) :y (/ h 2)}
        ctx (.getContext map-canvas "2d")
        waypoints (deltas-to-waypoints xs ys)
        map-center {:x 0 :y 0}
        transformed (transform-waypoints waypoints screen-center map-center zoom)]
    (do
      (.clearRect ctx 0 0 w h)
      (draw-path ctx transformed))))

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

(defn get-orientation [event]
  (let [alpha (.-alpha event)
        bearing (.getElementById js/document "bearing")
        bearing-text (.getElementById js/document "bearing-text")]
    (if (not= nil alpha)
      (do
        (rotate-compass alpha)
        (set! (.-value bearing) alpha)
        (set! (.-innerHTML bearing-text) (str alpha "\u00B0"))))))

(set! (.-onclick (.getElementById js/document "bearing-dec")) #(set-bearing -1))
(set! (.-oninput (.getElementById js/document "bearing")) #(set-bearing 0))
(set! (.-onclick (.getElementById js/document "bearing-inc")) #(set-bearing 1))

(set! (.-onclick (.getElementById js/document "paces-dec-10")) #(set-paces -10))
(set! (.-onclick (.getElementById js/document "paces-dec-1")) #(set-paces -1))
(set! (.-oninput (.getElementById js/document "paces")) #(set-paces 0))
(set! (.-onclick (.getElementById js/document "paces-inc-1")) #(set-paces 1))
(set! (.-onclick (.getElementById js/document "paces-inc-10")) #(set-paces 10))

(set! (.-onclick (.getElementById js/document "add-segment")) add-segment)

(set! (.-oninput (.getElementById js/document "zoom")) #(update-navigation (read-segments)))

(set! (.-onclick (.getElementById js/document "load-segment-file")) load-segment-file)
(set! (.-onclick (.getElementById js/document "save-segment-file")) save-segment-file)

(set! (.-oninput (.getElementById js/document "segment-list")) #(update-navigation (read-segments)))

(.addEventListener js/window "deviceorientation" get-orientation)
