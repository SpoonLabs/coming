package fr.inria.prophet4j.defined;

import fr.inria.prophet4j.defined.extended.ExtendedFeature;
import fr.inria.prophet4j.defined.extended.ExtendedFeatureCross;
import fr.inria.prophet4j.defined.original.OriginalFeature;
import fr.inria.prophet4j.defined.original.OriginalFeatureCross;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public interface Structure {
    enum FeatureOption {
        EXTENDED,
        ORIGINAL,
    }

    class Sample { // namely TrainingCase
        private String filePath;
        private FeatureOption featureOption;

        public Sample(String filePath) {
            this.filePath = filePath;
            this.featureOption = null;
        }

        public Sample(String filePath, FeatureOption featureOption) {
            this.filePath = filePath;
            this.featureOption = featureOption;
        }

        public List<FeatureVector> loadFeatureVectors() {
            try {
                // the first one is for human patch, others are for candidate patches
                File vectorFile = new File(filePath);
                List<FeatureVector> featureVectors = new ArrayList<>();
                String string = FileUtils.readFileToString(vectorFile, Charset.defaultCharset());
                StringTokenizer stringTokenizer = new StringTokenizer(string, "\n");
                while (stringTokenizer.hasMoreTokens()) {
                    featureVectors.add(new FeatureVector(stringTokenizer.nextToken(), featureOption));
                }
                // we deduplicate FeatureVectors as they should have no positive effect
                // we make sure the first one is for human patch by using LinkedHashSet
                Set<FeatureVector> linkedHashSet = new LinkedHashSet<>(featureVectors.size());
                linkedHashSet.addAll(featureVectors);
                featureVectors.clear();
                featureVectors.addAll(linkedHashSet);
                // todo: I need to ensure featureVectors.size() always larger than 1
                return featureVectors;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return new ArrayList<>();
        }

        public void saveFeatureVectors(List<FeatureVector> featureVectors) {
            try {
                File vectorFile = new File(filePath);
                StringJoiner stringJoiner = new StringJoiner("\n");
                for (FeatureVector featureVector : featureVectors) {
                    StringJoiner subStringJoiner = new StringJoiner(" ");
                    for (int featureCrossId : featureVector.featureArray) {
                        subStringJoiner.add(String.valueOf(featureCrossId));
                    }
                    stringJoiner.add(subStringJoiner.toString());
                }
                FileUtils.writeStringToFile(vectorFile, stringJoiner.toString(), Charset.defaultCharset(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class FeatureVector {
        private int arraySize = 0;
        private int[] featureArray;
        private Set<FeatureCross> featureCrosses;

        public FeatureVector(FeatureOption featureOption) {
            switch (featureOption) {
                case EXTENDED:
                    this.arraySize = ExtendedFeature.FEATURE_SIZE;
                    break;
                case ORIGINAL:
                    this.arraySize = OriginalFeature.FEATURE_SIZE;
                    break;
            }
            this.featureArray = new int[this.arraySize];
            this.featureCrosses = new HashSet<>();
        }

        public FeatureVector(String string, FeatureOption featureOption) {
            this(featureOption);
            String[] substrings = string.split(" ");
            assert substrings.length == this.arraySize;
            for (int i = 0; i < this.arraySize; i++) {
                int value = Integer.valueOf(substrings[i]);
                this.featureArray[i] = value;
                if (value == 1) {
                    switch (featureOption) {
                        case EXTENDED:
                            this.featureCrosses.add(new ExtendedFeatureCross(i));
                            break;
                        case ORIGINAL:
                            this.featureCrosses.add(new OriginalFeatureCross(i));
                            break;
                    }
                }
            }
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
            featureArray[featureCross.getId()] = 1;
            featureCrosses.add(featureCross);
        }

        public Set<FeatureCross> getFeatureCrosses() {
            return featureCrosses;
        }

        public Set<Integer> getFeatureCrossIds() {
            Set<Integer> featureCrossIds = new HashSet<>();
            for (FeatureCross featureCross : getFeatureCrosses()) {
                featureCrossIds.add(featureCross.getId());
            }
            return featureCrossIds;
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

        public void set(int index, double value) {
            parameterArray[index] = value;
        }

        public void clone(ParameterVector parameterVector) {
            this.arraySize = parameterVector.arraySize;
            this.parameterArray = parameterVector.parameterArray.clone();
        }

        public double dotProduct(FeatureVector featureVector) {
            double res = 0;
            for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
                res += parameterArray[featureCross.getId()];
            }
            return res;
        }

        public void load(File vectorFile) {
            try {
                List<Double> parameterList = new ArrayList<>();
                String string = FileUtils.readFileToString(vectorFile, Charset.defaultCharset());
                StringTokenizer stringTokenizer = new StringTokenizer(string, " ");
                while (stringTokenizer.hasMoreTokens()) {
                    parameterList.add(Double.valueOf(stringTokenizer.nextToken()));
                }
                parameterArray = parameterList.stream().mapToDouble(Double::doubleValue).toArray();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void save(File vectorFile) {
            try {
                StringJoiner stringJoiner = new StringJoiner(" ");
                for (double parameter : parameterArray) {
                    stringJoiner.add(String.valueOf(parameter));
                }
                FileUtils.writeStringToFile(vectorFile, stringJoiner.toString(), Charset.defaultCharset(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
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
