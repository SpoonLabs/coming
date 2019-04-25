package fr.inria.prophet4j.dataset;

import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.Sample;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Support;
import fr.inria.prophet4j.utility.Support.DirType;

import java.io.*;
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

    private Map<String, Map<File, List<File>>> loadDataWithPatches(String dataPath) {
        switch (option.dataOption) {
            case CARDUMEN:
                return DataLoader.loadCardumenWithPatches(dataPath, Support.PROPHET4J_DIR + "cardumen_dissection/");
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

    private Map<String, Map<File, File>> loadDataWithoutPatches(String dataPath) {
        switch (option.dataOption) {
            case CARDUMEN:
                return DataLoader.loadCardumenWithoutPatches(dataPath, Support.PROPHET4J_DIR + "cardumen_dissection/");
            case SANER:
                return DataLoader.loadSANERWithoutPatches(dataPath);
            case BEARS:
            case BUG_DOT_JAR:
            case DEFECTS4J:
            case QUIX_BUGS:
                return DataLoader.loadODSWithoutPatches(dataPath);
        }
        return new HashMap<>();
    }

    public List<String> run() {
        if (option.dataOption == DataOption.CARDUMEN && option.patchOption == PatchOption.CARDUMEN ||
                option.dataOption == DataOption.BEARS && option.patchOption == PatchOption.BEARS ||
                option.dataOption == DataOption.BUG_DOT_JAR && option.patchOption == PatchOption.BUG_DOT_JAR ||
                option.dataOption == DataOption.DEFECTS4J && option.patchOption == PatchOption.DEFECTS4J ||
                option.dataOption == DataOption.QUIX_BUGS && option.patchOption == PatchOption.QUIX_BUGS
        ) {
            return handleByPatches();
        } else {
            return handleByGenerator();
        }
    }

    // buggy files & human patches & generated patches are given
    private List<String> handleByPatches() {
        String dataPath = Support.getFilePath(DirType.DATA_DIR, option);
        String featurePath = Support.getFilePath(DirType.FEATURE_DIR, option);
        List<String> blackList = getBlacklist();

        List<String> filePaths = new ArrayList<>();
        String binFilePath = featurePath + "serial.bin";
        if (new File(binFilePath).exists()) {
            filePaths = deserialize(binFilePath);
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
                            List<FeatureVector> featureVectors = codeDiffer.runByPatches(oldFile, catalog.get(oldFile));
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
            serialize(binFilePath, filePaths);
        }
        return filePaths;
    }

    // buggy files & human patches are given
    private List<String> handleByGenerator() {
        String dataPath = Support.getFilePath(DirType.DATA_DIR, option);
        String featurePath = Support.getFilePath(DirType.FEATURE_DIR, option);
        List<String> blackList = getBlacklist();

        List<String> filePaths = new ArrayList<>();
        String binFilePath = featurePath + "serial.bin";
        if (new File(binFilePath).exists()) {
            filePaths = deserialize(binFilePath);
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
                            List<FeatureVector> featureVectors = codeDiffer.runByGenerator(oldFile, catalog.get(oldFile));
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
            serialize(binFilePath, filePaths);
        }
        return filePaths;
    }

    private static List<String> deserialize(String filePath) {
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

    private static void serialize(String filePath, List<String> strings) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
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
