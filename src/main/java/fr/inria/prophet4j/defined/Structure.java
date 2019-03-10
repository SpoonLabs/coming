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
    enum DiffActionType {
        DeleteAction,
        InsertAction,
        ReplaceAction,
        UnknownAction,
    }

    enum RepairActionKind { // what are their differences?
        ReplaceMutationKind,
        InsertMutationKind,
        InsertAfterMutationKind,
        ExprMutationKind,
    }

    enum RepairCandidateKind { // implementation is at RepairGenerator.java
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

    enum FeatureOption {
        EXTENDED,
        ORIGINAL,
    }

    static void save(File vectorFile, List<FeatureManager> featureManagers) throws IOException {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (FeatureManager featureManager : featureManagers) {
            StringJoiner subStringJoiner = new StringJoiner(" ");
            FeatureVector featureVector = featureManager.getFeatureVector();
            for (int featureCrossId : featureVector.featureArray) {
                subStringJoiner.add(String.valueOf(featureCrossId));
            }
            stringJoiner.add(subStringJoiner.toString());
        }
        FileUtils.writeStringToFile(vectorFile, stringJoiner.toString(), Charset.defaultCharset(), true);
    }

    class FeatureManager {
        private FeatureOption featureOption;
        private Set<FeatureCross> featureCrosses = new HashSet<>();

        public FeatureManager(FeatureOption featureOption) {
            this.featureOption = featureOption;
        }

        public void addFeature(FeatureCross featureCross) {
            featureCrosses.add(featureCross);
        }

        public boolean containFeature(Feature feature) {
            for (FeatureCross featureCross : featureCrosses) {
                if (featureCross.containFeature(feature)) {
                    return true;
                }
            }
            return false;
        }

        public Set<FeatureCross> getFeatureCrosses() {
            return featureCrosses;
        }

        public FeatureVector getFeatureVector() {
            return new FeatureVector(featureCrosses, featureOption);
        }

        @Override
        public String toString() {
            return "Features: " + featureCrosses;
        }
    }

    class FeatureVector {
        int[] featureArray;
        private int vectorSize = 0;
        private Set<FeatureCross> featureCrosses;

        public FeatureVector(String string, FeatureOption featureOption) {
            switch (featureOption) {
                case EXTENDED:
                    this.vectorSize = ExtendedFeature.FEATURE_SIZE;
                    break;
                case ORIGINAL:
                    this.vectorSize = OriginalFeature.FEATURE_SIZE;
                    break;
            }
            this.featureArray = new int[this.vectorSize];
            this.featureCrosses = new HashSet<>();

            String[] substrings = string.split(" ");
            assert substrings.length == this.vectorSize;
            for (int i = 0; i < this.vectorSize; i++) {
                int value = Integer.valueOf(substrings[i]);
                this.featureArray[i] = value;
                if (value == 1) {
                    switch (featureOption) {
                        case EXTENDED:
                            this.featureCrosses.add(new OriginalFeatureCross(i));
                            break;
                        case ORIGINAL:
                            this.featureCrosses.add(new ExtendedFeatureCross(i));
                            break;
                    }

                }
            }
        }

        FeatureVector(Set<FeatureCross> featureCrosses, FeatureOption featureOption) {
            switch (featureOption) {
                case EXTENDED:
                    this.vectorSize = ExtendedFeature.FEATURE_SIZE;
                    break;
                case ORIGINAL:
                    this.vectorSize = OriginalFeature.FEATURE_SIZE;
                    break;
            }
            this.featureArray = new int[this.vectorSize];
            this.featureCrosses = featureCrosses;
            for (FeatureCross featureCross : featureCrosses) {
                this.featureArray[featureCross.getFeatureCrossId()] = 1;
            }
        }

        public Set<FeatureCross> getFeatureCrosses() {
            return featureCrosses;
        }

        public Set<Integer> getFeatureCrossIds() {
            Set<Integer> featureCrossIds = new HashSet<>();
            for (FeatureCross featureCross : getFeatureCrosses()) {
                featureCrossIds.add(featureCross.getFeatureCrossId());
            }
            return featureCrossIds;
        }

        public int size() {
            return vectorSize;
        }

        public int get(int index) {
            return featureArray[index];
        }

        public void set(int index, int value) {
            featureArray[index] = value;
        }

        public void clone(FeatureVector featureVector) {
            this.featureArray = featureVector.featureArray.clone();
        }

        public double score(ParameterVector parameterVector) {
            return parameterVector.dotProduct(this);
        }
    }

    class ParameterVector {

        private int vectorSize = 0;
        private double[] parameterArray;

        public ParameterVector(FeatureOption featureOption) {
            switch (featureOption) {
                case EXTENDED:
                    this.vectorSize = ExtendedFeature.FEATURE_SIZE;
                    break;
                case ORIGINAL:
                    this.vectorSize = OriginalFeature.FEATURE_SIZE;
                    break;
            }
            this.parameterArray = new double[vectorSize];
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
            this.parameterArray = parameterVector.parameterArray.clone();
        }

        public double dotProduct(FeatureVector featureVector) {
            double res = 0;
            for (FeatureCross featureCross : featureVector.getFeatureCrosses()) {
                res += parameterArray[featureCross.getFeatureCrossId()];
            }
            return res;
        }

        public void read(File vectorFile) {
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

        public void write(File vectorFile) {
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

    class Sample { // namely TrainingCase
        private String filePath;
        private FeatureOption featureOption;
        public Sample(String filePath, FeatureOption featureOption) {
            this.filePath = filePath;
            this.featureOption = featureOption;
        }
        // avoid java.lang.OutOfMemoryError: GC overhead limit exceeded
        public List<FeatureVector> getFeatureVectors() {
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
    }

    // todo
    class Value2FeatureMap4Original { // namely ValueToFeatureMapTy
        public Map<String, Set<OriginalFeature.AtomicFeature>> map = new HashMap<>();
    }
    class Value2FeatureMap4Extended { // namely ValueToFeatureMapTy
        public Map<String, Set<ExtendedFeature.AtomicFeature>> map = new HashMap<>();
    }

    class DiffEntry { // DiffResultEntry
        // the reason why CtElement is used here is because clang::Expr isa clang::Stmt
        public DiffActionType type;
        public CtElement srcNode, dstNode;

        public DiffEntry(DiffActionType type, CtElement srcNode, CtElement dstNode) {
            this.type = type;
            this.srcNode = srcNode;
            this.dstNode = dstNode;
        }
    }

    class RepairAction {
        public RepairActionKind kind;
        // loc.stmt from "public ASTLocTy loc;"
        public CtElement srcElem;
        // It is a clang::Stmt or clang::Expr
        // todo: this should just be one placeholder, as replaceExprInCandidate() in CodeRewrite.cpp
        public CtElement dstElem;
        // This will only be used for expr level mutations
        List<CtElement> atoms;

        public RepairAction(RepairActionKind kind, CtElement srcElem, CtElement dstElem) {
            this.kind = kind;
            this.srcElem = srcElem;
            this.dstElem = dstElem;
            this.atoms = new ArrayList<>();
        }

        public RepairAction(CtElement srcElem, CtElement dstElem, List<CtElement> atoms) {
            this.kind = RepairActionKind.ExprMutationKind;
            this.srcElem = srcElem;
            this.dstElem = dstElem;
            this.atoms = atoms;
        }
    }

    class Repair {
        public RepairCandidateKind kind;
        public CtElement oldRExpr, newRExpr; // info for replace only
        public List<RepairAction> actions;

        public Repair() {
            this.kind = null;
            this.oldRExpr = null;
            this.newRExpr = null;
            this.actions = new ArrayList<>();
        }

        public Set<CtElement> getCandidateAtoms() {
            Set<CtElement> ret = new HashSet<>();
            ret.add(null);
            for (RepairAction action: actions) {
                if (action.kind == RepairActionKind.ExprMutationKind) {
                    ret.addAll(action.atoms);
                    return ret;
                }
            }
            return ret;
        }
    }
}
