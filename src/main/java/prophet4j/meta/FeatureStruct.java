package prophet4j.meta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import prophet4j.meta.FeatureType.AtomicFeature;
import prophet4j.meta.FeatureType.JointType;
import prophet4j.meta.FeatureType.Position;
import prophet4j.meta.FeatureType.RepairFeature;
import prophet4j.meta.FeatureType.ValueFeature;

public interface FeatureStruct {
    // the first one is for human patch, others are for candidate patches
    static List<FeatureVector> load(File vectorFile) throws IOException {
        List<FeatureVector> featureVectors = new ArrayList<>();
        String string = FileUtils.readFileToString(vectorFile, Charset.defaultCharset());
        StringTokenizer stringTokenizer = new StringTokenizer(string, "\n");
        while (stringTokenizer.hasMoreTokens()) {
            featureVectors.add(new FeatureVector(stringTokenizer.nextToken()));
        }
        // we deduplicate FeatureVectors as they should have no positive effect
        // we make sure the first one is for human patch by using LinkedHashSet
        Set<FeatureVector> linkedHashSet = new LinkedHashSet<>(featureVectors.size());
        linkedHashSet.addAll(featureVectors);
        featureVectors.clear();
        featureVectors.addAll(linkedHashSet);
        // todo: I need to ensure featureVectors.size() always larger than 1
        return featureVectors;
    }

    static void save(File vectorFile, List<FeatureManager> featureManagers) throws IOException {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (FeatureManager featureManager : featureManagers) {
            StringJoiner subStringJoiner = new StringJoiner(" ");
            FeatureVector featureVector = featureManager.getFeatureVector();
            for (int featureId : featureVector.featureArray) {
                subStringJoiner.add(String.valueOf(featureId));
            }
            stringJoiner.add(subStringJoiner.toString());
        }
        FileUtils.writeStringToFile(vectorFile, stringJoiner.toString(), Charset.defaultCharset(), true);
    }

    static void generateCSV(String csvFileName, Map<String, List<FeatureManager>> metadata) {
        List<String> header = new ArrayList<>();
        AtomicFeature[] atomicFeatures = AtomicFeature.values();
        RepairFeature[] repairFeatures = RepairFeature.values();
        ValueFeature[] valueFeatures = ValueFeature.values();
        header.add("entryName");
        header.addAll(Arrays.stream(atomicFeatures).map(AtomicFeature::name).collect(Collectors.toList()));
        header.addAll(Arrays.stream(repairFeatures).map(RepairFeature::name).collect(Collectors.toList()));
        header.addAll(Arrays.stream(valueFeatures).map(ValueFeature::name).collect(Collectors.toList()));
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFileName));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header.toArray(new String[0])));
            for (String key : metadata.keySet()) {
                List<String> entry = new ArrayList<>();
                Set<Feature> overallFeatureSet = new HashSet<>();
                for (FeatureManager featureManager : metadata.get(key)) {
                    overallFeatureSet.addAll(featureManager.featureSet);
                }
                Set<FeatureType> featureTypeSet = new HashSet<>();
                for (Feature feature : overallFeatureSet) {
                    featureTypeSet.addAll(feature.getFeatureTypes());
                }
                List<String> featureTypeVector = new ArrayList<>();
                for (AtomicFeature atomicFeature : atomicFeatures) {
                    featureTypeVector.add(featureTypeSet.contains(atomicFeature) ? "1" : "0");
                }
                for (RepairFeature repairFeature : repairFeatures) {
                    featureTypeVector.add(featureTypeSet.contains(repairFeature) ? "1" : "0");
                }
                for (ValueFeature valueFeature : valueFeatures) {
                    featureTypeVector.add(featureTypeSet.contains(valueFeature) ? "1" : "0");
                }
                entry.add(key);
                entry.addAll(featureTypeVector);
                csvPrinter.printRecord(entry);
            }
            csvPrinter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    class FeatureManager {

        private Set<Feature> featureSet = new HashSet<>();

        public void addFeature(Feature feature) {
            featureSet.add(feature);
        }

        public boolean containFeatureType(FeatureType featureType) {
            for (Feature feature : featureSet) {
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

        public Feature(Integer featureId) {
            this.featureId = featureId;
            this.featureTypes = new ArrayList<>();
            if (featureId >= FeatureType.FEATURE_BASE_3) {
                int tmp = featureId - FeatureType.FEATURE_BASE_3;
                int ordinal0 = tmp / FeatureType.VF_SIZE;
                int ordinal1 = tmp % FeatureType.VF_SIZE;
                featureTypes.add(AtomicFeature.values()[ordinal0]);
                featureTypes.add(ValueFeature.values()[ordinal1]);
            } else if (featureId >= FeatureType.FEATURE_BASE_2) {
                int tmp = featureId - FeatureType.FEATURE_BASE_2;
                int ordinal0 = tmp / (FeatureType.AF_SIZE * FeatureType.AF_SIZE);
                int ordinal1 = (tmp % (FeatureType.AF_SIZE * FeatureType.AF_SIZE)) / FeatureType.AF_SIZE;
                int ordinal2 = (tmp % (FeatureType.AF_SIZE * FeatureType.AF_SIZE)) % FeatureType.AF_SIZE;
                featureTypes.add(Position.values()[ordinal0]);
                featureTypes.add(AtomicFeature.values()[ordinal1]);
                featureTypes.add(AtomicFeature.values()[ordinal2]);
            } else if (featureId >= FeatureType.FEATURE_BASE_1) {
                int tmp = featureId - FeatureType.FEATURE_BASE_1;
                int ordinal0 = tmp / (FeatureType.AF_SIZE * FeatureType.RF_SIZE);
                int ordinal1 = (tmp % (FeatureType.AF_SIZE * FeatureType.RF_SIZE)) / FeatureType.RF_SIZE;
                int ordinal2 = (tmp % (FeatureType.AF_SIZE * FeatureType.RF_SIZE)) % FeatureType.RF_SIZE;
                featureTypes.add(Position.values()[ordinal0]);
                featureTypes.add(AtomicFeature.values()[ordinal1]);
                featureTypes.add(RepairFeature.values()[ordinal2]);
            } else if (featureId >= FeatureType.FEATURE_BASE_0) {
                int ordinal0 = featureId - FeatureType.FEATURE_BASE_0;
                featureTypes.add(RepairFeature.values()[ordinal0]);
            }
        }

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

        public List<FeatureType> getFeatureTypes() {
            return featureTypes;
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

        FeatureVector(String string) {
            String[] substrings = string.split(" ");
            assert substrings.length == FeatureType.FEATURE_SIZE;
            for (int i = 0; i < FeatureType.FEATURE_SIZE; i++) {
                int value = Integer.valueOf(substrings[i]);
                featureArray[i] = value;
                if (value == 1) {
                    featureSet.add(new Feature(i));
                }
            }
        }

        FeatureVector(Set<Feature> featureSet) {
            this.featureSet = featureSet;
            for (Feature feature : featureSet) {
                featureArray[feature.getFeatureId()] = 1;
            }
        }

        public Set<Feature> getFeatures() {
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
        public Sample(String filePath) {
            this.filePath = filePath;
        }
        // avoid java.lang.OutOfMemoryError: GC overhead limit exceeded
        public List<FeatureVector> getFeatureVectors() {
            try {
                return FeatureStruct.load(new File(filePath));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return new ArrayList<>();
        }
    }

    class ValueToFeatureMapTy { // ValueToFeatureMapTy
        public Map<String, Set<AtomicFeature>> map = new HashMap<>();
    }
}
