package prophet4j.defined;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import prophet4j.defined.FeatureType.*;
import spoon.reflect.declaration.CtElement;

import java.util.*;

public interface FeatureStruct {
    // it seems weird to place load() and save() here
    static void save(File vectorFile, List<FeatureVector> featureVectors) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (FeatureVector featureVector: featureVectors) {
            for (int featureId : featureVector.featureArray) {
                sb.append(featureId);
                sb.append(" ");
            }
        }
        FileUtils.writeStringToFile(vectorFile, sb.toString().trim() + "\n", Charset.defaultCharset(), true);
    }
    static List<FeatureVector> load(File vectorFile) throws IOException {
        List<FeatureVector> featureVectors = new ArrayList<>();
        String string = FileUtils.readFileToString(vectorFile, Charset.defaultCharset());
        String[] substrings = string.split("\n");
        for (String substring: substrings) {
            featureVectors.add(new FeatureVector(substring));
        }
        return featureVectors;
    }

    class FeatureManager {

        private Set<Feature> featureSet = new HashSet<>();

        public void addFeature(Feature feature) {
            featureSet.add(feature);
        }

        public boolean containFeatureType(FeatureType featureType) {
            for (Feature feature : featureSet) {
//                System.out.println(feature);
                if (feature.containFeatureType(featureType)) {
                    return true;
                }
            }
            return false;
        }

        public FeatureVector getFeatureVector() {
            return new FeatureVector(featureSet);
        }

        @Override
        public String toString() {
            return "Features: " + featureSet;
        }
    }

    class Feature {

        private Integer featureId;
        private List<FeatureType> featureTypes;

        public Feature(JointType jointType, List<FeatureType> featureTypes) {
            int ordinal0, ordinal1, ordinal2;

            switch (jointType) {
                case RF_JT:
                    assert featureTypes.size() == 1;
                    assert featureTypes.get(0) instanceof RepairFeature;
                    ordinal0 = ((RepairFeature) featureTypes.get(0)).ordinal();
                    featureId = FeatureType.FEATURE_BASE_0 + ordinal0;
                    break;
                case POS_AF_RF_JT:
                    assert featureTypes.size() == 3;
                    assert featureTypes.get(0) instanceof Position;
                    assert featureTypes.get(1) instanceof AtomicFeature;
                    assert featureTypes.get(2) instanceof RepairFeature;
                    ordinal0 = ((Position) featureTypes.get(0)).ordinal();
                    ordinal1 = ((AtomicFeature) featureTypes.get(1)).ordinal();
                    ordinal2 = ((RepairFeature) featureTypes.get(2)).ordinal();
                    featureId = FeatureType.FEATURE_BASE_1 + ordinal0 * FeatureType.AF_SIZE * FeatureType.RF_SIZE + ordinal1 * FeatureType.RF_SIZE + ordinal2;
                    break;
                case POS_AF_AF_JT:
                    assert featureTypes.size() == 3;
                    assert featureTypes.get(0) instanceof Position;
                    assert featureTypes.get(1) instanceof AtomicFeature;
                    assert featureTypes.get(2) instanceof AtomicFeature;
                    ordinal0 = ((Position) featureTypes.get(0)).ordinal();
                    ordinal1 = ((AtomicFeature) featureTypes.get(1)).ordinal();
                    ordinal2 = ((AtomicFeature) featureTypes.get(2)).ordinal();
                    featureId = FeatureType.FEATURE_BASE_2 + ordinal0 * FeatureType.AF_SIZE * FeatureType.AF_SIZE + ordinal1 * FeatureType.AF_SIZE + ordinal2;
                    break;
                case AF_VF_JT:
                    assert featureTypes.size() == 2;
                    assert featureTypes.get(0) instanceof AtomicFeature;
                    assert featureTypes.get(1) instanceof ValueFeature;
                    ordinal0 = ((AtomicFeature) featureTypes.get(0)).ordinal();
                    ordinal1 = ((ValueFeature) featureTypes.get(1)).ordinal();
                    featureId = FeatureType.FEATURE_BASE_3 + ordinal0 * FeatureType.VF_SIZE + ordinal1;
                    break;
            }
            this.featureTypes = featureTypes;
        }

        public Integer getFeatureId() {
            return featureId;
        }

        public boolean containFeatureType(FeatureType featureType) {
            return featureTypes.contains(featureType);
        }

        @Override
        public String toString() {
            return "Feature: " + featureTypes;
        }
    }

    class FeatureVector {

        private Set<Feature> featureSet = new HashSet<>();
        int[] featureArray = new int[FeatureType.FEATURE_SIZE];

        public FeatureVector() {
        }

        public FeatureVector(String string) {
            String[] substrings = string.split(" ");
            assert substrings.length == FeatureType.FEATURE_SIZE;
            for (int i = 0; i < FeatureType.FEATURE_SIZE; i++) {
                featureArray[i] = Integer.valueOf(substrings[i]);
            }
        }

        FeatureVector(Set<Feature> featureSet) {
            this.featureSet = featureSet;
            for (Feature feature : featureSet) {
                featureArray[feature.getFeatureId()] = 1;
            }
        }

        public Set<Feature> getFeatures() {
            // only valid when constructed by FeatureVector(Set<Feature> featureSet)
            return featureSet;
        }

        public int size() {
            return featureArray.length;
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
    }

    class ParameterVector {

        private double[] parameterArray = new double[FeatureType.FEATURE_SIZE];

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
            for (Feature feature : featureVector.getFeatures()) {
                res += parameterArray[feature.featureId];
            }
            return res;
        }
    }

    class Sample { // namely TrainingCase
        // the first one is for human patch, others are for candidate patches
        public List<FeatureVector> featureVectors = new ArrayList<>();
    }

    class ValueToFeatureMapTy { // ValueToFeatureMapTy
        public Map<String, Set<AtomicFeature>> map = new HashMap<>();
    }

    class Cache { // Cache
        public Map<CtElement, ValueToFeatureMapTy> map = new HashMap<>();
    }
}
