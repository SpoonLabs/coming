package fr.inria.prophet4j.dataset;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

class DataLoader {
    // load buggy files and human patches
    static Map<String, Map<File, File>> loadSANERWithoutPatches(String dataPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        for (File typeFile : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
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

    // load buggy files and human patches
    static Map<String, Map<File, File>> loadCardumenWithoutPatches(String dataPath, String patchPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        FilenameFilter javaFilter = (dir, name) -> name.endsWith(".java");
        for (File typeFile : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            for (File numFile : typeFile.listFiles((dir, name) -> !name.startsWith("."))) {
                String pathName = typeFile.getName() + "/" + numFile.getName();
                if (!catalogs.containsKey(pathName)) {
                    catalogs.put(pathName, new HashMap<>());
                }
                Map<File, File> catalog = catalogs.get(pathName);
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
                    values.addAll(Arrays.asList(patchedFile.listFiles(javaFilter)));
                }
                for (File key : keys) {
                    String keyName = key.getName();
                    List<File> patches = new ArrayList<>();
                    // we add human patch at the first place
                    File humanFile = new File(patchPath + pathName);
                    for (File file : Lists.newArrayList(Files.fileTraverser().depthFirstPreOrder(humanFile))) {
                        String fileName = file.getName();
                        if (keyName.equals(fileName)) {
                            patches.add(file);
                        }
                    }
                    if (patches.size() == 1) {
                        // the following files are generated patches
                        for (File value : values) {
                            String valueName = value.getName();
                            if (keyName.equals(valueName)) {
                                patches.add(value);
                            }
                        }
                        if (patches.size() > 1) {
                            catalog.put(key, patches.get(0));
                        }
                    }
                }
            }
        }
        return catalogs;
    }

    // load buggy files and human patches, as well as generated patches
    static Map<String, Map<File, List<File>>> loadCardumenWithPatches(String dataPath, String patchPath) throws NullPointerException {
        Map<String, Map<File, List<File>>> catalogs = new HashMap<>();
        FilenameFilter javaFilter = (dir, name) -> name.endsWith(".java");
        for (File typeFile : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            for (File numFile : typeFile.listFiles((dir, name) -> !name.startsWith("."))) {
                String pathName = typeFile.getName() + "/" + numFile.getName();
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
                    values.addAll(Arrays.asList(patchedFile.listFiles(javaFilter)));
                }
                for (File key : keys) {
                    String keyName = key.getName();
                    List<File> patches = new ArrayList<>();
                    // we add human patch at the first place
                    File humanFile = new File(patchPath + pathName);
                    for (File file : Lists.newArrayList(Files.fileTraverser().depthFirstPreOrder(humanFile))) {
                        String fileName = file.getName();
                        if (keyName.equals(fileName)) {
                            patches.add(file);
                        }
                    }
                    if (patches.size() == 1) {
                        // the following files are generated patches
                        for (File value : values) {
                            String valueName = value.getName();
                            if (keyName.equals(valueName)) {
                                patches.add(value);
                            }
                        }
                        if (patches.size() > 1) {
                            catalog.put(key, patches);
                        }
                    }
                }
            }
        }
        return catalogs;
    }

    /**
     for Project ODS(OverfittingDetectionSystem)
     BEARS BUG_DOT_JAR_MINUS_MATH QUIX_BUGS
     */
    // load buggy files and human patches
    static Map<String, Map<File, File>> loadODSWithoutPatches(String dataPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        FilenameFilter javaFilter = (dir, name) -> name.endsWith(".java");
        for (File eachFile : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            File humanFile = null;
            try {
                File[] humanFiles = new File(eachFile.getPath() + "/human").listFiles(javaFilter);
                if (humanFiles.length == 1) {
                    humanFile = humanFiles[0];
                } else {
                    continue;
                }
            } catch (NullPointerException e) {
//                e.printStackTrace();
                continue;
            }

            String pathName = eachFile.getName();
            if (!catalogs.containsKey(pathName)) {
                catalogs.put(pathName, new HashMap<>());
            }
            Map<File, File> catalog = catalogs.get(pathName);
            File buggyFile = null;

            String humanFileName = humanFile.getName();
            FilenameFilter nameFilter = (dir, name) -> name.equals(humanFileName);
            label: for (File typeFile : eachFile.listFiles((dir, name) -> !name.startsWith("."))) {
                if (typeFile.getName().equals("human")) {
                    continue;
                }
                for (File numFile : typeFile.listFiles((dir, name) -> !name.startsWith("."))) {
                    if (buggyFile == null) {
                        File[] keyFiles = new File(numFile.getPath() + "/buggy").listFiles(nameFilter);
                        if (keyFiles.length == 1) {
                            buggyFile = keyFiles[0];
                            break label;
                        }
                    }
                }
            }

            catalog.put(buggyFile, humanFile);
        }
        return catalogs;
    }

    // load buggy files and human patches, as well as generated patches
    static Map<String, Map<File, List<File>>> loadODSWithPatches(String dataPath) throws NullPointerException {
        Map<String, Map<File, List<File>>> catalogs = new HashMap<>();
        FilenameFilter javaFilter = (dir, name) -> name.endsWith(".java");
        for (File eachFile : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            File humanFile = null;
            try {
                File[] humanFiles = new File(eachFile.getPath() + "/human").listFiles(javaFilter);
                if (humanFiles.length == 1) {
                    humanFile = humanFiles[0];
                } else {
                    continue;
                }
            } catch (NullPointerException e) {
//                e.printStackTrace();
                continue;
            }

            String pathName = eachFile.getName();
            if (!catalogs.containsKey(pathName)) {
                catalogs.put(pathName, new HashMap<>());
            }
            Map<File, List<File>> catalog = catalogs.get(pathName);
            File buggyFile = null;
            List<File> patchedFiles = new ArrayList<>();
            // we add human patch at the first place
            patchedFiles.add(0, humanFile);

            String humanFileName = humanFile.getName();
            FilenameFilter nameFilter = (dir, name) -> name.equals(humanFileName);
            for (File typeFile : eachFile.listFiles((dir, name) -> !name.startsWith("."))) {
                if (typeFile.getName().equals("human")) {
                    continue;
                }
                for (File numFile : typeFile.listFiles((dir, name) -> !name.startsWith("."))) {
                    if (buggyFile == null) {
                        File[] keyFiles = new File(numFile.getPath() + "/buggy").listFiles(nameFilter);
                        if (keyFiles.length == 1) {
                            buggyFile = keyFiles[0];
                        }
                    }
                    File[] valueFiles = new File(numFile.getPath() + "/patch").listFiles(nameFilter);
                    if (valueFiles.length == 1) {
                        // the following files are generated patches
                        patchedFiles.addAll(Arrays.asList(valueFiles));
                    }
                }
            }

            catalog.put(buggyFile, patchedFiles);
        }
        return catalogs;
    }

    static Map<String, Map<File, File>> loadCLOSUREWithoutPatches(String dataPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        System.out.println(dataPath);
        for (File file : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            // patchInfo
            String pathName = file.getName();
            File buggyFile = null;
            File patchedFile = null;
            for (File tmpFile : Files.fileTraverser().breadthFirst(file)) {
                if (tmpFile.getName().endsWith("_s.java")) {
                    buggyFile = tmpFile;
                } else if (tmpFile.getName().endsWith("_t.java")) {
                    patchedFile = tmpFile;
                }
            }
            if (buggyFile != null && patchedFile != null) {
                Map<File, File> catalog = new HashMap<>();
                catalog.put(buggyFile, patchedFile);

                if (!catalogs.containsKey(pathName)) {
                    catalogs.put(pathName, catalog);
                } else {
                    catalogs.get(pathName).putAll(catalog);
                }
            }
        }
        return catalogs;
    }
}
