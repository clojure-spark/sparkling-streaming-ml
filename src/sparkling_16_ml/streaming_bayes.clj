(ns sparkling-16-ml.streaming-bayes
  (:gen-class)
  (:require
   [serializable.fn :as sfn]
   [sparkling.conf :as conf]
   [sparkling.core :as spark]
   [sparkling.api :as api]
   [flambo.api :as fapi]
   [sparkling.function :refer [function2]]
   [sparkling.scalaInterop :as scala]
   [clojure.tools.logging :as log])
  (:import
   (org.apache.spark.api.java JavaRDD)
   (sparkinterface VectorClojure)
   (org.apache.spark.mllib.feature HashingTF)
   (org.apache.spark.mllib.linalg Vectors)
   (org.apache.spark.mllib.classification NaiveBayes)
   (org.apache.spark.mllib.regression LabeledPoint)
   (org.apache.spark.streaming Duration)
   (org.apache.spark.api.java JavaSparkContext)
   (org.apache.spark.streaming.api.java JavaStreamingContext JavaDStream)
   (org.apache.spark.streaming.api.java JavaPairInputDStream)
   (kafka.serializer StringDecoder)
   (org.apache.spark.streaming.kafka KafkaUtils)
   (java.util Arrays)
   (java.util Collections)
   (java.util HashMap)))

(defn foreach-rdd [dstream f]
  (.foreachRDD dstream (function2 f)))

(def c (-> (conf/spark-conf)
           (conf/master "local[*]")
           (conf/app-name "Consumer")))
(def context (JavaSparkContext. c))

(defn -main
  [& args]
  (let [;; c & context
        streaming-context (JavaStreamingContext. context (Duration. 1000))
        parameters (HashMap. {"metadata.broker.list" "127.0.0.1:9092"})
        topics (Collections/singleton "w4u_messages")
        stream (KafkaUtils/createDirectStream streaming-context String String StringDecoder StringDecoder parameters topics)]
    (do
      (foreach-rdd
       stream
       (fn [rdd arg2]
         (log/info (str "=====" rdd "=====" arg2))
         (fapi/foreach
          rdd (fapi/fn [x]
                (log/info (str "*********" x "*****" ))))))
      (.start streaming-context)
      (.awaitTermination streaming-context))))


(def spam (spark/text-file context "files/spam.txt"))
(def ham (spark/text-file context "files/ham.txt"))
(def tf (HashingTF. 100))

#_(def spam-features (spark/map (fn [x] (VectorClojure/tftransform tf x)) spam))
#_(def ham-features (spark/map (fn [x] (VectorClojure/tftransform tf x)) ham))

(def positive-examples
  (fapi/map spam
            (fn [x]
              (VectorClojure/labeledPoint 1 (VectorClojure/tftransform tf x)))))

(def negative-examples
  (spark/map (fn [x]  (VectorClojure/labeledPoint 0 x)) ham-features))

(def training-data (spark/union positive-examples negative-examples))

;;(def model (NaiveBayes/train (.rdd training-data) 1.0))


  
