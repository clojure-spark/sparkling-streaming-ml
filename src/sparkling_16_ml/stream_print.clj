(ns sparkling-16-ml.stream-print
  (:gen-class)
  (:require
   [sparkling.conf :as conf]
   [sparkling.core :as spark]
   [sparkling.scalaInterop :as scala])
  (:import
   (org.apache.spark.api.java JavaRDD)
   (sparkinterface VectorClojure)
   (org.apache.spark.mllib.linalg Vectors)
   (org.apache.spark.mllib.regression StreamingLinearRegressionWithSGD LabeledPoint)
   (org.apache.spark.streaming Duration)
   (org.apache.spark.streaming.api.java JavaStreamingContext JavaDStream)))

(defn duration [ms] (Duration. ms))

(def ssc (JavaStreamingContext. "local[*]" "First Streaming App" (duration 10000)))

(def model (VectorClojure/linearRegressionodel (double-array (repeat 100 0.0)) 1 0.01))

(def stream (.socketTextStream ssc "localhost" 9999))

(defn -main
  [& args]
  (do
    (.print stream)
    (.start ssc)
    (.awaitTermination ssc)))
