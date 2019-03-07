package prophet4j.port;

import prophet4j.meta.FeatureStruct;
import prophet4j.util.CodeDiffer;
import prophet4j.util.FeatureLearner;

import java.io.File;
import java.util.*;

public class SANER {
    private final String PROPHET4J_DIR = "src/main/resources/prophet4j/";
    private final String SANER_DATA_DIR = PROPHET4J_DIR + "saner_data/";
    private final String SANER_VECTORS_DIR = PROPHET4J_DIR + "saner_vectors/";
    private final String SANER_PARAMETERS_DIR = PROPHET4J_DIR + "saner_parameters/";

    private Map<String, Map<File, File>> loadSANERData() throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        for (File typeFile : new File(SANER_DATA_DIR).listFiles((dir, name) -> !name.startsWith("."))) {
            File[] targetDirs = typeFile.listFiles((dir, name) -> name.equals("modifiedFiles"));
            if (targetDirs!=null && targetDirs.length > 0) {
                for (File numFile : targetDirs[0].listFiles((dir, name) -> !name.startsWith("."))) {
                    String pathName = typeFile.getName() + numFile.getName();
                    if (!catalogs.containsKey(pathName)) {
                        catalogs.put(pathName, new LinkedHashMap<>());
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
    // old files and human patches
    public void handleData() throws NullPointerException {
        List<String> filePaths = new ArrayList<>();
        CodeDiffer codeDiffer = new CodeDiffer(true);
        Map<String, Map<File, File>> catalogs = loadSANERData();
        int progressAll = catalogs.size(), progressNow = 0;
        for (String pathName : catalogs.keySet()) {
            Map<File, File> catalog = catalogs.get(pathName);
            for (File oldFile : catalog.keySet()) {
                try {
                    String vectorFilePath = SANER_VECTORS_DIR + pathName + "/" + oldFile.getName();
                    System.out.println(vectorFilePath);
                    File vectorFile = new File(vectorFilePath);
                    if (!vectorFile.exists()) {
                        List<FeatureStruct.FeatureManager> featureManagers = codeDiffer.func4Demo(oldFile, catalog.get(oldFile));
                        if (featureManagers.size() == 0) {
                            // diff.commonAncestor() returns null value
                            continue;
                        }
                        FeatureStruct.save(vectorFile, featureManagers);
                    }
                    filePaths.add(vectorFilePath);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            progressNow += 1;
            System.out.println(pathName + " : " + progressNow + " / " + progressAll);
        }
        new FeatureLearner().func4Demo(filePaths, SANER_PARAMETERS_DIR + "PV");
    }
}
