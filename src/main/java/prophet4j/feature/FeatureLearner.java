package prophet4j.feature;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import prophet4j.defined.FeatureStruct;
import prophet4j.defined.FeatureType;
import prophet4j.defined.FeatureStruct.*;

// based on learner.cpp (utilize diff tool to focus on the differences between models)
public class FeatureLearner {
    // check these variables
    @Parameters(arity = "1..*", index = "0..*", paramLabel = "train_list_file", description = "The list of extracted feature vector files!")
    private String[] ConfFile; // Required // Positional // here Array could be List
    @Option(names = {"--alpha"}, paramLabel = "learning_rate", description = "The initial learning rate!")
    private double LearningRate = 1.0;
    @Option(names = {"--lambda"}, paramLabel = "regularity_lambda", description = "The regularity factor!")
    private double Regularity = 1e-3;
    @Option(names = {"--lambdal1"}, paramLabel = "regularity_lambdal1", description = "The regularity l1 factor, default 1e-3")
    private double RegularityL1 = 1e-3;
    @Option(names = {"-v", "--validation-ratio"}, paramLabel = "validation_ratio", description = "The part of training that we are going to use for validation!")
    private double ValidationRatio = 0.15;
    @Option(names = {"-o", "--out-file"}, paramLabel = "output_file", description = "The output file for the parameter!")
    private String OutFile = "para.out";
    // actually need to check all related implementations about dump function (GU ignored them)
    @Option(names = {"--dump-feature"}, description = "Dump Feature")
    private boolean DumpFeature = false;
    @Option(names = {"--hmargin"}, description = "margin value for hingloss")
    private double HingeMargin = 0.1;
    @Option(names = {"--algo"}, description = "entropy_or_hingeloss")
    private String LearningAlgo = "entropy";

    private static final Logger logger = LogManager.getLogger(FeatureLearner.class.getName());

    // Maximum Entropy Model // todo: compare TrainingCase with Context
    ParameterVector maximumEntropyModel(List<TrainingCase> trainSet, List<TrainingCase> validateSet) {
        ParameterVector Theta = new ParameterVector();
        ParameterVector bestTheta = new ParameterVector();
        bestTheta.clone(Theta);

        double alpha = LearningRate, bestResV = -1e20;
        int round = 0, last_update = 0;

        while (last_update + 200 > round) {
            round++;
            double resT = 0;
            ParameterVector delta = new ParameterVector();
            for (int i = 0; i < trainSet.size(); i++) {
                TrainingCase c = trainSet.get(i);
                double[] a = new double[c.cases.size()];

                double sumExp = 0;
                for (int j = 0; j < c.cases.size(); j++) {
                    a[j] = Theta.dotProduct(c.cases.get(j));
                    sumExp += Math.exp(a[j]);
                }
                for (int j = 0; j < c.cases.size(); j++) {
                    double p = Math.exp(a[j]) / sumExp;
                    FeatureVector vec = c.cases.get(j);
                    for (int k = 0; k < vec.size(); k++) {
                        int idx = vec.get(k);
                        delta.set(idx, delta.get(idx) - p);
                        if (vec.getMark()) {
                            delta.set(idx, delta.get(idx) + 1.0 / c.marked.size());
                        }
                    }
                    if (vec.getMark())
                        resT += a[j] / c.marked.size();
                }
                resT -= Math.log(sumExp);
            }
            resT /= trainSet.size();
            double adjustedResT = resT;
            for (int i = 0; i < delta.size(); i++) {
                delta.set(i, delta.get(i) / trainSet.size() - 2 * Regularity * Theta.get(i) - RegularityL1 * sign(Theta.get(i)));
                adjustedResT -= Regularity * Theta.get(i) * Theta.get(i) - RegularityL1 * Math.abs(Theta.get(i));
            }
            // update the Theta
            for (int i = 0; i < delta.size(); i++) {
                Theta.set(i, Theta.get(i) + alpha * delta.get(i));
            }
            // validation set
            double resV = 0;
            for (int i = 0; i < validateSet.size(); i++) {
                TrainingCase c = validateSet.get(i);
                double[] a = new double[c.cases.size()];
                for (int j = 0; j < c.cases.size(); j++)
                    a[j] = Theta.dotProduct(c.cases.get(j));
                int max_beat = 0;
//                System.out.println("c.marked.size():" + c.marked.size() + " c.cases.size():" + c.cases.size());
                for (int k = 0; k < c.marked.size(); k++) {
                    int beat = 0;
                    for (int j = 0; j < c.cases.size(); j++) {
//                        System.out.println("j:" + j + " c.marked.get(k):" + c.marked.get(k));
//                        System.out.println("a[j]:" + a[j] + " a[c.marked.get(k)]:" + a[c.marked.get(k)]);
                        if (a[j] < a[c.marked.get(k)] || ((a[j] == a[c.marked.get(k)]) && (j > c.marked.get(k))))
                            beat++;
                    }
                    max_beat = max_beat < beat ? beat : max_beat;
                }
                resV += ((double) (max_beat) / c.cases.size()) / validateSet.size();
            }
            double adjustedResV = resV;
//            logger.log(Level.INFO, "Round " + round + ": resT " + resT + " adjResT " + adjustedResT + " resV " + resV + " adjResV " + adjustedResV);
            if (resV > bestResV) {
                bestTheta = Theta;
                bestResV = resV;
                last_update = round;
                logger.log(Level.INFO, "Update best!");
            } else if (alpha > 1) {
                alpha *= 0.1;
//                logger.log(Level.INFO, "Drop alpha to " + alpha);
            }
        }
        logger.log(Level.INFO, getClass().getName() + " bestResV " + bestResV);
        return bestTheta;
    }

    // Simple SVM Model
    ParameterVector simpleSVMModel(List<TrainingCase> trainSet, List<TrainingCase> validateSet) {
        ParameterVector Theta = new ParameterVector();
        ParameterVector bestTheta = new ParameterVector();
        bestTheta.clone(Theta);

        double alpha = LearningRate, bestResV = -1e20;
        int round = 0, last_update = 0;

        while (last_update + 200 > round) {
            round++;
            double resT = 0;
            ParameterVector delta = new ParameterVector();
            for (int i = 0; i < trainSet.size(); i++) {
                TrainingCase c = trainSet.get(i);
                double[] a = new double[c.cases.size()];

                int tot_marked = c.marked.size();
                int tot_unmarked = c.cases.size() - tot_marked;
                double diff = 0;
                for (int j = 0; j < c.cases.size(); j++) {
                    a[j] = Theta.dotProduct(c.cases.get(j));
                    if (c.cases.get(j).getMark()) {
                        diff += a[j] / tot_marked;
                    } else {
                        diff -= a[j] / tot_unmarked;
                    }
                }
                if (diff < HingeMargin) {
                    resT += diff - HingeMargin;
                    for (int j = 0; j < c.cases.size(); j++) {
                        FeatureVector vec = c.cases.get(j);
                        if (c.cases.get(j).getMark()) {
                            for (int k = 0; k < vec.size(); k++)
                                delta.set(vec.get(k), delta.get(vec.get(k)) + 1.0 / tot_marked);
                        } else {
                            for (int k = 0; k < vec.size(); k++)
                                delta.set(vec.get(k), delta.get(vec.get(k)) - 1.0 / tot_unmarked);
                        }
                    }
                }
            }
            resT /= trainSet.size();
            double adjustedResT = resT;
            for (int i = 0; i < delta.size(); i++) {
                delta.set(i, delta.get(i) / trainSet.size() - 2 * Regularity * Theta.get(i) - RegularityL1 * sign(Theta.get(i)));
                adjustedResT -= Regularity * Theta.get(i) * Theta.get(i) - RegularityL1 * Math.abs(Theta.get(i));
            }
            // update the Theta
            for (int i = 0; i < delta.size(); i++) {
                Theta.set(i, Theta.get(i) + alpha * delta.get(i));
            }
            // validation set
            double resV = 0;
            for (int i = 0; i < validateSet.size(); i++) {
                TrainingCase c = validateSet.get(i);
                double[] a = new double[c.cases.size()];
                for (int j = 0; j < c.cases.size(); j++)
                    a[j] = Theta.dotProduct(c.cases.get(j));
                int max_beat = 0;
                for (int k = 0; k < c.marked.size(); k++) {
                    int beat = 0;
                    for (int j = 0; j < c.cases.size(); j++)
                        if (a[j] < a[c.marked.get(k)] || ((a[j] == a[c.marked.get(k)]) && (j > c.marked.get(k))))
                            beat++;
                    max_beat = max_beat < beat ? beat : max_beat;
                }
                resV += ((double) (max_beat) / c.cases.size()) / validateSet.size();
            }
            double adjustedResV = resV;
//            logger.log(Level.INFO, "Round " + round + ": resT " + resT + " adjResT " + adjustedResT + " resV " + resV + " adjResV " + adjustedResV);
            if (resV > bestResV) {
                bestTheta = Theta;
                bestResV = resV;
                last_update = round;
                logger.log(Level.INFO, "Update best!");
            } else if (alpha > 0.004) {
                if (last_update + 50 < round) {
                    alpha *= 0.9;
//                    logger.log(Level.INFO, "Drop alpha to " + alpha);
                    last_update = round;
                }
            }
        }
        logger.log(Level.INFO, getClass().getName() + " bestResV " + bestResV);
        return bestTheta;
    }

    // Hinge Loss Model
    ParameterVector hingeLossModel(List<TrainingCase> trainSet, List<TrainingCase> validateSet) {
        ParameterVector Theta = new ParameterVector();
        ParameterVector bestTheta = new ParameterVector();
        bestTheta.clone(Theta);

        double alpha = LearningRate, bestResV = -1e20;
        int round = 0, last_update = 0;

        while (last_update + 200 > round) {
            round++;
            double resT = 0;
            ParameterVector delta = new ParameterVector();
            for (int i = 0; i < trainSet.size(); i++) {
                TrainingCase c = trainSet.get(i);
                double[] a = new double[c.cases.size()];

                double bestMarked = -1e20, bestUnmarked = -1e20;
                int bestMarkedIdx = 0, bestUnmarkedIdx = 0;
                for (int j = 0; j < c.cases.size(); j++) {
                    a[j] = Theta.dotProduct(c.cases.get(j));
                    if (c.cases.get(j).getMark()) {
                        if (bestMarked < a[j]) {
                            bestMarked = a[j];
                            bestMarkedIdx = j;
                        }
                    }
                }
                FeatureVector bestMarkedVec = c.cases.get(bestMarkedIdx);
                for (int j = 0; j < c.cases.size(); j++) {
                    if (!c.cases.get(j).getMark()) {
                        FeatureVector vec = c.cases.get(j);
                        double dis = vectorDistance(bestMarkedVec, vec) * HingeMargin;
                        if (a[j] + dis > bestUnmarked) {
                            bestUnmarked = a[j] + dis;
                            bestUnmarkedIdx = j;
                        }
                    }
                }
                double hinge = bestMarked - bestUnmarked;
                if (hinge < 0) {
                    resT += hinge;
                    FeatureVector vec = c.cases.get(bestMarkedIdx);
                    for (int j = 0; j < vec.size(); j++)
                        delta.set(vec.get(j), delta.get(vec.get(j)) + 1);
                    FeatureVector vec1 = c.cases.get(bestUnmarkedIdx);
                    for (int j = 0; j < vec1.size(); j++)
                        delta.set(vec1.get(j), delta.get(vec1.get(j)) - 1);
                }
            }
            resT /= trainSet.size();
            double adjustedResT = resT;
            for (int i = 0; i < delta.size(); i++) {
                delta.set(i, delta.get(i) / trainSet.size() - 2 * Regularity * Theta.get(i) - RegularityL1 * sign(Theta.get(i)));
                adjustedResT -= Regularity * Theta.get(i) * Theta.get(i) - RegularityL1 * Math.abs(Theta.get(i));
            }
            // update the Theta
            for (int i = 0; i < delta.size(); i++) {
                Theta.set(i, Theta.get(i) + alpha * delta.get(i));
            }
            // validation set
            double resV = 0;
            for (int i = 0; i < validateSet.size(); i++) {
                TrainingCase c = validateSet.get(i);
                double[] a = new double[c.cases.size()];
                for (int j = 0; j < c.cases.size(); j++)
                    a[j] = Theta.dotProduct(c.cases.get(j));
                int max_beat = 0;
                for (int k = 0; k < c.marked.size(); k++) {
                    int beat = 0;
                    for (int j = 0; j < c.cases.size(); j++)
                        if (a[j] < a[c.marked.get(k)] || ((a[j] == a[c.marked.get(k)]) && (j > c.marked.get(k))))
                            beat++;
                    max_beat = max_beat < beat ? beat : max_beat;
                }
                resV += ((double) (max_beat) / c.cases.size()) / validateSet.size();
            }
            double adjustedResV = resV;
//            logger.log(Level.INFO, "Round " + round + ": resT " + resT + " adjResT " + adjustedResT + " resV " + resV + " adjResV " + adjustedResV);
            if (resV > bestResV) {
                bestTheta = Theta;
                bestResV = resV;
                last_update = round;
                logger.log(Level.INFO, "Update best!");
            } else if (alpha > 0.004) {
                if (last_update + 100 < round) {
                    alpha *= 0.5;
//                    logger.log(Level.INFO, "Drop alpha to " + alpha);
                    last_update = round;
                }
            }
        }
        logger.log(Level.INFO, getClass().getName() + " bestResV " + bestResV);
        return bestTheta;
    }

    private double sign(double x) {
        // if this round operation is not important then replace sign() with Math.signum()
        if (Math.abs(x) < 1e-6)
            return 0;
        else if (x > 0)
            return 1;
        else
            return -1;
    }

    private double vectorDistance(FeatureVector vec1, FeatureVector vec2) {
        int i = 0, j = 0;
        double ret = 0;
        while ((i < vec1.size()) && (j < vec2.size())) {
            if (vec1.get(i) == vec2.get(j)) {
                i++;
                j++;
            } else if (vec1.get(i) < vec2.get(j)) {
                i++;
                ret++;
            } else if (vec1.get(i) > vec2.get(j)) {
                j++;
                ret++;
            }
        }
        if (i < vec1.size())
            ret += vec1.size() - i;
        if (j < vec2.size())
            ret += vec2.size() - j;
        return Math.sqrt(ret);
    }

    // todo: should this unused function be converted to be corresponding test class ?
    // todo: find the NearLocations ? which occurred in the description
    public static void main(String[] args) {
        FeatureLearner featureLearner = new FeatureLearner();
        new CommandLine(featureLearner).parse(args);

        long total_cases = 0;
        long total_volume = 0;

        File file;
        FileInputStream in;
        InputStreamReader streamReader;
        BufferedReader bufferReader;

        // create wholeSet to avoid using extra deep copy operation
        List<TrainingCase> wholeSet = new ArrayList<>();
        List<TrainingCase> trainSet = new ArrayList<>();
        List<TrainingCase> validateSet = new ArrayList<>();
        List<String> trainingFileList = new ArrayList<>(Arrays.asList(featureLearner.ConfFile));
        try {
            wholeSet.clear();
            for (int i = 0; i < trainingFileList.size(); i++) {
                file = new File(trainingFileList.get(i));
                in = new FileInputStream(file);
                streamReader = new InputStreamReader(in);
                bufferReader = new BufferedReader(streamReader);

                logger.log(Level.INFO, "Processing file " + trainingFileList.get(i));
                // check this initialization
                TrainingCase c = new TrainingCase();
                c.cases.clear();
                c.marked.clear();
                FeatureVector vec = new FeatureVector();
                // todo: check the format of read file
                String str;
                while ((str = bufferReader.readLine()) != null) {
                    for (int j = 0; j < Integer.valueOf(str); j++) {
                        int v = Integer.valueOf(bufferReader.readLine());
                        vec.set(j, v); // in this line, originally written as add(), replace with set()
                    }
                    vec.setMark(Boolean.valueOf(bufferReader.readLine())); // here take action, as the setMark()

                    c.cases.add(vec);
                    if (vec.getMark())
                        c.marked.add(c.cases.size() - 1);
                    total_cases++;
                    total_volume += vec.size();
                }
                wholeSet.add(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
//        } finally {
//            try {
//                if (bufferReader != null) {
//                    bufferReader.close();
//                }
//                if (streamReader != null) {
//                    streamReader.close();
//                }
//                if (in != null) {
//                    in.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        logger.log(Level.INFO, "Total number of features: " + FeatureType.FEATURE_SIZE);
        logger.log(Level.INFO, "Total number of whole set: " + wholeSet.size());
        logger.log(Level.INFO, "Total number of cases: " + total_cases);
        logger.log(Level.INFO, "Total volume: " + total_volume);

        assert (featureLearner.ValidationRatio < 0.5);
        int validation_size = (int) (wholeSet.size() * featureLearner.ValidationRatio);
        assert (validation_size < wholeSet.size());

        // split validation
        int k = wholeSet.size() / validation_size;
        trainSet.clear();
        validateSet.clear();
        for (int i = 0; i < wholeSet.size(); i++)
            if ((i < validation_size * k) && (i % k == 0))
                validateSet.add(wholeSet.get(i));
            else
                trainSet.add(wholeSet.get(i));

        logger.log(Level.INFO, "Total number for training: " + trainSet.size());
        logger.log(Level.INFO, "Total number for validation: " + validateSet.size());

        // todo: directly print results right now, turn to save results to files using learner.OutFile
        switch (featureLearner.LearningAlgo) {
            case "entropy":
                logger.log(Level.INFO, featureLearner.maximumEntropyModel(trainSet, validateSet));
                break;
            case "simple":
                logger.log(Level.INFO, featureLearner.simpleSVMModel(trainSet, validateSet));
                break;
            case "ssvm":
                logger.log(Level.INFO, featureLearner.hingeLossModel(trainSet, validateSet));
                break;
            default:
                logger.log(Level.INFO, "Unsupported algorithm!");
        }
    }

    // TODO: this is almost same as the unused function above, combine them
    public void func4Demo(List<String> filePaths) {
        long total_cases = 0;
        long total_volume = 0;

        File file;

        // create wholeSet to avoid using extra deep copy operation
        List<TrainingCase> wholeSet = new ArrayList<>();
        List<TrainingCase> trainSet = new ArrayList<>();
        List<TrainingCase> validateSet = new ArrayList<>();
        try {
            for (String filePath : filePaths) {
                file = new File(filePath);
                logger.log(Level.INFO, "Processing file " + filePath);
                // check this initialization
                TrainingCase c = new TrainingCase();
                c.cases.clear();
                c.marked.clear();
                for (FeatureVector vec : FeatureStruct.load(file)) {
                    c.cases.add(vec);
                    if (vec.getMark())
                        c.marked.add(c.cases.size() - 1);
                    total_cases++;
                    total_volume += vec.size();
                }
                wholeSet.add(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.log(Level.INFO, "Total number of features: " + FeatureType.FEATURE_SIZE);
        logger.log(Level.INFO, "Total number of whole set: " + wholeSet.size());
        logger.log(Level.INFO, "Total number of cases: " + total_cases);
        logger.log(Level.INFO, "Total volume: " + total_volume);

        int validation_size = (int) (wholeSet.size() * 0.15);
        assert (validation_size < wholeSet.size());

        // split validation
        int k = wholeSet.size() / validation_size;
        trainSet.clear();
        validateSet.clear();
        for (int i = 0; i < wholeSet.size(); i++)
            if ((i < validation_size * k) && (i % k == 0))
                validateSet.add(wholeSet.get(i));
            else
                trainSet.add(wholeSet.get(i));

        logger.log(Level.INFO, "Total number for training: " + trainSet.size());
        logger.log(Level.INFO, "Total number for validation: " + validateSet.size());

        logger.log(Level.INFO, maximumEntropyModel(trainSet, validateSet));
//        logger.log(Level.INFO, extraLearner.simpleSVMModel(trainSet, validateSet));
//        logger.log(Level.INFO, extraLearner.hingeLossModel(trainSet, validateSet));
    }
}
