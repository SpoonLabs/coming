package prophet4j.port;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import prophet4j.meta.FeatureStruct;
import prophet4j.util.CodeDiffer;
import prophet4j.util.FeatureLearner;

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

    // 2019-03-04 18:44:13 INFO  FeatureLearner:94 - BestGamma 0.45767766122898
    // 2019-03-06 01:26:31 INFO  FeatureLearner:95 - BestGamma 0.3954791310028565
    // 2019-03-06 07:14:37 INFO  FeatureLearner:88 - Update best gamma 0.5626711274648177
    // 2019-03-06 10:55:03 INFO  FeatureLearner:96 - BestGamma 0.31852054721306666
    // the following results are the firstly updated bestGamma (expecting 0.05~0.10 lower)
    // 2019-03-06 10:59:07 INFO  FeatureLearner:88 - 0 Update best gamma 0.6626388805028863
    // 2019-03-06 10:59:59 INFO  FeatureLearner:88 - 0 Update best gamma 0.6391350385846465
    // 2019-03-06 11:00:45 INFO  FeatureLearner:88 - 0 Update best gamma 0.47811933180546606
    // 2019-03-06 11:01:30 INFO  FeatureLearner:88 - 0 Update best gamma 0.5410770001973683
    // 2019-03-06 11:02:28 INFO  FeatureLearner:88 - 0 Update best gamma 0.6162740922033824
    // 2019-03-06 11:03:19 INFO  FeatureLearner:88 - 0 Update best gamma 0.7088177119898706
    // 2019-03-06 11:04:04 INFO  FeatureLearner:88 - 0 Update best gamma 0.5480431436130994
    // 2019-03-06 11:04:48 INFO  FeatureLearner:88 - 0 Update best gamma 0.4968135677167363
    // 2019-03-06 11:05:37 INFO  FeatureLearner:88 - 0 Update best gamma 0.5119353626311811
    // 2019-03-06 11:06:19 INFO  FeatureLearner:88 - 0 Update best gamma 0.5074681134536615
    // 2019-03-06 11:07:07 INFO  FeatureLearner:88 - 0 Update best gamma 0.5133541278650269
    // 2019-03-06 11:34:47 INFO  FeatureLearner:88 - 0 Update best gamma 0.4068330824438837
    // 2019-03-06 12:03:20 INFO  FeatureLearner:88 - 0 Update best gamma 0.5114584373196781
    // 2019-03-06 12:34:36 INFO  FeatureLearner:88 - 0 Update best gamma 0.6248328044658776
    // 2019-03-06 12:49:22 INFO  FeatureLearner:88 - 0 Update best gamma 0.3851005199855352
    // 2019-03-06 12:54:11 INFO  FeatureLearner:88 - 0 Update best gamma 0.5495353795626376
    // 2019-03-06 22:09:43 INFO  FeatureLearner:88 - 0 Update best gamma 0.5184518256024112
    // human patches: program-repair/defects4j-dissection
    // generated patches: kth-tcs/overfitting-analysis(/data/Training/patched_cardumen/)
    // old files && human patches && generated patches
    public void handleData() throws NullPointerException {
        List<String> filePaths = new ArrayList<>();
        CodeDiffer codeDiffer = new CodeDiffer(true);
        Map<String, Map<File, List<File>>> catalogs = loadCardumenData();
        int progressAll = catalogs.size(), progressNow = 0;
        for (String pathName : catalogs.keySet()) {
            Map<File, List<File>> catalog = catalogs.get(pathName);
            for (File oldFile : catalog.keySet()) {
                try {
                    String vectorFilePath = CARDUMEN_VECTORS_DIR + pathName + "/" + oldFile.getName();
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
        new FeatureLearner().func4Demo(filePaths, CARDUMEN_PARAMETERS_DIR + "PV");
    }

    // generated patches: kth-tcs/overfitting-analysis(/data/Training/patched_cardumen/)
    public void generateCSV() throws NullPointerException {
        // if only one large csv file is needed, we could just combine all generated ones
        CodeDiffer codeDiffer = new CodeDiffer(false);
        Map<String, Map<File, List<File>>> catalogs = loadCardumenData();
        for (String pathName : catalogs.keySet()) {
            Map<String, List<FeatureStruct.FeatureManager>> metadata = new LinkedHashMap<>();
            Map<File, List<File>> catalog = catalogs.get(pathName);
            int progressAll = catalog.size(), progressNow = 0;
            String csvFileName = CARDUMEN_CSV_DIR + pathName + ".csv";
            File csvFile = new File(csvFileName);
            if (!csvFile.exists()) {
                for (File oldFile : catalog.keySet()) {
                    for (File newFile : catalog.get(oldFile)) {
                        try {
                            String buggyFileName = oldFile.getName(); // xxx.java
                            String patchedFileName = newFile.getParentFile().getName(); // patchX
                            String entryName = pathName + "-" + buggyFileName + "-" + patchedFileName;
                            System.out.print(entryName);
                            List<FeatureStruct.FeatureManager> featureManagers = codeDiffer.func4Demo(oldFile, newFile);
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
                // generate csv file
                FeatureStruct.generateCSV(csvFileName, metadata);
                System.out.println("csv generated");
            } else {
                System.out.println("csv existed");
            }
        }
    }
}
