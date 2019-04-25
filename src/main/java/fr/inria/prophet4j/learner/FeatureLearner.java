package fr.inria.prophet4j.learner;

import java.util.*;

import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.feature.S4R.S4RFeature;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature;
import fr.inria.prophet4j.feature.extended.ExtendedFeature;
import fr.inria.prophet4j.feature.original.OriginalFeature;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Support;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.ParameterVector;
import fr.inria.prophet4j.utility.Structure.Sample;

// based on learner.cpp (follow the way of ProphetPaper)
public class FeatureLearner {
    private Option option;

    private static final Logger logger = LogManager.getLogger(FeatureLearner.class.getName());

    public FeatureLearner(Option option) {
        this.option = option;
    }

//    private double getLogSumExp(double[] array) {
//        assert array.length > 0;
//        double max = Arrays.stream(array).max().getAsDouble();
//        double sum = 0;
//        for (double value : array) {
//            sum += Math.exp(value - max);
//        }
//        return max + Math.log(sum);
//    }

    private double[] newFeatureArray() {
        int arraySize = 0;
        switch (option.featureOption) {
            case ENHANCED:
                arraySize = EnhancedFeature.FEATURE_SIZE;
                break;
            case EXTENDED:
                arraySize = ExtendedFeature.FEATURE_SIZE;
                break;
            case ORIGINAL:
                arraySize = OriginalFeature.FEATURE_SIZE;
                break;
            case S4R:
                arraySize = S4RFeature.FEATURE_SIZE;
                break;
        }
        return new double[arraySize];
    }

    private ParameterVector learn(List<Sample> trainingData, List<Sample> validationData) {
        double eta = 1;
        double bestGamma = 1;
        final double lambda = 1e-3;
        ParameterVector theta = new ParameterVector(option.featureOption);
        ParameterVector bestTheta = new ParameterVector(option.featureOption);

        for (int count = 0; count < 100; count++) { // 200 seem unnecessary
            ParameterVector delta = new ParameterVector(option.featureOption);
            // handle training data
            for (Sample sample : trainingData) {
                List<FeatureVector> featureVectors = sample.getFeatureVectors();
                // compute scores
                double[] scores = new double[featureVectors.size()];
                for (int i = 0; i < featureVectors.size(); i++) {
                    FeatureVector featureVector = featureVectors.get(i);
                    scores[i] = featureVector.score(theta);
                }
                // compute expValues
                double[] expValues = new double[featureVectors.size()];
                double maxSuperscript = Arrays.stream(scores).max().orElse(0);
                for (int i = 0; i < featureVectors.size(); i++) {
                    expValues[i] = Math.exp(scores[i] - maxSuperscript);
                }
                double sumExpValues = Arrays.stream(expValues).sum();
                double[] tmpValues = newFeatureArray();
                for (int i = 0; i < featureVectors.size(); i++) {
                    FeatureVector featureVector = featureVectors.get(i);
                    List<FeatureCross> featureCrosses = featureVector.getFeatureCrosses();
                    for (FeatureCross featureCross : featureCrosses) {
                        int featureCrossId = featureCross.getId();
                        // maybe we need Rescaling (min-max normalization) todo consider
                        // https://en.wikipedia.org/wiki/Feature_scaling#Application
//                        tmpValues[featureCrossId] += expValues[i] * 1;
                        tmpValues[featureCrossId] += expValues[i] * featureCross.getDegree();
                    }
                }
                // compute delta
                for (int i = 0; i < tmpValues.length; i++) {
                    delta.dec(i, tmpValues[i] / sumExpValues);
                }
                long markedSize = featureVectors.stream().filter(FeatureVector::isMarked).count();
                for (FeatureVector featureVector : featureVectors) {
                    if (featureVector.isMarked()) {
                        List<FeatureCross> featureCrosses = featureVector.getFeatureCrosses();
                        for (FeatureCross featureCross : featureCrosses) {
                            // 1 was derived before but i forget the procedure 03/24 todo check
                            // but i believe it is okay as 1 is also used in the original project
//                            delta.inc(featureCross.getId(), 1.0 / markedSize);
                            delta.inc(featureCross.getId(), featureCross.getDegree() / markedSize);
                        }
                    }
                }
            }
            // compute delta
            for (int i = 0; i < delta.size(); i++) {
                delta.div(i, trainingData.size());
                // I feel L1 normalization term is not necessary todo consider
                delta.dec(i, lambda * (Math.signum(theta.get(i)) + 2 * theta.get(i)));
            }
            // update theta
            for (int i = 0; i < delta.size(); i++) {
                theta.inc(i, eta * delta.get(i));
            }
            // handle validation data
            double gamma = 0;
            for (Sample sample : validationData) {
                List<FeatureVector> featureVectors = sample.getFeatureVectors();
                double[] scores = new double[featureVectors.size()];
                for (int i = 0; i < featureVectors.size(); i++) {
                    // scores means values of phi dotProduct theta
                    scores[i] = featureVectors.get(i).score(theta);
                }
                int rank = 0;
                for (int i = 0; i < featureVectors.size(); i++) {
                    if (featureVectors.get(i).isMarked()) {
                        for (int j = 0; j < featureVectors.size(); j++) {
                            if (!featureVectors.get(j).isMarked()) {
                                if (scores[j] >= scores[i]) rank++;
                            }
                        }
                    }
                }
                gamma += ((double) rank) / featureVectors.size();
            }
            gamma /= validationData.size();
            // update results
            if (bestGamma > gamma) {
                count = 0;
                bestTheta.clone(theta);
                bestGamma = gamma;
                logger.log(Level.INFO, count + " Update BestGamma " + bestGamma);
            } else if (eta > 0.01) {
                eta *= 0.9;
//                logger.log(Level.INFO, count + " Drop eta to " + eta);
//            } else {
//                logger.log(Level.INFO, count + " Keep eta as " + eta);
            }
        }
        bestTheta.gamma = bestGamma;
        logger.log(Level.INFO, "BestGamma " + bestGamma);
        return bestTheta;
    }

    public void run(List<String> filePaths) {
        String parameterFilePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option) + "ParameterVector";
        // sort all sample data as we want one distinct baseline
        filePaths.sort(String::compareTo);
        logger.log(Level.INFO, "Size of SampleData: " + filePaths.size());

        // k-fold Cross Validation
        final int k = 5;
        assert filePaths.size() >= k;
        List<List<Sample>> folds = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            folds.add(new ArrayList<>());
        }
        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            Sample sample = new Sample(filePath);
            sample.loadFeatureVectors();
            folds.get(i % 5).add(sample);
        }
        double averageGamma = 0;
        double bestGamma = 1;
        ParameterVector bestParameterVector = null;
        for (int i = 0; i < k; i++) {
            List<Sample> trainingData = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                if (j != i) {
                    trainingData.addAll(folds.get(j));
                }
            }
            List<Sample> validationData = new ArrayList<>(folds.get(i));
            ParameterVector parameterVector = learn(trainingData, validationData);
            averageGamma += parameterVector.gamma;
            if (bestGamma > parameterVector.gamma) {
                bestGamma = parameterVector.gamma;
                bestParameterVector = parameterVector;
            }
        }
        averageGamma /= k;
        logger.log(Level.INFO, k + "-fold Cross Validation: " + averageGamma);
        if (bestParameterVector != null) {
            bestParameterVector.save(parameterFilePath);
            System.out.println("ParameterVector is saved to " + parameterFilePath);
        }
    }
}