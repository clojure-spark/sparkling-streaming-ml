(ns sparkling-16-ml.streaming-bayes
  (:gen-class)
  (:require
   [serializable.fn :as sfn]
   [sparkling.conf :as conf]
   [sparkling.core :as spark]
   [sparkling.api :as api]
   ;;[sparkling.function :refer [function2 function]]
   [sparkling.function :refer [flat-map-function
                               flat-map-function2
                               function
                               function2
                               function3
                               pair-function
                               pair-flat-map-function
                               void-function]]
   ;;
   [sparkling.scalaInterop :as scala]
   [clojure.tools.logging :as log])
  (:import
   (org.apache.spark.api.java JavaRDD)
   (sparkinterface VectorClojure)
   (org.apache.spark.mllib.linalg Vectors)
   (org.apache.spark.streaming Duration)
   (org.apache.spark.api.java JavaSparkContext)
   (org.apache.spark.streaming.api.java JavaStreamingContext JavaDStream)
   (org.apache.spark.streaming.api.java JavaPairInputDStream)
   (kafka.serializer StringDecoder)
   (org.apache.spark.streaming.kafka KafkaUtils)
   (java.util Collections)
   (java.util HashMap)))

(defmacro szfn
  [& body]
  `(sfn/fn ~@body))

(defn foreach-rdd [dstream f]
  (.foreachRDD dstream (function2 f)))

(def c (-> (conf/spark-conf)
           (conf/master "local[*]")
           (conf/app-name "Consumer")))
(def context (JavaSparkContext. c))
(def streaming-context (JavaStreamingContext. context (Duration. 1000)))
(def parameters (HashMap. {"metadata.broker.list" "127.0.0.1:9092"}))
(def topics (Collections/singleton "w4u_messages"))
(def stream (KafkaUtils/createDirectStream streaming-context String String StringDecoder StringDecoder parameters topics))

(defn foreach
  "Applies the function `f` to all elements of `rdd`."
  [rdd f]
  (.foreach rdd (void-function f)))

(foreach-rdd
 stream
 (fn [rdd arg2]
   (log/info (str "=====" rdd "=====" arg2))
   ;;;;;;;;
   (foreach
    rdd
    (fn [x]
      (log/info (str "*********" x "*****" ))))
   ;;;;;;;
   ))

(.start streaming-context)
(.awaitTermination streaming-context)
