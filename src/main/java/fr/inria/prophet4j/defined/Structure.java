package fr.inria.prophet4j.defined;

import fr.inria.prophet4j.defined.extended.ExtendedFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtElement;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public interface Structure {
    enum FeatureOption {
        EXTENDED,
        ORIGINAL,
    }

    class Sample { // namely TrainingCase
        private String filePath;
        private List<FeatureVector> featureVectors;

        public Sample(String filePath) {
            this.filePath = filePath;
            this.featureVectors = new ArrayList<>();
        }

        public List<FeatureVector> getFeatureVectors() {
            return featureVectors;
        }

        public void loadFeatureVectors() {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                ObjectInputStream ois = new ObjectInputStream(fis);
                featureVectors = (List<FeatureVector>) ois.readObject();
                ois.close();
                fis.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }

        public void saveFeatureVectors(List<FeatureVector> featureVectors) {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(filePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(featureVectors);
                oos.flush();
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return filePath;
        }
    }

    class FeatureVector implements Serializable {
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

        public double score(ParameterVector parameterVector) {
            return parameterVector.dotProduct(this);
        }

        @Override
        public String toString() {
            return "FeatureCrosses: " + featureCrosses;
        }
    }

    class ParameterVector {
        public double gamma = 0.0;
        private int arraySize = 0;
        private double[] parameterArray;

        public ParameterVector(FeatureOption featureOption) {
            switch (featureOption) {
                case EXTENDED:
                    this.arraySize = ExtendedFeature.FEATURE_SIZE;
                    break;
                case ORIGINAL:
                    this.arraySize = OriginalFeature.FEATURE_SIZE;
                    break;
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
            parameterArray[index] += value;
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
            double res = 0.0;
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
                FileUtils.writeStringToFile(vectorFile, stringJoiner.toString(), Charset.defaultCharset(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    enum DiffType {
        DeleteType,
        InsertType,
        ReplaceType,
        UnknownType,
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
        public CtElement oldRExpr, newRExpr; // only for ReplaceType

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
    }
}
