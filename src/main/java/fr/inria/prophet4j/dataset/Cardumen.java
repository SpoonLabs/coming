package fr.inria.prophet4j.dataset;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import fr.inria.prophet4j.defined.Feature;
import fr.inria.prophet4j.defined.FeatureCross;
import fr.inria.prophet4j.defined.original.OriginalFeature.AtomicFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature.RepairFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature.ValueFeature;
import fr.inria.prophet4j.defined.Structure;
import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.defined.Structure.FeatureManager;
import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.FeatureLearner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Cardumen {
    private final String PROPHET4J_DIR = "src/main/resources/prophet4j/";
    private final String CARDUMEN_DATA_DIR = PROPHET4J_DIR + "cardumen_data/";
    // github.com/program-repair/defects4j-dissection/tree/9d5aeea14e2e2e0c440f8b6970f1b278fc5e2271/projects
    private final String DISSECTION_DATA_DIR = PROPHET4J_DIR + "dissection_data/";
    private final String CARDUMEN_CSV_DIR = PROPHET4J_DIR + "cardumen_csv/";
    private final String CARDUMEN_VECTORS_DIR = PROPHET4J_DIR + "cardumen_vectors/";
    private final String CARDUMEN_PARAMETERS_DIR = PROPHET4J_DIR + "cardumen_parameters/";

    private Map<String, Map<File, List<File>>> loadCardumenData() throws NullPointerException {
        Map<String, Map<File, List<File>>> catalogs = new HashMap<>();
        for (File typeFile : new File(CARDUMEN_DATA_DIR).listFiles((dir, name) -> !name.startsWith("."))) {
            for (File numFile : typeFile.listFiles((dir, name) -> !name.startsWith("."))) {
                String pathName = typeFile.getName() + numFile.getName();
                if (!catalogs.containsKey(pathName)) {
                    catalogs.put(pathName, new LinkedHashMap<>());
                }
                Map<File, List<File>> catalog = catalogs.get(pathName);
                List<File> buggyFiles = new ArrayList<>();
                List<File> patchedFiles = new ArrayList<>();
                for (File dataFile : numFile.listFiles((dir, name) -> !name.startsWith("."))) {
                    if (dataFile.getName().equals("buggy")) {
                        buggyFiles.addAll(Arrays.asList(dataFile.listFiles((dir, name) -> !name.startsWith("."))));
                    } else if (dataFile.getName().equals("patched")) {
                        patchedFiles.addAll(Arrays.asList(dataFile.listFiles((dir, name) -> !name.startsWith("."))));
                    }
                }
                List<File> keys = new ArrayList<>();
                List<File> values = new ArrayList<>();
                for (File buggyFile : buggyFiles) {
                    if (buggyFile.getName().endsWith(".java")) {
                        keys.add(buggyFile);
                    }
                }
                for (File patchedFile : patchedFiles) {
                    FilenameFilter filter = (dir, name) -> name.endsWith(".java");
                    values.addAll(Arrays.asList(patchedFile.listFiles(filter)));
                }
                for (File key : keys) {
                    String keyName = key.getName();
                    assert !catalog.containsKey(key);
                    List<File> patches = new ArrayList<>();
                    // we add human patch at the first place
                    File scopeFile = new File(DISSECTION_DATA_DIR + pathName);
                    for (File file : Lists.newArrayList(Files.fileTraverser().depthFirstPreOrder(scopeFile))) {
                        String fileName = file.getName();
                        if (keyName.equals(fileName)) {
                            patches.add(file);
                        }
                    }
                    assert patches.size() > 0;
                    // the following files are generated patches
                    for (File value : values) {
                        String valueName = value.getName();
                        if (keyName.equals(valueName)) {
                            patches.add(value);
                        }
                    }
                    assert patches.size() > 1;
                    catalog.put(key, patches);
                }
            }
        }
        return catalogs;
    }

    // final result of default case by commenting shuffle
    // 2019-03-07 02:32:18 INFO  FeatureLearner:96 - BestGamma 0.4948037947754156
    // all following results are first-time updated bestGamma (final results are lower)
    // 2019-03-06 23:28:10 INFO  FeatureLearner:88 - 0 Update best gamma 0.6062913611533309
    // 2019-03-06 23:29:19 INFO  FeatureLearner:88 - 0 Update best gamma 0.5773545479809099
    // 2019-03-06 23:30:11 INFO  FeatureLearner:88 - 0 Update best gamma 0.5323678289652912
    // 2019-03-06 23:33:40 INFO  FeatureLearner:88 - 0 Update best gamma 0.5900419626960629
    // 2019-03-06 23:34:30 INFO  FeatureLearner:88 - 0 Update best gamma 0.6530135283634988
    // 2019-03-06 23:39:03 INFO  FeatureLearner:88 - 0 Update best gamma 0.5652875005700051
    // 2019-03-06 23:39:53 INFO  FeatureLearner:88 - 0 Update best gamma 0.5674457622643052
    // 2019-03-06 23:40:43 INFO  FeatureLearner:88 - 0 Update best gamma 0.5651883800236959
    // 2019-03-06 23:41:30 INFO  FeatureLearner:88 - 0 Update best gamma 0.6326805727058656
    // 2019-03-06 23:43:04 INFO  FeatureLearner:88 - 0 Update best gamma 0.4634928238147532
    // human patches: program-repair/defects4j-dissection
    // generated patches: kth-tcs/overfitting-analysis(/dataset/Training/patched_cardumen/)
    // old files && human patches && generated patches
    public void handleData(boolean doShuffle, FeatureOption featureOption) throws NullPointerException {
        List<String> filePaths = new ArrayList<>();
        CodeDiffer codeDiffer = new CodeDiffer(true, featureOption);
        Map<String, Map<File, List<File>>> catalogs = loadCardumenData();
        int progressAll = catalogs.size(), progressNow = 0;
        for (String pathName : catalogs.keySet()) {
            Map<File, List<File>> catalog = catalogs.get(pathName);
            for (File oldFile : catalog.keySet()) {
                try {
                    String vectorFilePath = CARDUMEN_VECTORS_DIR + featureOption.toString() + "/" + pathName + "/" + oldFile.getName();
                    System.out.println(vectorFilePath);
                    File vectorFile = new File(vectorFilePath);
                    if (!vectorFile.exists()) {
                        List<FeatureManager> featureManagers = codeDiffer.func4Demo(oldFile, catalog.get(oldFile));
                        if (featureManagers.size() == 0) {
                            // diff.commonAncestor() returns null value
                            continue;
                        }
                        // todo
                        Structure.save(vectorFile, featureManagers);
                    }
                    filePaths.add(vectorFilePath);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            progressNow += 1;
            System.out.println(pathName + " : " + progressNow + " / " + progressAll);
        }
        new FeatureLearner(doShuffle, featureOption).func4Demo(filePaths, CARDUMEN_PARAMETERS_DIR + "PV");
    }

    // todo
    private void dumpCSV(String csvFileName, Map<String, List<FeatureManager>> metadata) {
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
            for (String key : metadata.keySet()) {
                List<String> entry = new ArrayList<>();
                Set<FeatureCross> overallFeatureCrosses = new HashSet<>();
                for (FeatureManager featureManager : metadata.get(key)) {
                    overallFeatureCrosses.addAll(featureManager.getFeatureCrosses());
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // by the way, feature types are reflected in prophet's way, which seems stricter
    // generated patches: kth-tcs/overfitting-analysis(/dataset/Training/patched_cardumen/)
    public void generateCSV(FeatureOption featureOption) throws NullPointerException {
        // if only one large csv file is needed, we could just combine all generated ones
        CodeDiffer codeDiffer = new CodeDiffer(false, featureOption);
        Map<String, Map<File, List<File>>> catalogs = loadCardumenData();
        for (String pathName : catalogs.keySet()) {
            Map<String, List<FeatureManager>> metadata = new LinkedHashMap<>();
            Map<File, List<File>> catalog = catalogs.get(pathName);
            int progressAll = catalog.size(), progressNow = 0;
            String csvFileName = CARDUMEN_CSV_DIR + featureOption.toString() + "/" + pathName + ".csv";
            File csvFile = new File(csvFileName);
            if (!csvFile.exists()) {
                for (File oldFile : catalog.keySet()) {
                    for (File newFile : catalog.get(oldFile)) {
                        try {
                            String buggyFileName = oldFile.getName(); // xxx.java
                            String patchedFileName = newFile.getParentFile().getName(); // patchX
                            String entryName = pathName + "-" + buggyFileName + "-" + patchedFileName;
                            System.out.print(entryName);
                            List<FeatureManager> featureManagers = codeDiffer.func4Demo(oldFile, newFile);
                            if (featureManagers.size() == 0) {
                                // diff.commonAncestor() returns null value
                                System.out.println("patched file in patched_cardumen/ does not match patch file in cardumen/");
                            }
                            metadata.put(entryName, featureManagers);
                            System.out.println("\tokay");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    progressNow += 1;
                    System.out.println(pathName + " : " + progressNow + " / " + progressAll);
                }
                // save as csv file
                dumpCSV(csvFileName, metadata);
                System.out.println("csv generated");
            } else {
                System.out.println("csv existed");
            }
        }
    }
}
