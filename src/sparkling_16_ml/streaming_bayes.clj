(ns sparkling-16-ml.streaming-bayes
  (:gen-class)
  (:require
   [serializable.fn :as sfn]
   [sparkling.conf :as conf]
   [sparkling.core :as spark]
   [sparkling.api :as api]
   [sparkling.function :refer [function2]]
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

(defn foreach-rdd [dstream f]
  (.foreachRDD dstream (function2 f)))

(defn -main
  [& args]
  (spark/with-context context
    (-> (conf/spark-conf)
        (conf/master "local[*]")
        (conf/app-name "Consumer"))
    (let [streaming-context (JavaStreamingContext. context (Duration. 1000))
          parameters (HashMap. {"metadata.broker.list" "127.0.0.1:9092"})
          topics (Collections/singleton "w4u_messages")
          stream (KafkaUtils/createDirectStream streaming-context String String StringDecoder StringDecoder parameters topics)]
      (do
        (foreach-rdd
         stream
         (fn [rdd arg2]
           (log/info (str "=====" rdd "=====" arg2))
           (spark/foreach
            (fn [x]
              (log/info (str "*********" x "*****" ))) rdd)))
        (.start streaming-context)
        (.awaitTermination streaming-context)))))
