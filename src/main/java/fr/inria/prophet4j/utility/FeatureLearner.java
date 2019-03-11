package fr.inria.prophet4j.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.ParameterVector;
import fr.inria.prophet4j.defined.Structure.Sample;

// based on learner.cpp (follow the way of ProphetPaper)
public class FeatureLearner {
    private boolean doShuffle;
    private FeatureOption featureOption;
    // create wholeSet to avoid using extra deep copy operation
    private List<Sample> sampleSet = new ArrayList<>();
    private List<Sample> trainingSet = new ArrayList<>();
    private List<Sample> validationSet = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger(FeatureLearner.class.getName());

    public FeatureLearner (boolean doShuffle, FeatureOption featureOption) {
        this.doShuffle = doShuffle;
        this.featureOption = featureOption;
    }

    // Maximum Entropy Model
    private ParameterVector modelMaximumEntropy() {
        double alpha = 1;
        double gammaBest = 1;
        final double L1 = 1e-3, L2 = 1e-3;
        ParameterVector theta = new ParameterVector(featureOption);
        ParameterVector thetaBest = new ParameterVector(featureOption);

        int count = 0;
        while (count < 200) {
            ParameterVector delta = new ParameterVector(featureOption);
            // training set
            for (Sample sample: trainingSet) {
                List<FeatureVector> featureVectors = sample.getFeatureVectors();
                double[] tmp = new double[featureVectors.size()];
                for (int i = 0; i < featureVectors.size(); i++) {
                    tmp[i] = Math.exp(theta.dotProduct(featureVectors.get(i)));
                }
                double sumExp = Arrays.stream(tmp).sum();
                for (int i = 0; i < featureVectors.size(); i++) {
                    tmp[i] /= sumExp;
                }
                for (int i = 0; i < featureVectors.size(); i++) {
                    FeatureVector featureVector = featureVectors.get(i);
                    for (int featureCrossId: featureVector.getFeatureCrossIds()) {
                        delta.set(featureCrossId, delta.get(featureCrossId) + tmp[i]);
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
                List<FeatureVector> featureVectors = sample.getFeatureVectors();
                // here tmp means values of phi dotProduct theta
                double[] tmp = new double[featureVectors.size()];
                for (int i = 0; i < featureVectors.size(); i++)
                    tmp[i] = theta.dotProduct(featureVectors.get(i));
                int rank = 0;
                // the first one corresponds to the human-patch
                for (int i = 1; i < featureVectors.size(); i++) {
//                    if (tmp[i] >= tmp[0]) rank++;
                    if (tmp[i] > tmp[0]) rank++;
                }
                gamma += ((double) rank) / featureVectors.size() / validationSet.size();
            }
            // update results
            count += 1;
            if (gammaBest > gamma) {
                thetaBest.clone(theta);
                gammaBest = gamma;
                count = 0;
                logger.log(Level.INFO, count + " Update best gamma " + gammaBest);
            } else if (alpha > 0.01) {
                alpha *= 0.9;
                logger.log(Level.INFO, count + " Drop alpha to " + alpha);
            } else {
                logger.log(Level.INFO, count + " Keep alpha as " + alpha);
            }
        }
        logger.log(Level.INFO, "BestGamma " + gammaBest);
        return thetaBest;
    }

    public void func4Demo(List<String> filePaths, String vectorFilePath) {
        for (String filePath : filePaths) {
            logger.log(Level.INFO, "Processing file " + filePath);
            sampleSet.add(new Sample(filePath, featureOption));
        }
        if (doShuffle) {
            Collections.shuffle(sampleSet);
        } // else get result of default case

        logger.log(Level.INFO, "Size of Sample-Set: " + sampleSet.size());

        int sizeValidationSet = (int) (sampleSet.size() * 0.15);
        assert (sizeValidationSet < sampleSet.size());

        trainingSet.clear();
        validationSet.clear();
        int k = sampleSet.size() / sizeValidationSet;
        for (int i = 0; i < sampleSet.size(); i++)
            if (i % k == 0)
                validationSet.add(sampleSet.get(i));
            else
                trainingSet.add(sampleSet.get(i));

        logger.log(Level.INFO, "Size of Training-Set: " + trainingSet.size());
        logger.log(Level.INFO, "Size of Validation-Set: " + validationSet.size());

        modelMaximumEntropy().write(new File(vectorFilePath));
    }
}
