package fr.inria.prophet4j.defined;

import java.util.*;

import fr.inria.prophet4j.defined.extended.ExtendedFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Support;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.ParameterVector;
import fr.inria.prophet4j.defined.Structure.Sample;

// based on learner.cpp (follow the way of ProphetPaper)
public class FeatureLearner {
    private Option option;
    private List<Sample> sampleData;

    private static final Logger logger = LogManager.getLogger(FeatureLearner.class.getName());

    public FeatureLearner(Option option) {
        this.option = option;
        this.sampleData = new ArrayList<>();
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
            case EXTENDED:
                arraySize = ExtendedFeature.FEATURE_SIZE;
                break;
            case ORIGINAL:
                arraySize = OriginalFeature.FEATURE_SIZE;
                break;
        }
        return new double[arraySize];
    }

    private ParameterVector learn(List<Sample> trainingData, List<Sample> validationData) {
        double alpha = 1;
        double gammaBest = 1;
        final double lambda = 1e-3;
        ParameterVector theta = new ParameterVector(option.featureOption);
        ParameterVector thetaBest = new ParameterVector(option.featureOption);

        int count = 0;
        while (count < 200) {
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
                switch (option.getModelOption()) {
                    case CROSS_ENTROPY: {
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
                                tmpValues[featureCrossId] += expValues[i] * 1;
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
                                    delta.inc(featureCross.getId(), 1.0 / markedSize);
                                }
                            }
                        }
                    }
                    break;
                    case SUPPORT_VECTOR_MACHINE: {
                        // unsuitable as we have multiVectors for each patch todo check
                        double[] tmpValues = newFeatureArray();
                        for (int i = 0; i < featureVectors.size(); i++) {
                            FeatureVector featureVector = featureVectors.get(i);
                            List<FeatureCross> featureCrosses = featureVector.getFeatureCrosses();
                            for (FeatureCross featureCross : featureCrosses) {
                                int featureCrossId = featureCross.getId();
                                tmpValues[featureCrossId] += Math.max(0, scores[i] - scores[0] + 1);
                            }
                        }
                        for (int i = 0; i < tmpValues.length; i++) {
                            delta.dec(i, tmpValues[i]);
                        }
                    }
                    break;
                }
            }
            // compute delta
            for (int i = 0; i < delta.size(); i++) {
                delta.div(i, trainingData.size());
                switch (option.getModelOption()) {
                    case CROSS_ENTROPY:
                        // I feel L1 normalization term is not necessary todo consider
                        delta.dec(i, lambda * (Math.signum(theta.get(i)) + 2 * theta.get(i)));
                        break;
                    case SUPPORT_VECTOR_MACHINE:
                        delta.dec(i, lambda * (2 * theta.get(i)));
                        break;
                }
            }
            // update theta
            for (int i = 0; i < delta.size(); i++) {
                theta.inc(i, alpha * delta.get(i));
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
            count += 1;
            if (gammaBest > gamma) {
                thetaBest.clone(theta);
                gammaBest = gamma;
                count = 0;
                logger.log(Level.INFO, count + " Update best gamma " + gammaBest);
            } else if (alpha > 0.01) {
                alpha *= 0.9;
//                logger.log(Level.INFO, count + " Drop alpha to " + alpha);
//            } else {
//                logger.log(Level.INFO, count + " Keep alpha as " + alpha);
            }
        }
        logger.log(Level.INFO, "BestGamma " + gammaBest);
        thetaBest.gamma = gammaBest;
        return thetaBest;
    }

    public void func4Demo(List<String> filePaths) {
        String parameterFilePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option) + "ParameterVector";
        // sort all sample data as we want one distinct baseline
        filePaths.sort(String::compareTo);
        for (String filePath : filePaths) {
//            logger.log(Level.INFO, "Processing file " + filePath);
            Sample sample = new Sample(filePath);
            // it is possible as FeatureVector only utilize Set<FeatureCross>
            sample.loadFeatureVectors();
            sampleData.add(sample);
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
        double gammaBest = 1;
        double gammaAverage = 0;
        for (int i = 0; i < k; i++) {
            List<Sample> trainingData = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                if (j != i) {
                    trainingData.addAll(folds.get(j));
                }
            }
            List<Sample> validationData = new ArrayList<>(folds.get(i));
            ParameterVector parameterVector = learn(trainingData, validationData);
            gammaAverage += parameterVector.gamma;
            if (parameterVector.gamma < gammaBest) {
                gammaBest = parameterVector.gamma;
                parameterVector.save(parameterFilePath);
            }
        }
        gammaAverage /= k;
        logger.log(Level.INFO, k + "-fold Cross Validation: " + gammaAverage);
        System.out.println("ParameterVector is saved to " + parameterFilePath);
    }
}
