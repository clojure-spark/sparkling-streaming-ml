package sparkinterface;
import java.util.Arrays;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.StreamingLinearRegressionWithSGD;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;

public class VectorClojure {
    
    public static Vector dense(String[] args) {
        double[] prompt = new double[args.length];
        for (int i =0; i<args.length; i++){
            prompt[i] = Double.parseDouble(args[i]);
        }
        Vector denseVec = Vectors.dense(prompt);
        return denseVec;
    }

    public static StreamingLinearRegressionWithSGD linearRegressionodel(double [] args, int num, float size) {
        StreamingLinearRegressionWithSGD model = new StreamingLinearRegressionWithSGD()
            .setStepSize(size)
            .setNumIterations(num)
            .setInitialWeights(Vectors.dense(args));
        return model;
    }

    public static LabeledPoint labeledPoint(double label, double [] args) {
        LabeledPoint point = new LabeledPoint(label, Vectors.dense(args));
        return point;
    }

    public static Vector tftransform(HashingTF tf, String data) {
        //final HashingTF tf = new HashingTF(100);
        Vector tfres = tf.transform(Arrays.asList(data.split(" ")));
        return tfres;
    }

    public static NaiveBayesModel naiveBayesTrain(JavaRDD trdd) {
        NaiveBayesModel model = NaiveBayes.train(trdd.rdd(), 1.0);
        return model;
    }
}
