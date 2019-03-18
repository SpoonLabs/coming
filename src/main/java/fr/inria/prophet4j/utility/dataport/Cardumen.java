package fr.inria.prophet4j.utility.dataport;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import fr.inria.prophet4j.utility.dataport.util.Helper;
import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.Sample;
import fr.inria.prophet4j.defined.CodeDiffer;
import fr.inria.prophet4j.defined.FeatureLearner;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

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
                    catalogs.put(pathName, new HashMap<>());
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

    // human patches: program-repair/defects4j-dissection
    // generated patches: kth-tcs/overfitting-analysis(/dataport/Training/patched_cardumen/)
    // buggy files & human patches & generated patches are given
    public void handleData(FeatureOption featureOption) throws NullPointerException {
        List<String> filePaths = new ArrayList<>();
        String binFilePath = CARDUMEN_VECTORS_DIR + featureOption.toString() + "/" + "serial.bin";
        if (new File(binFilePath).exists()) {
            filePaths = Helper.deserialize(binFilePath);
        } else {
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
                            List<FeatureVector> featureVectors = codeDiffer.func4Cardumen(oldFile, catalog.get(oldFile));
                            if (featureVectors.size() == 0) {
                                // diff.commonAncestor() returns null value
                                continue;
                            }
                            new Sample(vectorFile.getPath()).saveFeatureVectors(featureVectors);
                        }
                        filePaths.add(vectorFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressNow += 1;
                System.out.println(pathName + " : " + progressNow + " / " + progressAll);
            }
            Helper.serialize(binFilePath, filePaths);
        }
        new FeatureLearner(featureOption).func4Demo(filePaths, CARDUMEN_PARAMETERS_DIR  + featureOption.toString() + "/" + "ParameterVector");
    }

    // by the way, feature types are reflected in prophet's way, which seems stricter
    // generated patches: kth-tcs/overfitting-analysis(/dataport/Training/patched_cardumen/)
    public void generateCSV(FeatureOption featureOption) throws NullPointerException {
        // if only one large csv file is needed, we could just combine all generated ones
        CodeDiffer codeDiffer = new CodeDiffer(false, featureOption);
        Map<String, Map<File, List<File>>> catalogs = loadCardumenData();
        for (String pathName : catalogs.keySet()) {
            Map<String, List<FeatureVector>> metadata = new HashMap<>();
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
                            List<FeatureVector> featureVectors = codeDiffer.func4Demo(oldFile, newFile);
                            if (featureVectors.size() == 0) {
                                // diff.commonAncestor() returns null value
                                System.out.println("patched file in patched_cardumen/ does not match patch file in cardumen/");
                            }
                            metadata.put(entryName, featureVectors);
                            System.out.println("\tokay");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    progressNow += 1;
                    System.out.println(pathName + " : " + progressNow + " / " + progressAll);
                }
                // saveFeatureVectors as csv file
                Helper.dumpCSV(csvFileName, metadata);
                System.out.println("csv generated");
            } else {
                System.out.println("csv existed");
            }
        }
    }
}
