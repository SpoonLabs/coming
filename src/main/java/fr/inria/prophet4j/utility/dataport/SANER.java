package fr.inria.prophet4j.utility.dataport;

import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.Sample;
import fr.inria.prophet4j.defined.CodeDiffer;
import fr.inria.prophet4j.defined.FeatureLearner;
import fr.inria.prophet4j.utility.dataport.util.Helper;

import java.io.File;
import java.util.*;

public class SANER {
    private final String PROPHET4J_DIR = "src/main/resources/prophet4j/";
    private final String SANER_DATA_DIR = PROPHET4J_DIR + "saner_data/";
    private final String SANER_VECTORS_DIR = PROPHET4J_DIR + "saner_vectors/";
    private final String SANER_PARAMETERS_DIR = PROPHET4J_DIR + "saner_parameters/";
    // avoid handling some cases with diff issues
    private List<String> blacklist4VectorFilePath = new ArrayList<>();

    public SANER() {
        blacklist4VectorFilePath.add("ignatov_intellij-erlang10/ErlangFormattingModelBuilder.java");
        blacklist4VectorFilePath.add("ignatov_intellij-erlang14/ErlangFormattingModelBuilder.java");
        blacklist4VectorFilePath.add("JetBrains_kotlin28/JetFormattingModelBuilder.java");
        blacklist4VectorFilePath.add("JetBrains_kotlin14/JetFormattingModelBuilder.java");
    }

    private Map<String, Map<File, File>> loadSANERData() throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        for (File typeFile : new File(SANER_DATA_DIR).listFiles((dir, name) -> !name.startsWith("."))) {
            File[] targetDirs = typeFile.listFiles((dir, name) -> name.equals("modifiedFiles"));
            if (targetDirs != null && targetDirs.length > 0) {
                for (File numFile : targetDirs[0].listFiles((dir, name) -> !name.startsWith("."))) {
                    String pathName = typeFile.getName() + numFile.getName();
                    if (!catalogs.containsKey(pathName)) {
                        catalogs.put(pathName, new HashMap<>());
                    }
                    Map<File, File> catalog = catalogs.get(pathName);
                    List<File> oldFiles = new ArrayList<>();
                    List<File> fixFiles = new ArrayList<>();
                    for (File dataFile : numFile.listFiles((dir, name) -> !name.startsWith("."))) {
                        if (dataFile.getName().equals("old")) {
                            oldFiles.addAll(Arrays.asList(dataFile.listFiles((dir, name) -> name.endsWith(".java"))));
                        } else if (dataFile.getName().equals("fix")) {
                            fixFiles.addAll(Arrays.asList(dataFile.listFiles((dir, name) -> name.endsWith(".java"))));
                        }
                    }
                    for (File key : oldFiles) {
                        String keyName = key.getName();
                        assert !catalog.containsKey(key);
                        for (File value : fixFiles) {
                            String valueName = value.getName();
                            if (keyName.equals(valueName)) {
                                catalog.put(key, value);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return catalogs;
    }

    // human patches: https://github.com/monperrus/bug-fixes-saner16
    // buggy files & human patches are given
    public void handleData(FeatureOption featureOption) throws NullPointerException {
        List<String> filePaths = new ArrayList<>();
        String binFilePath = SANER_VECTORS_DIR + featureOption.toString() + "/" + "serial.bin";
        if (new File(binFilePath).exists()) {
            filePaths = Helper.deserialize(binFilePath);
        } else {
            CodeDiffer codeDiffer = new CodeDiffer(true, featureOption);
            Map<String, Map<File, File>> catalogs = loadSANERData();
            int progressAll = catalogs.size(), progressNow = 0;
            for (String pathName : catalogs.keySet()) {
                Map<File, File> catalog = catalogs.get(pathName);
                for (File oldFile : catalog.keySet()) {
                    try {
                        String vectorFilePath = pathName + "/" + oldFile.getName();
                        System.out.println(vectorFilePath);
                        if (blacklist4VectorFilePath.contains(vectorFilePath)) {
                            progressNow += 1;
                            System.out.println("blacklist");
                            continue;
                        }
                        File vectorFile = new File(SANER_VECTORS_DIR + featureOption.toString() + "/" + vectorFilePath);
                        if (!vectorFile.exists()) {
                            List<FeatureVector> featureVectors = codeDiffer.func4Demo(oldFile, catalog.get(oldFile));
                            if (featureVectors.size() == 0) {
                                // diff.commonAncestor() returns null value
                                continue;
                            }
                            new Sample(vectorFile.getPath()).saveFeatureVectors(featureVectors);
                        }
                        filePaths.add(SANER_VECTORS_DIR + featureOption.toString() + "/" + vectorFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressNow += 1;
                System.out.println(pathName + " : " + progressNow + " / " + progressAll);
            }
            Helper.serialize(binFilePath, filePaths);
        }
        new FeatureLearner(featureOption).func4Demo(filePaths, SANER_PARAMETERS_DIR + featureOption.toString() + "/" + "ParameterVector");
    }
}
