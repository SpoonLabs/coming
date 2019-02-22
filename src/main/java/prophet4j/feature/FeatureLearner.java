package prophet4j.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prophet4j.defined.FeatureStruct;
import prophet4j.defined.FeatureStruct.Feature;
import prophet4j.defined.FeatureStruct.FeatureVector;
import prophet4j.defined.FeatureStruct.ParameterVector;
import prophet4j.defined.FeatureStruct.Sample;
import prophet4j.defined.FeatureType;

// based on learner.cpp (follow the way of ProphetPaper)
public class FeatureLearner {
    // create wholeSet to avoid using extra deep copy operation
    private List<Sample> sampleSet = new ArrayList<>();
    private List<Sample> trainingSet = new ArrayList<>();
    private List<Sample> validationSet = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger(FeatureLearner.class.getName());

    // Maximum Entropy Model
    private ParameterVector modelMaximumEntropy() {
        double alpha = 1;
        double gammaBest = 1;
        final double L1 = 1e-3, L2 = 1e-3;
        ParameterVector theta = new ParameterVector();
        ParameterVector thetaBest = new ParameterVector();

        int count = 0;
        while (count < 200) {
            ParameterVector delta = new ParameterVector();
            // training set
            for (Sample sample: trainingSet) {
                double[] tmp = new double[sample.featureVectors.size()];
                for (int i = 0; i < sample.featureVectors.size(); i++) {
                    tmp[i] = Math.exp(theta.dotProduct(sample.featureVectors.get(i)));
                }
                double sumExp = Arrays.stream(tmp).sum();
                for (int i = 0; i < sample.featureVectors.size(); i++) {
                    tmp[i] /= sumExp;
                }
                for (int i = 0; i < sample.featureVectors.size(); i++) {
                    FeatureVector featureVector = sample.featureVectors.get(i);
                    for (Feature feature: featureVector.getFeatures()) {
                        int featureId = feature.getFeatureId();
                        delta.set(featureId, delta.get(featureId) + tmp[i]);
                    }
                }
            }
            // compute delta
            for (int i = 0; i < delta.size(); i++) {
                delta.set(i, delta.get(i) / trainingSet.size() - L1 * Math.signum(theta.get(i)) - L2 * 2 * theta.get(i));
            }
            // update theta
            for (int i = 0; i < delta.size(); i++) {
                theta.set(i, theta.get(i) + alpha * delta.get(i));
            }
            // validation set
            double gamma = 0;
            for (Sample sample: validationSet) {
                // here tmp means values of phi dotProduct theta
                double[] tmp = new double[sample.featureVectors.size()];
                for (int i = 0; i < sample.featureVectors.size(); i++)
                    tmp[i] = theta.dotProduct(sample.featureVectors.get(i));
                int rank = 0;
                // fixme: to make sure the first one corresponds to the human-patch
                for (int i = 1; i < sample.featureVectors.size(); i++) {
                    if (tmp[i] >= tmp[0])
                        rank++;
                }
                gamma += ((double) rank) / sample.featureVectors.size() / validationSet.size();
            }
            // update results
            count += 1;
            if (gammaBest > gamma) {
                thetaBest.clone(theta);
                gammaBest = gamma;
                count = 0;
                logger.log(Level.INFO, "Update best!");
            } else if (alpha > 0.01) {
                alpha *= 0.9;
                logger.log(Level.INFO, "Drop alpha to " + alpha);
            }
        }
        logger.log(Level.INFO, "BestGamma " + gammaBest);
        return thetaBest;
    }

    public void func4Demo(List<String> filePaths) {
        try {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                logger.log(Level.INFO, "Processing file " + filePath);
                // check this initialization
                Sample sample = new Sample();
                sample.featureVectors = FeatureStruct.load(file);
                sampleSet.add(sample);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.log(Level.INFO, "Size of Features: " + FeatureType.FEATURE_SIZE);
        logger.log(Level.INFO, "Size of Sample-Set: " + sampleSet.size());

        int sizeValidationSet = (int) (sampleSet.size() * 0.15);
        assert (sizeValidationSet < sampleSet.size());

        trainingSet.clear();
        validationSet.clear();
        // split validation
        int k = sampleSet.size() / sizeValidationSet;
        for (int i = 0; i < sampleSet.size(); i++)
            if (i % k == 0)
                validationSet.add(sampleSet.get(i));
            else
                trainingSet.add(sampleSet.get(i));

        logger.log(Level.INFO, "Size of Training-Set: " + trainingSet.size());
        logger.log(Level.INFO, "Size of Validation-Set: " + validationSet.size());

        modelMaximumEntropy();
    }
}
