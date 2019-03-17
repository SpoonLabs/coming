package fr.inria.prophet4j.defined;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.ParameterVector;
import fr.inria.prophet4j.defined.Structure.Sample;

// consider improve the algorithm performance (time & space)
// based on learner.cpp (follow the way of ProphetPaper)
public class FeatureLearner {
    private FeatureOption featureOption;
    private List<Sample> sampleData;

    private static final Logger logger = LogManager.getLogger(FeatureLearner.class.getName());

    public FeatureLearner(FeatureOption featureOption) {
        this.featureOption = featureOption;
        this.sampleData = new ArrayList<>();
    }

    // Maximum Entropy Model
    private ParameterVector modelMaximumEntropy(List<Sample> trainingData, List<Sample> validationData) {
        double alpha = 1.0;
        double gammaBest = 1.0;
        final double L1 = 1e-3, L2 = 1e-3;
        ParameterVector theta = new ParameterVector(featureOption);
        ParameterVector thetaBest = new ParameterVector(featureOption);

        int count = 0;
        while (count < 200) {
            ParameterVector delta = new ParameterVector(featureOption);
            // handle training data
            for (Sample sample : trainingData) {
                List<FeatureVector> featureVectors = sample.loadFeatureVectors();
                double[] tmp = new double[featureVectors.size()];
                for (int i = 0; i < featureVectors.size(); i++) {
                    tmp[i] = Math.exp(featureVectors.get(i).score(theta));
                }
                double sumExp = Arrays.stream(tmp).sum();
                // we need to make sure no infinite or NaN exists
                // but assert seems ignored by program, why?
                assert !Double.isInfinite(sumExp);
                for (int i = 0; i < featureVectors.size(); i++) {
                    tmp[i] /= sumExp;
                }
                for (int i = 0; i < featureVectors.size(); i++) {
                    FeatureVector featureVector = featureVectors.get(i);
                    for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
                        int featureCrossId = featureCross.getId();
                        delta.inc(featureCrossId, tmp[i]);
                    }
                }
            }
            // compute delta
            for (int i = 0; i < delta.size(); i++) {
                delta.div(i, trainingData.size());
                delta.dec(i, L1 * Math.signum(theta.get(i)) + L2 * 2 * theta.get(i));
            }
            // update theta
            for (int i = 0; i < delta.size(); i++) {
                theta.inc(i, alpha * delta.get(i));
            }
            // handle validation data
            double gamma = 0.0;
            for (Sample sample : validationData) {
                List<FeatureVector> featureVectors = sample.loadFeatureVectors();
                double[] scores = new double[featureVectors.size()];
                for (int i = 0; i < featureVectors.size(); i++) {
                    // scores means values of phi dotProduct theta
                    scores[i] = featureVectors.get(i).score(theta);
                }
                int rank = 0;
                // the first one corresponds to the human-patch
                for (int i = 1; i < featureVectors.size(); i++) {
//                    if (scores[i] >= scores[0]) rank++;
                    if (scores[i] > scores[0]) rank++;
                }
                gamma += ((double) rank) / featureVectors.size();
            }
            gamma /= validationData.size();
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
        thetaBest.gamma = gammaBest;
        return thetaBest;
    }

    public void func4Demo(List<String> filePaths, String vectorFilePath) {
        // sort all sample data as we want one distinct baseline
        filePaths.sort(String::compareTo);
        for (String filePath : filePaths) {
            logger.log(Level.INFO, "Processing file " + filePath);
            sampleData.add(new Sample(filePath, featureOption));
        }
        logger.log(Level.INFO, "Size of SampleData: " + sampleData.size());

        // k-fold Cross Validation
        final int k = 5;
        assert sampleData.size() >= k;
        List<List<Sample>> folds = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            folds.add(new ArrayList<>());
        }
        for (int i = 0; i < sampleData.size(); i++) {
            folds.get(i % 5).add(sampleData.get(i));
        }
        double gammaAverage = 0.0;
        for (int i = 0; i < k; i++) {
            List<Sample> trainingData = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                if (j != i) {
                    trainingData.addAll(folds.get(j));
                }
            }
            List<Sample> validationData = new ArrayList<>(folds.get(i));
            ParameterVector parameterVector = modelMaximumEntropy(trainingData, validationData);
            gammaAverage += parameterVector.gamma;
//            parameterVector.save(vectorFilePath);
        }
        gammaAverage /= k;
        logger.log(Level.INFO, k + "-fold Cross Validation: " + gammaAverage);
    }
}
