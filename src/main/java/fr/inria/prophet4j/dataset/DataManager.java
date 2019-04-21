package fr.inria.prophet4j.dataset;

import fr.inria.prophet4j.defined.CodeDiffer;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.Sample;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Support;
import fr.inria.prophet4j.utility.Support.DirType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private Option option;

    public DataManager(Option option) {
        this.option = option;
    }

    // avoid handling some cases with diff issues
    // return List of VectorFilePath
    private List<String> getBlacklist() {
        List<String> blacklist = new ArrayList<>();
        switch (option.patchOption) {
            case CARDUMEN:
                break;
            case SPR:
                switch (option.dataOption) {
                    case CARDUMEN:
                        break;
                    case PGA:
                        break;
                    case SANER:
                        blacklist.add("ignatov_intellij-erlang10/ErlangFormattingModelBuilder.java");
                        blacklist.add("ignatov_intellij-erlang14/ErlangFormattingModelBuilder.java");
                        blacklist.add("JetBrains_kotlin28/JetFormattingModelBuilder.java");
                        blacklist.add("JetBrains_kotlin14/JetFormattingModelBuilder.java");
                        break;
                }
                break;
        }
        return blacklist;
    }

    private Map<String, Map<File, File>> loadDataWithoutPatches(String dataPath) {
        switch (option.dataOption) {
            case CARDUMEN:
                return DataLoader.loadCardumenWithoutPatches(dataPath, Support.PROPHET4J_DIR + "cardumen_dissection/");
            case PGA:
                break;
            case SANER:
                return DataLoader.loadSANERData(dataPath);
            case BEARS:
            case BUG_DOT_JAR:
            case DEFECTS4J:
            case QUIX_BUGS:
                return DataLoader.loadODSWithoutPatches(dataPath);
        }
        return new HashMap<>();
    }

    private Map<String, Map<File, List<File>>> loadDataWithPatches(String dataPath) {
        switch (option.dataOption) {
            case CARDUMEN:
                return DataLoader.loadCardumenWithPatches(dataPath, Support.PROPHET4J_DIR + "cardumen_dissection/");
            case PGA:
                break;
            case SANER:
                break;
            case BEARS:
            case BUG_DOT_JAR:
            case DEFECTS4J:
            case QUIX_BUGS:
                return DataLoader.loadODSWithPatches(dataPath);
        }
        return new HashMap<>();
    }

    public List<String> func4Demo() {
        if (option.dataOption == DataOption.CARDUMEN && option.patchOption == PatchOption.CARDUMEN ||
                option.dataOption == DataOption.BEARS && option.patchOption == PatchOption.BEARS ||
                option.dataOption == DataOption.BUG_DOT_JAR && option.patchOption == PatchOption.BUG_DOT_JAR ||
                option.dataOption == DataOption.DEFECTS4J && option.patchOption == PatchOption.DEFECTS4J ||
                option.dataOption == DataOption.QUIX_BUGS && option.patchOption == PatchOption.QUIX_BUGS
        ) {
            return handleDataWithoutGenerator();
        } else {
            return handleDataWithGenerator();
        }
    }

    // buggy files & human patches are given
    private List<String> handleDataWithGenerator() {
        String dataPath = Support.getFilePath(DirType.DATA_DIR, option);
        String featurePath = Support.getFilePath(DirType.FEATURE_DIR, option);
        List<String> blackList = getBlacklist();

        List<String> filePaths = new ArrayList<>();
        String binFilePath = featurePath + "serial.bin";
        if (new File(binFilePath).exists()) {
            filePaths = DataHelper.deserialize(binFilePath);
        } else {
            CodeDiffer codeDiffer = new CodeDiffer(true, option);
            Map<String, Map<File, File>> catalogs = loadDataWithoutPatches(dataPath);
            int progressAll = catalogs.size(), progressNow = 0;
            for (String pathName : catalogs.keySet()) {
                Map<File, File> catalog = catalogs.get(pathName);
                for (File oldFile : catalog.keySet()) {
                    try {
                        String vectorPath = pathName + "/" + oldFile.getName();
                        System.out.println(vectorPath);
                        if (blackList.contains(vectorPath)) {
                            progressNow += 1;
                            System.out.println("blacklist");
                            continue;
                        }
                        vectorPath = featurePath + vectorPath;
                        File vectorFile = new File(vectorPath);
                        if (!vectorFile.exists()) {
                            List<FeatureVector> featureVectors = codeDiffer.func4Demo(oldFile, catalog.get(oldFile));
                            if (featureVectors.size() == 0) {
                                // diff.commonAncestor() returns null value
                                continue;
                            }
                            new Sample(vectorFile.getPath()).saveFeatureVectors(featureVectors);
                        }
                        filePaths.add(vectorPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressNow += 1;
                System.out.println(pathName + " : " + progressNow + " / " + progressAll);
            }
            DataHelper.serialize(binFilePath, filePaths);
        }
        return filePaths;
    }

    // buggy files & human patches & generated patches are given
    private List<String> handleDataWithoutGenerator() {
        String dataPath = Support.getFilePath(DirType.DATA_DIR, option);
        String featurePath = Support.getFilePath(DirType.FEATURE_DIR, option);
        List<String> blackList = getBlacklist();

        List<String> filePaths = new ArrayList<>();
        String binFilePath = featurePath + "serial.bin";
        if (new File(binFilePath).exists()) {
            filePaths = DataHelper.deserialize(binFilePath);
        } else {
            CodeDiffer codeDiffer = new CodeDiffer(true, option);
            Map<String, Map<File, List<File>>> catalogs = loadDataWithPatches(dataPath);
            int progressAll = catalogs.size(), progressNow = 0;
            for (String pathName : catalogs.keySet()) {
                Map<File, List<File>> catalog = catalogs.get(pathName);
                for (File oldFile : catalog.keySet()) {
                    try {
                        String vectorPath = pathName + "/" + oldFile.getName();
                        System.out.println(vectorPath);
                        if (blackList.contains(vectorPath)) {
                            progressNow += 1;
                            System.out.println("blacklist");
                            continue;
                        }
                        vectorPath = featurePath + vectorPath;
                        File vectorFile = new File(vectorPath);
                        if (!vectorFile.exists()) {
                            List<FeatureVector> featureVectors = codeDiffer.func4Cardumen(oldFile, catalog.get(oldFile));
                            if (featureVectors.size() == 0) {
                                // diff.commonAncestor() returns null value
                                continue;
                            }
                            new Sample(vectorFile.getPath()).saveFeatureVectors(featureVectors);
                        }
                        filePaths.add(vectorPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressNow += 1;
                System.out.println(pathName + " : " + progressNow + " / " + progressAll);
            }
            DataHelper.serialize(binFilePath, filePaths);
        }
        return filePaths;
    }

    // by the way, feature types are reflected in prophet's way, which seems stricter
    // generated patches: kth-tcs/overfitting-analysis(/dataport/Training/patched_cardumen/)
    public void generateCSV(FeatureOption featureOption) throws NullPointerException {
        String dataPath = Support.getFilePath(DirType.DATA_DIR, option);
        String csvPath = Support.getFilePath(DirType.FEATURE_DIR, option);

        // if only one large csv file is needed, we could just combine all generated ones
        CodeDiffer codeDiffer = new CodeDiffer(false, option);
        Map<String, Map<File, List<File>>> catalogs = loadDataWithPatches(dataPath);
        for (String fileName : catalogs.keySet()) {
            Map<String, List<FeatureVector>> metadata = new HashMap<>();
            Map<File, List<File>> catalog = catalogs.get(fileName);
            int progressAll = catalog.size(), progressNow = 0;
            String csvFileName = csvPath + fileName + ".csv";
            File csvFile = new File(csvFileName);
            if (!csvFile.exists()) {
                for (File oldFile : catalog.keySet()) {
                    for (File newFile : catalog.get(oldFile)) {
                        String buggyFileName = oldFile.getName(); // xxx.java
                        String patchedFileName = newFile.getParentFile().getName(); // patchX
                        String entryName = fileName + "-" + buggyFileName + "-" + patchedFileName;
                        System.out.print(entryName);
                        List<FeatureVector> featureVectors = codeDiffer.func4Demo(oldFile, newFile);
                        if (featureVectors.size() == 0) {
                            // diff.commonAncestor() returns null value
                            System.out.println("patched file in patched_cardumen/ does not match patch file in cardumen/");
                        }
                        metadata.put(entryName, featureVectors);
                        System.out.println("\tokay");
                    }
                    progressNow += 1;
                    System.out.println(fileName + " : " + progressNow + " / " + progressAll);
                }
                // saveFeatureVectors as csv file
                DataHelper.dumpCSV(csvFileName, metadata);
                System.out.println("csv generated");
            } else {
                System.out.println("csv existed");
            }
        }
    }
}
