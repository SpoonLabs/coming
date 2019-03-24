package fr.inria.prophet4j.dataset;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

class DataLoader {
    // load buggy files and human patches
    static Map<String, Map<File, File>> loadCardumenDataWithoutPatches(String dataPath, String patchPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
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
                    FilenameFilter filter = (dir, name) -> name.endsWith(".java");
                    values.addAll(Arrays.asList(patchedFile.listFiles(filter)));
                }
                for (File key : keys) {
                    String keyName = key.getName();
                    List<File> patches = new ArrayList<>();
                    // we add human patch at the first place
                    File scopeFile = new File(patchPath + pathName);
                    for (File file : Lists.newArrayList(Files.fileTraverser().depthFirstPreOrder(scopeFile))) {
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

    // load buggy files and human patches
    static Map<String, Map<File, File>> loadSANERData(String dataPath) throws NullPointerException {
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

    // load buggy files and human patches, as well as generated patches
    static Map<String, Map<File, List<File>>> loadCardumenDataWithPatches(String dataPath, String patchPath) throws NullPointerException {
        Map<String, Map<File, List<File>>> catalogs = new HashMap<>();
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
                    FilenameFilter filter = (dir, name) -> name.endsWith(".java");
                    values.addAll(Arrays.asList(patchedFile.listFiles(filter)));
                }
                for (File key : keys) {
                    String keyName = key.getName();
                    List<File> patches = new ArrayList<>();
                    // we add human patch at the first place
                    File scopeFile = new File(patchPath + pathName);
                    for (File file : Lists.newArrayList(Files.fileTraverser().depthFirstPreOrder(scopeFile))) {
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
}
