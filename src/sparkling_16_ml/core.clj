(ns sparkling-16-ml.core
  (:gen-class)
  (:require
   [sparkling.conf :as conf]
   [sparkling.core :as spark]
   [sparkling.function :refer [function2]]
   [sparkling.scalaInterop :as scala]
   [clojure.tools.logging :as log])
  (:import
   (org.apache.spark.api.java JavaRDD)
   (sparkinterface VectorClojure)
   (org.apache.spark.mllib.linalg Vectors)
   (org.apache.spark.mllib.regression StreamingLinearRegressionWithSGD LabeledPoint)
   (org.apache.spark.streaming Duration)
   (org.apache.spark.streaming.api.java JavaStreamingContext JavaDStream)))

(defn duration [ms] (Duration. ms))
(defn foreach-rdd [dstream f]
  (.foreachRDD dstream (function2 f)))

(def ssc (JavaStreamingContext. "local[*]" "First Streaming App" (duration 10000)))
(def model (VectorClojure/linearRegressionodel (double-array (repeat 100 0.0)) 1 0.01))
(def stream (.socketTextStream ssc "localhost" 9999))

(def labeled-stream
  (spark/map
   (fn [record]
     (let [split (clojure.string/split record #"\t")
           y (Double/parseDouble (nth split 0))
           features (-> (nth split 1) (clojure.string/split #",") ((fn [fs] (map #(Double/parseDouble %) fs))) double-array)]
       (VectorClojure/labeledPoint y features))) stream))

(defn -main
  [& args]
  (do
    (.trainOn model labeled-stream)
    (.print
     (.predictOnValues
      model
      (spark/map-to-pair
       (fn [lp]
         (spark/tuple (.label lp) (.features lp)))
       labeled-stream)))
    (.start ssc)
    (.awaitTermination ssc)))
