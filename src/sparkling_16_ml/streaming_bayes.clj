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
   (scala Tuple2)
   (org.apache.spark.api.java JavaRDD)
   (sparkinterface VectorClojure)
   (org.apache.spark.mllib.linalg Vectors)
   (org.apache.spark.mllib.feature HashingTF)
   (org.apache.spark.mllib.classification NaiveBayes)
   (org.apache.spark.mllib.regression LabeledPoint)
   (org.apache.spark.streaming Duration)
   (org.apache.spark.api.java JavaSparkContext)
   (org.apache.spark.streaming.api.java JavaStreamingContext JavaDStream)
   (org.apache.spark.streaming.api.java JavaPairInputDStream)
   (kafka.serializer StringDecoder)
   (org.apache.spark.streaming.kafka KafkaUtils)
   (java.util Collections)
   (java.util HashMap)))

(def rdd-log (atom (list)))
(def sc-log (atom ""))
(def predict-log (atom ""))
(defn foreach-rdd [dstream f]
  (.foreachRDD dstream (function2 f)))

(defn untuple [^Tuple2 t]
  (persistent!
   (conj!
    (conj! (transient []) (._1 t))
    (._2 t))))

(defn -main
  [& args]
  (spark/with-context context
    (-> (conf/spark-conf)
        (conf/master "local[*]")
        (conf/app-name "Consumer"))
    (let [streaming-context (JavaStreamingContext. context (Duration. 1000))
          parameters (HashMap. {"metadata.broker.list" "127.0.0.1:9092"})
          topics (Collections/singleton "w4u_messages")
          stream (KafkaUtils/createDirectStream
                  streaming-context String String StringDecoder StringDecoder parameters topics)
          ;; 加载贝叶斯模型, 用于数据流的预测分类
          spam (spark/text-file context "files/spam.txt")
          ham (spark/text-file context "files/ham.txt")
          tf (HashingTF. 100)
          spam-features (spark/map (fn [x] (VectorClojure/tftransform tf x)) spam)
          ham-features (spark/map (fn [x] (VectorClojure/tftransform tf x)) ham)
          positive-examples (spark/map (fn [x] (LabeledPoint. 1 x)) spam-features)
          negative-examples (spark/map (fn [x] (LabeledPoint. 0 x)) ham-features)
          training-data (spark/union (.rdd positive-examples) (.rdd negative-examples))
          model (NaiveBayes/train training-data 1.0)
          predict (fn [x] (.predict model (VectorClojure/tftransform tf x)))]
      (do
        (foreach-rdd
         stream
         (fn [rdd arg2]
           (log/info (str "=====" rdd "=====" arg2 "====="
                          (reset! sc-log context)
                          (reset! predict-log predict)
                          "======="
                          ;; =[(null,啊啊啊啊)] or []
                          ;; [#sparkling/tuple [nil "哎哎哎"]]
                          (let [res (last (vec (.toArray (spark/collect rdd))))]
                            ;; class scala.Tuple2 ==>> (type res)
                            ;;(str "_____" (type res) "_____" (nil? res))
                            (if (nil? res) "_____" (predict (last (untuple res))))
                            )
                          ;;(swap! rdd-log conj rdd)
                          ))
           
           #_(spark/foreach
            (fn [x]
              (log/info (str "*****" x "*****"))) rdd)
           ))
        (.start streaming-context)
        (.awaitTermination streaming-context)))))

;;(spark/collect (nth @rdd-log 5))
