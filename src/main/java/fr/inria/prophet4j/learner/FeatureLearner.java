package fr.inria.prophet4j.learner;

import java.util.*;

import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.feature.S4R.S4RFeature;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature;
import fr.inria.prophet4j.feature.original.OriginalFeature;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Support;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
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
                throw new RuntimeException("class removed by Martin for cleaning");
            case ORIGINAL:
                arraySize = OriginalFeature.FEATURE_SIZE;
                break;
            case S4R:
                arraySize = S4RFeature.FEATURE_SIZE;
                break;
            case S4RO:
                throw new RuntimeException("removed see https://github.com/SpoonLabs/coming/issues/235");
        }
        return new double[arraySize];
    }

    private ParameterVector learn(List<Sample> trainingData, List<Sample> validationData) {
        double eta = 1;
        double bestGamma = 1;
        final double lambda = 1e-3;
        ParameterVector theta = new ParameterVector(option.featureOption);
        ParameterVector bestTheta = new ParameterVector(option.featureOption);

        for (int epoch = 0; epoch < 100; epoch++) { // 200 seem unnecessary
            ParameterVector delta = new ParameterVector(option.featureOption);
            // handle training data
            for (Sample sample : trainingData) {
                List<FeatureMatrix> featureMatrices = sample.getFeatureMatrices();
                // compute scores
                Map<FeatureVector, Double> scores = new HashMap<>();
                for (FeatureMatrix featureMatrix : featureMatrices) {
                    for (FeatureVector featureVector : featureMatrix.getFeatureVectors()) {
                        scores.put(featureVector, featureVector.score(theta));
                    }
                }
                // compute expValues
                Map<FeatureVector, Double> expValues = new HashMap<>();
                double maxSuperscript = scores.values().stream().max(Double::compareTo).orElse(0.0);
                for (FeatureMatrix featureMatrix : featureMatrices) {
                    for (FeatureVector featureVector : featureMatrix.getFeatureVectors()) {
                        expValues.put(featureVector, Math.exp(scores.get(featureVector) - maxSuperscript));
                    }
                }
                double sumExpValues = expValues.values().stream().reduce(0.0, Double::sum);
                double[] tmpValues = newFeatureArray();
                for (FeatureMatrix featureMatrix : featureMatrices) {
                    for (FeatureVector featureVector : featureMatrix.getFeatureVectors()) {
                        for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
                            int featureCrossId = featureCross.getId();
//                            tmpValues[featureCrossId] += expValues[i] * 1;
                            tmpValues[featureCrossId] += expValues.get(featureVector) * featureCross.getDegree();
                        }
                    }
                }
                // compute delta
                for (int i = 0; i < tmpValues.length; i++) {
                    delta.dec(i, tmpValues[i] / sumExpValues);
                }
                int markedScale = 0;
                for (FeatureMatrix featureMatrix : featureMatrices) {
                    if (featureMatrix.isMarked()) {
                        markedScale += featureMatrix.getFeatureVectors().size();
                        break; // we only have one marked FeatureMatrix
                    }
                }
                for (FeatureMatrix featureMatrix : featureMatrices) {
                    if (featureMatrix.isMarked()) {
                        for (FeatureVector featureVector : featureMatrix.getFeatureVectors()) {
                            for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
//                                delta.inc(featureCross.getId(), 1.0 / markedSize);
                                delta.inc(featureCross.getId(), featureCross.getDegree() / markedScale);
                            }
                        }
                        break; // we only have one marked FeatureMatrix
                    }
                }
            }
            // compute delta
            for (int i = 0; i < delta.size(); i++) {
                delta.div(i, trainingData.size());
                delta.dec(i, lambda * (Math.signum(theta.get(i)) + 2 * theta.get(i)));
            }
            // update theta
            for (int i = 0; i < delta.size(); i++) {
                theta.inc(i, eta * delta.get(i));
            }
            // handle validation data
            double gamma = 0;
            for (Sample sample : validationData) {
                List<FeatureMatrix> featureMatrices = sample.getFeatureMatrices();
                Map<FeatureVector, Double> scores = new HashMap<>();
                for (FeatureMatrix featureMatrix : featureMatrices) {
                    for (FeatureVector featureVector : featureMatrix.getFeatureVectors()) {
                        scores.put(featureVector, featureVector.score(theta));
                    }
                }
                int ranking = 0;
                int rankingScale = 0;
                for (FeatureMatrix featureMatrixI : featureMatrices) {
                    if (featureMatrixI.isMarked()) {
                        for (FeatureMatrix featureMatrixJ : featureMatrices) {
                            if (!featureMatrixJ.isMarked()) {
                                for (FeatureVector markedFeatureVector : featureMatrixI.getFeatureVectors()) {
                                    Double scoreOfMarkedFeatureVector = scores.get(markedFeatureVector);
                                    for (FeatureVector unmarkedFeatureVector : featureMatrixJ.getFeatureVectors()) {
                                        Double scoreOfUnmarkedFeatureVector = scores.get(unmarkedFeatureVector);
                                        if (scoreOfUnmarkedFeatureVector >= scoreOfMarkedFeatureVector){
                                            ranking++;
                                        }
                                        rankingScale++;
                                    }
                                }
                            }
                        }
                        break; // we only have one marked FeatureMatrix
                    }
                }
                // why add 1 to the denominator? as in few cases we have no generated patches
                // for example, when we are utilizing restricted patch-generators such as SPR
                gamma += ((double) ranking) / (1 + rankingScale);
            }
            gamma /= validationData.size();
            // update results
            if (bestGamma > gamma) {
                epoch = 0;
                bestTheta.clone(theta);
                bestGamma = gamma;
                logger.log(Level.INFO, epoch + " Update BestGamma " + bestGamma);
            } else if (eta > 0.01) {
                eta *= 0.9;
//                logger.log(Level.INFO, epoch + " Drop eta to " + eta);
//            } else {
//                logger.log(Level.INFO, epoch + " Keep eta as " + eta);
            }
        }
        bestTheta.gamma = bestGamma;
        logger.log(Level.INFO, "BestGamma " + bestGamma);
        return bestTheta;
    }

    // consider CLR(Cyclical Learning Rates) or autoML
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
            sample.loadFeatureMatrices();
            folds.get(i % k).add(sample);
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
            if (option.learnerOption == Option.LearnerOption.CROSS_ENTROPY) {
                ParameterVector parameterVector = learn(trainingData, validationData);
                averageGamma += parameterVector.gamma;
                if (bestGamma > parameterVector.gamma) {
                    bestGamma = parameterVector.gamma;
                    bestParameterVector = parameterVector;
                }
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
