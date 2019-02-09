package prophet4j.defined;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import prophet4j.defined.FeatureType.*;
import spoon.reflect.code.CtStatement;

import java.util.*;

public interface FeatureStruct {
    // it seems weird to place load() and save() here
    static void save(File vectorFile, List<FeatureVector> featureVectors) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (FeatureVector featureVector : featureVectors) {
            for (int fid : featureVector.featureIds) {
                sb.append(fid);
                sb.append(" ");
            }
            sb.append(featureVector.mark);
            sb.append("\n");
        }
        FileUtils.writeStringToFile(vectorFile, sb.toString(), Charset.defaultCharset(), true);
    }

    static List<FeatureVector> load(File vectorFile) throws IOException {
        List<FeatureVector> featureVectors = new ArrayList<>();
        String string = FileUtils.readFileToString(vectorFile, Charset.defaultCharset());
        String[] substrings = string.split("\n");
        for (String substring : substrings) {
            featureVectors.add(new FeatureVector(substring));
        }
        return featureVectors;
    }

    class FeatureManager {

        private Set<Feature> features = new HashSet<>();

        public void addFeature(Feature feature) {
            features.add(feature);
        }

        public boolean containFeatureType(FeatureType featureType) {
            for (Feature feature : features) {
                System.out.println(feature);
                if (feature.containFeatureType(featureType)) {
                    return true;
                }
            }
            return false;
        }

        public FeatureVector getFeatureVector() {
            return new FeatureVector(features);
        }

        @Override
        public String toString() {
            return "Context " + features + "\n";
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

        Integer getFeatureId() {
            return featureId;
        }

        public boolean containFeatureType(FeatureType featureType) {
            return featureTypes.contains(featureType);
        }

        @Override
        public String toString() {
            return "Feature " + featureTypes + "\n";
        }
    }

    class FeatureVector {

        private boolean mark = false;
        private Set<Feature> features = new HashSet<>();
        int[] featureIds = new int[FeatureType.FEATURE_SIZE];

        public FeatureVector() {
        }

        public FeatureVector(String string) {
            String[] substrings = string.split(" ");
            assert substrings.length == FeatureType.FEATURE_SIZE + 1;
            for (int i = 0; i < FeatureType.FEATURE_SIZE; i++) {
                featureIds[i] = Integer.valueOf(substrings[i]);
            }
            mark = Boolean.valueOf(substrings[FeatureType.FEATURE_SIZE]);
        }

        FeatureVector(Set<Feature> features) {
            this.features = features;
            for (Feature feature : features) {
                featureIds[feature.getFeatureId()] = 1; // ? todo: compare with FeatureVector.h
            }
        }

        public Set<Feature> getFeatures() {
            // valid only constructed by FeatureVector(Set<Feature> features)
            return features;
        }

        public int size() {
            return featureIds.length;
        }

        public int get(int index) {
            return featureIds[index];
        }

        // just follow prophet4c
        public void setMark() {
            mark = true;
        }

        public void setMark(boolean value) {
            mark = value;
        }

        public boolean getMark() {
            return mark;
        }

        public void set(int index, int value) {
            featureIds[index] = value;
        }

        public void clone(FeatureVector featureVector) {
            this.featureIds = featureVector.featureIds.clone();
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
            for (int i = 0; i < featureVector.featureIds.length; i++)
                res += parameterArray[featureVector.featureIds[i]];
            return res;
        }
    }

    class TrainingCase {
        public List<FeatureVector> cases = new ArrayList<>();
        public List<Integer> marked = new ArrayList<>();
    }

    class ValueToFeatureMapTy { // ValueToFeatureMapTy
        public Map<String, Set<AtomicFeature>> map = new HashMap<>();
    }

    class Cache { // Cache
        public Map<CtStatement, ValueToFeatureMapTy> map = new HashMap<>();
    }
}
