(ns sparkling-16-ml.matrix
  (:gen-class)
  (:require
   [sparkling.conf :as conf]
   [sparkling.core :as spark])
  (:import
   (org.apache.spark.api.java JavaRDD)
   (org.apache.spark.mllib.linalg Matrices Matrix Vector Vectors)
   (org.apache.spark.mllib.regression LabeledPoint)
   (org.apache.spark.mllib.stat Statistics)
   (org.apache.spark.mllib.stat.test ChiSqTestResult)
   (org.apache.spark.mllib.linalg.distributed IndexedRow IndexedRowMatrix RowMatrix)
   (java.util Arrays)))

(def vec (Vectors/dense (double-array (list 0.1 0.15 0.2 0.3 0.25))))

(def mat (Matrices/dense 3 2 (double-array (list 1.0 3.0 5.0 2.0 4.0 6.0))))

(def sm (Matrices/sparse 3 2 (int-array (list 0 1 3)) (int-array (list 0 2 1)) (double-array (list 9 6 8))))

(def sv (Vectors/sparse 3 (int-array (list 0 2)) (double-array (list 1.0 3.0))))

(def pos (LabeledPoint. 1.0 (Vectors/dense (double-array (list 1.0 0.0 3.0)))))

(def mat-t (.transpose mat))

(def mat-multiply (.multiply mat mat-t))
;; => #object[org.apache.spark.mllib.linalg.DenseMatrix 0x63608adf "5.0   11.0  17.0  \n11.0  25.0  39.0  \n17.0  39.0  61.0  "]

;; Distributed matrix
(spark/with-context sc
  (-> (conf/spark-conf)
      (conf/master "local[*]")
      (conf/app-name "Consumer"))
  (let [data (Arrays/asList (into-array (list 1 2 3 4 5 6)))
        rdd-dist-data (.parallelize sc data)
        mat (RowMatrix. (.rdd rdd-dist-data))
        mat2 (IndexedRowMatrix. (.rdd rdd-dist-data))
        row-mat (.toRowMatrix mat2)]
    ;;(list (.numRows mat) (.numCols mat))
    (.numRows mat) ;;=> 6
    ))
