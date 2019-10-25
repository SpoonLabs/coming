package fr.inria.prophet4j.dataset;

import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
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
                        blacklist.add("ignatov_intellij-erlang10/ErlangFormattingModelBuilder.bin");
                        blacklist.add("ignatov_intellij-erlang14/ErlangFormattingModelBuilder.bin");
                        blacklist.add("JetBrains_kotlin28/JetFormattingModelBuilder.bin");
                        blacklist.add("JetBrains_kotlin14/JetFormattingModelBuilder.bin");
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
            case BUG_DOT_JAR_MINUS_MATH:
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
            case BUG_DOT_JAR_MINUS_MATH:
            case QUIX_BUGS:
                return DataLoader.loadODSWithoutPatches(dataPath);
            case CLOSURE:
                return DataLoader.loadCLOSUREWithoutPatches(dataPath);
        }
        return new HashMap<>();
    }

    public List<String> run() {
        if (option.dataOption == DataOption.CARDUMEN && option.patchOption == PatchOption.CARDUMEN ||
                option.dataOption == DataOption.BEARS && option.patchOption == PatchOption.BEARS ||
                option.dataOption == DataOption.BUG_DOT_JAR_MINUS_MATH && option.patchOption == PatchOption.BUG_DOT_JAR_MINUS_MATH ||
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
        String binFilePath = featurePath + "catalog.bin";
        if (new File(binFilePath).exists()) {
            filePaths = Support.deserialize(binFilePath);
        } else {
            CodeDiffer codeDiffer = new CodeDiffer(false, option);
            Map<String, Map<File, List<File>>> catalogs = loadDataWithPatches(dataPath);
            int progressAll = catalogs.size(), progressNow = 0;
            for (String pathName : catalogs.keySet()) {
                codeDiffer.setPathName(pathName);
                Map<File, List<File>> catalog = catalogs.get(pathName);
                for (File oldFile : catalog.keySet()) {
                    try {
                        String tmpFileName = oldFile.getName().replace(".java", ".bin");
                        String vectorPath = pathName + "/" + tmpFileName;
                        System.out.println(vectorPath);
                        if (blackList.contains(vectorPath)) {
                            progressNow += 1;
                            System.out.println("blacklist");
                            continue;
                        }
                        vectorPath = featurePath + vectorPath;
                        File vectorFile = new File(vectorPath);
                        if (!vectorFile.exists()) {
                            List<FeatureMatrix> featureMatrices = codeDiffer.runByPatches(oldFile, catalog.get(oldFile));
                            if (featureMatrices.size() == 0) {
                                // diff.commonAncestor() returns null value
                                continue;
                            }
                            new Sample(vectorFile.getPath()).saveFeatureMatrices(featureMatrices);
                        }
                        if (!filePaths.contains(vectorPath)) {
                            filePaths.add(vectorPath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressNow += 1;
                System.out.println(pathName + " : " + progressNow + " / " + progressAll);
            }
            Support.serialize(binFilePath, filePaths);
        }
        return filePaths;
    }

    // buggy files & human patches are given
    private List<String> handleByGenerator() {
        String dataPath = Support.getFilePath(DirType.DATA_DIR, option);
        String featurePath = Support.getFilePath(DirType.FEATURE_DIR, option);
        List<String> blackList = getBlacklist();

        List<String> filePaths = new ArrayList<>();
        String binFilePath = featurePath + "catalog.bin";
        if (new File(binFilePath).exists()) {
            filePaths = Support.deserialize(binFilePath);
        } else {
            CodeDiffer codeDiffer = new CodeDiffer(true, option);
            Map<String, Map<File, File>> catalogs = loadDataWithoutPatches(dataPath);
            int progressAll = catalogs.size(), progressNow = 0;
            for (String pathName : catalogs.keySet()) {
                Map<File, File> catalog = catalogs.get(pathName);
                for (File oldFile : catalog.keySet()) {
                    try {
                        String tmpFileName = oldFile.getName().replace(".java", ".bin");
                        String vectorPath = pathName + "/" + tmpFileName;
                        System.out.println(vectorPath);
                        if (blackList.contains(vectorPath)) {
                            progressNow += 1;
                            System.out.println("blacklist");
                            continue;
                        }
                        vectorPath = featurePath + vectorPath;
                        File vectorFile = new File(vectorPath);
                        if (!vectorFile.exists()) {
                            List<FeatureMatrix> featureMatrices = codeDiffer.runByGenerator(oldFile, catalog.get(oldFile));
                            // we should have more than one FeatureMatrix when CodeDiffer's "byGenerator" is true
                            if (featureMatrices.size() == 0) {
                                continue;
                            }
                            if (featureMatrices.get(0).getFeatureVectors().size() == 0) {
                                // diff.commonAncestor() returns null value
                                continue;
                            }
                            new Sample(vectorFile.getPath()).saveFeatureMatrices(featureMatrices);
                        }
                        if (!filePaths.contains(vectorPath)) {
                            filePaths.add(vectorPath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressNow += 1;
                System.out.println(pathName + " : " + progressNow + " / " + progressAll);
            }
            Support.serialize(binFilePath, filePaths);
        }
        return filePaths;
    }
}
