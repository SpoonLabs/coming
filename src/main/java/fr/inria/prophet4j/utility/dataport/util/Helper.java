package fr.inria.prophet4j.utility.dataport.util;

import fr.inria.prophet4j.defined.Feature;
import fr.inria.prophet4j.defined.FeatureCross;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.original.OriginalFeature.AtomicFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature.RepairFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature.ValueFeature;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Helper {
    // right now it is only needed by Original Features todo improve
    public static void dumpCSV(String csvFileName, Map<String, List<FeatureVector>> metadata) {
        List<String> header = new ArrayList<>();
        AtomicFeature[] atomicFeatures = AtomicFeature.values();
        RepairFeature[] repairFeatures = RepairFeature.values();
        ValueFeature[] valueFeatures = ValueFeature.values();
        header.add("entryName");
        header.addAll(Arrays.stream(atomicFeatures).map(AtomicFeature::name).collect(Collectors.toList()));
        header.addAll(Arrays.stream(repairFeatures).map(RepairFeature::name).collect(Collectors.toList()));
        header.addAll(Arrays.stream(valueFeatures).map(ValueFeature::name).collect(Collectors.toList()));
        try {
            BufferedWriter writer = java.nio.file.Files.newBufferedWriter(Paths.get(csvFileName));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header.toArray(new String[0])));
            List<String> keyList = new ArrayList<>(metadata.keySet());
            keyList.sort(String::compareTo);
            for (String key : keyList) {
                List<String> entry = new ArrayList<>();
                Set<FeatureCross> overallFeatureCrosses = new HashSet<>();
                for (FeatureVector featureVector : metadata.get(key)) {
                    overallFeatureCrosses.addAll(featureVector.getFeatureCrosses());
                }
                Set<Feature> featureSet = new HashSet<>();
                for (FeatureCross featureCross : overallFeatureCrosses) {
                    featureSet.addAll(featureCross.getFeatures());
                }
                List<String> featureList = new ArrayList<>();
                for (AtomicFeature atomicFeature : atomicFeatures) {
                    featureList.add(featureSet.contains(atomicFeature) ? "1" : "0");
                }
                for (RepairFeature repairFeature : repairFeatures) {
                    featureList.add(featureSet.contains(repairFeature) ? "1" : "0");
                }
                for (ValueFeature valueFeature : valueFeatures) {
                    featureList.add(featureSet.contains(valueFeature) ? "1" : "0");
                }
                entry.add(key);
                entry.addAll(featureList);
                csvPrinter.printRecord(entry);
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> deserialize(String filePath) {
        List<String> strings = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            strings = (List<String>) ois.readObject();
            ois.close();
            fis.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    public static void serialize(String filePath, List<String> strings) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(strings);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
