package sparkinterface;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.StreamingLinearRegressionWithSGD;

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
}
