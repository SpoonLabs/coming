package fr.inria.prophet4j.utility;

import com.google.gson.Gson;
import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.feature.S4R.S4RFeature;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature;
import fr.inria.prophet4j.feature.original.OriginalFeature;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtElement;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public interface Structure {
    class Sample { // namely TrainingCase
        private String filePath;
        private List<FeatureMatrix> featureMatrices;

        public Sample(String filePath) {
            this.filePath = filePath;
            this.featureMatrices = new ArrayList<>();
        }

        public List<FeatureMatrix> getFeatureMatrices() {
            return featureMatrices;
        }

        public void loadFeatureMatrices() {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                ObjectInputStream ois = new ObjectInputStream(fis);
                featureMatrices = (List<FeatureMatrix>) ois.readObject();
                /*
                // if we need to try on merged feature-vector
                // however it usually not performances better
                List<FeatureMatrix> tmpFeatureMatrices = new ArrayList<>();
                for (FeatureMatrix featureMatrix: featureMatrices) {
                    FeatureVector tmpFeatureVector = new FeatureVector();
                    for (FeatureVector featureVector: featureMatrix.featureVectors) {
                        tmpFeatureVector.merge(featureVector);
                    }
                    List<FeatureVector> tmpFeatureVectors = new ArrayList<>();
                    tmpFeatureVectors.add(tmpFeatureVector);
                    tmpFeatureMatrices.add(new FeatureMatrix(featureMatrix.marked, featureMatrix.fileKey, tmpFeatureVectors));
                }
                featureMatrices = tmpFeatureMatrices;
                 */
                ois.close();
                fis.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }

        public void saveFeatureMatrices(List<FeatureMatrix> featureMatrices) {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(filePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(featureMatrices);
                oos.flush();
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // as we do not utilize json-format data, we have no loadJson method
        // the root reason is that prophet4j owns sub-features
        public void saveAsJson(FeatureOption featureOption) {
            int arraySize = 0;
            switch (featureOption) {
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

            Map<String, List<double[]>> featureMatrixList = new HashMap<>();
            for (FeatureMatrix featureMatrix : featureMatrices) {
                List<double[]> featureVectorList = new ArrayList<>();
                // always skip this loop when we meet invalid patches
                for (FeatureVector featureVector : featureMatrix.getFeatureVectors()) {
                    double[] featureCrossArray = new double[arraySize];
                    for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
                        featureCrossArray[featureCross.getId()] = featureCross.getDegree();
                    }
                    featureVectorList.add(featureCrossArray);
                }
                featureMatrixList.put(featureMatrix.fileKey, featureVectorList);
            }
            String json = new Gson().toJson(featureMatrixList);
            String jsonPath = filePath.replace("prophet4j/_BIN/", "prophet4j/_JSON/");
            jsonPath = jsonPath.replace(".bin", ".json");
            try {
                File file = new File(jsonPath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                Files.write(Paths.get(jsonPath), json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return filePath;
        }
    }

    // entity which contains feature-crosses to express characteristics of each diff-operation
    class FeatureVector implements Serializable {
        static final long serialVersionUID = 1L;
        private Set<FeatureCross> featureCrosses;

        public FeatureVector() {
            this.featureCrosses = new HashSet<>();
        }

        public boolean containFeature(Feature feature) {
            for (FeatureCross featureCross : featureCrosses) {
                if (featureCross.containFeature(feature)) {
                    return true;
                }
            }
            return false;
        }

        public void addFeatureCross(FeatureCross featureCross) {
            featureCrosses.add(featureCross);
        }

        // if we return set directly, results would be unpredictable. DO NOT return set directly
        public List<FeatureCross> getFeatureCrosses() {
            List<FeatureCross> list = new ArrayList<>(featureCrosses);
            list.sort(Comparator.comparingInt(FeatureCross::getId));
            return list;
        }
        
        public List<FeatureCross> getNonSortedFeatureCrosses() {
            List<FeatureCross> list = new ArrayList<>(featureCrosses);
            return list;
        }

        public void merge(FeatureVector featureVector) {
            this.featureCrosses.addAll(featureVector.getFeatureCrosses());
        }

        public double score(ParameterVector parameterVector) {
            // scores means values of phi dotProduct theta
            return parameterVector.dotProduct(this);
        }

        @Override
        public String toString() {
            return "FeatureCrosses: " + featureCrosses;
        }
    }

    // entity which contains feature-vectors to express multi-diff-operations of each patch
    class FeatureMatrix implements Serializable {
        static final long serialVersionUID = 1L;
        private boolean marked; // for human patch or not
        private String fileKey;
        private List<FeatureVector> featureVectors;

        public FeatureMatrix(boolean marked, String fileKey, List<FeatureVector> featureVectors) {
            this.marked = marked;
            this.fileKey = fileKey;
            this.featureVectors = featureVectors;
        }

        public boolean containFeature(Feature feature) {
            for (FeatureVector featureVector : featureVectors) {
                if (featureVector.containFeature(feature)) {
                    return true;
                }
            }
            return false;
        }

        public List<FeatureVector> getFeatureVectors() {
            return featureVectors;
        }

        public boolean isMarked() {
            return this.marked;
        }

        // to mark generated patches false
        public void correctMarked() {
            this.marked = false;
        }

        public double score(ParameterVector parameterVector) {
            double score = 0;
            // we compute the sum as the whole score
            for (FeatureVector featureVector : featureVectors) {
                score += featureVector.score(parameterVector);
            }
            return score;
        }

        @Override
        public String toString() {
            return "FeatureVectors: " + featureVectors;
        }

    }

    // entity which contains weights for all feature-crosses
    class ParameterVector {
        public double gamma = 0;
        private int arraySize = 0;
        private double[] parameterArray;

        public ParameterVector(FeatureOption featureOption) {
            switch (featureOption) {
                case ENHANCED:
                    throw new RuntimeException("removed see https://github.com/SpoonLabs/coming/issues/235");
                case EXTENDED:
                    throw new RuntimeException("class removed by Martin for cleaning");
                case ORIGINAL:
                    this.arraySize = OriginalFeature.FEATURE_SIZE;
                    break;
                case S4R:
                    this.arraySize = S4RFeature.FEATURE_SIZE;
                    break;
                case S4RO:
                    throw new RuntimeException("removed see https://github.com/SpoonLabs/coming/issues/235");
            }
            this.parameterArray = new double[arraySize];
        }

        public int size() {
            return parameterArray.length;
        }

        public double get(int index) {
            return parameterArray[index];
        }

        // +=
        public void inc(int index, double value) {
            parameterArray[index] += value;
        }

        // -=
        public void dec(int index, double value) {
            parameterArray[index] -= value;
        }

        // /=
        public void div(int index, double value) {
            parameterArray[index] /= value;
        }

        public void clone(ParameterVector parameterVector) {
            this.arraySize = parameterVector.arraySize;
            this.parameterArray = parameterVector.parameterArray.clone();
        }

        public double dotProduct(FeatureVector featureVector) {
            double res = 0;
            for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
                int featureCrossId = featureCross.getId();
                res += parameterArray[featureCrossId];
            }
            return res;
        }

        public void load(String filePath) {
            try {
                File vectorFile = new File(filePath);
                String string = FileUtils.readFileToString(vectorFile, Charset.defaultCharset());
                String[] substrings = string.split(" ");
                parameterArray = Arrays.stream(substrings).mapToDouble(Double::valueOf).toArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void save(String filePath) {
            try {
                StringJoiner stringJoiner = new StringJoiner(" ");
                for (double parameter : parameterArray) {
                    stringJoiner.add(String.valueOf(parameter));
                }
                File vectorFile = new File(filePath);
                FileUtils.writeStringToFile(vectorFile, stringJoiner.toString(), Charset.defaultCharset(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    enum DiffType {
        DeleteType,
        InsertType,
        UpdateType,
        //Delete and Move
        PartialDeleteType,
    }

    enum RepairKind { // implementation is at RepairGenerator.java
        // INSERT_CONTROL_RF
        IfExitKind,             // genAddIfExit()
        // INSERT_GUARD_RF
        GuardKind,              // genAddIfGuard()
        SpecialGuardKind,       // genAddIfGuard()
        // INSERT_STMT_RF
        AddInitKind,            // Inapplicable to Java
        AddAndReplaceKind,      // genAddStatement()
        // REPLACE_COND_RF
        TightenConditionKind,   // genTightCondition()
        LoosenConditionKind,    // genLooseCondition()
        // REPLACE_STMT_RF
        ReplaceKind,            // genReplaceStmt()
        ReplaceStringKind,      // genReplaceStmt()
        //code deletion related features
        RemovePartialIFKind,
        RemoveSTMTKind,
        RemoveWholeIFKind,
        RemoveWholeBlockKind,
    }

    class DiffEntry { // DiffResultEntry
        // the reason why CtElement is used here is because clang::Expr isa clang::Stmt
        public DiffType type;
        public CtElement srcNode, dstNode;

        public DiffEntry(DiffType type, CtElement srcNode, CtElement dstNode) {
            this.type = type;
            this.srcNode = srcNode;
            this.dstNode = dstNode;
        }
    }

    class Repair {
        public RepairKind kind;
        public boolean isReplace;
        public CtElement srcElem, dstElem; // from RepairAction
        public List<CtElement> atoms; // from RepairAction
        public CtElement oldRExpr, newRExpr; // only for UpdateType

        public Repair() {
            this.kind = null;
            this.isReplace = false;
            this.srcElem = null;
            this.dstElem = null;
            this.atoms = new ArrayList<>();
            this.oldRExpr = null;
            this.newRExpr = null;
        }

        public Set<CtElement> getCandidateAtoms() {
            Set<CtElement> ret = new HashSet<>();
            ret.add(null); // ?
            ret.addAll(atoms);
            return ret;
        }

        @Override
        public String toString() {
            return "Repair{" +
                    "kind=" + kind +
                    ", isReplace=" + isReplace +
                    ", srcElem=" + srcElem +
                    ", dstElem=" + dstElem +
                    ", atoms=" + atoms +
                    ", oldRExpr=" + oldRExpr +
                    ", newRExpr=" + newRExpr +
                    '}';
        }
    }
}
