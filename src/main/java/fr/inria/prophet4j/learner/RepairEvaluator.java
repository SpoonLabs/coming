package fr.inria.prophet4j.learner;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import fr.inria.prophet4j.utility.Structure.ParameterVector;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.RankingOption;
import fr.inria.prophet4j.utility.Support;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

// https://github.com/kth-tcs/defects4j-repair-reloaded/tree/master/drr-fullcontext
// score and rank patches
// the way of computing scores is straightforward as we just use scores to rank patches
public class RepairEvaluator {

    private Option option;
    private CodeDiffer codeDiffer;
    private ParameterVector parameterVector;

    public RepairEvaluator(Option option) {
        this.option = option;
        this.codeDiffer = new CodeDiffer(false, option);
        this.parameterVector = new ParameterVector(option.featureOption);

        String parameterFilePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option) + "ParameterVector";
        this.parameterVector.load(parameterFilePath);
    }

    // example : Map<"Chart/3", Map<buggy file, patched file>>
    public Map<String, Map<File, File>> loadFiles(String dataPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        for (File file : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            if (file.getName().equals("patch7-Closure-93-SequenceR")) {
                // report this case to He YE someday
                continue;
            }
            String[] info = file.getName().split("-");
            // typeInfo + numInfo
            String pathName = info[1] + info[2];
            File buggyFile = null;
            File patchedFile = null;
            for (File dataFile : file.listFiles((dir, name) -> !name.startsWith("."))) {
                if (dataFile.getName().equals("buggy")) {
                    List<File> childFiles = Arrays.asList(dataFile.listFiles((dir, name) -> name.endsWith(".java")));
                    assert childFiles.size() == 1;
                    buggyFile = childFiles.get(0);
                } else if (dataFile.getName().equals("patched")) {
                    List<File> childFiles = Arrays.asList(dataFile.listFiles((dir, name) -> name.endsWith(".java")));
                    assert childFiles.size() == 1;
                    patchedFile = childFiles.get(0);
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

    // for D_HUMAN
    // example : Map<"Chart/3", Map<buggy file, patched file>>
    private Map<String, Map<File, File>> loadFiles(String dataPath, String auxPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        for (File file : new File(auxPath).listFiles((dir, name) -> !name.startsWith("."))) {
            if (file.getName().equals("patch7-Closure-93-SequenceR")) {
                // report this case to He YE someday
                continue;
            }
            String[] info = file.getName().split("-");
            // typeInfo + numInfo
            String pathName = info[1] + info[2];
            File buggyFile = null;
            File patchedFile = null;
            for (File dataFile : file.listFiles((dir, name) -> !name.startsWith("."))) {
                if (dataFile.getName().equals("buggy")) {
                    List<File> childFiles = Arrays.asList(dataFile.listFiles((dir, name) -> name.endsWith(".java")));
                    assert childFiles.size() == 1;
                    buggyFile = childFiles.get(0);
                }
            }
            if (buggyFile != null) {
                String buggyFileName = buggyFile.getName();
                File scopeFile = new File(dataPath + info[1] + "/" + info[2] + "/");
                for (File tmpFile : Lists.newArrayList(Files.fileTraverser().depthFirstPreOrder(scopeFile))) {
                    if (tmpFile.getName().equals(buggyFileName)) {
                        patchedFile = tmpFile;
                        break;
                    }
                }
                if (patchedFile != null) {
                    Map<File, File> catalog = new HashMap<>();
                    catalog.put(buggyFile, patchedFile);

                    if (!catalogs.containsKey(pathName)) {
                        catalogs.put(pathName, catalog);
                    } else {
                        catalogs.get(pathName).putAll(catalog);
                    }
                }
            }
        }
        return catalogs;
    }

    private Map<String, Map<File, Double>> scoreFiles(Map<String, Map<File, File>> files) {
        Map<String, Map<File, Double>> scores4Files = new HashMap<>();
        for (String key : files.keySet()) {
            if (!scores4Files.containsKey(key)) {
                scores4Files.put(key, new HashMap<>());
            }
            Map<File, Double> value = scores4Files.get(key);
            Map<File, File> pairs = files.get(key);
            for (File buggyFile : pairs.keySet()) {
                File patchedFile = pairs.get(buggyFile);
                List<FeatureMatrix> featureMatrices = codeDiffer.runByGenerator(buggyFile, patchedFile);
                if (featureMatrices.size() == 1) {
                    value.put(patchedFile, featureMatrices.get(0).score(parameterVector));
                }
            }
            if (value.size() == 0) {
                scores4Files.remove(key);
            }
        }
        return scores4Files;
    }

    public void run() {
        run(RankingOption.D_HUMAN, RankingOption.D_INCORRECT);
    }

    public void run(RankingOption foreOption, RankingOption backOption) {
        // here we handle buggy and patched files but not patch files
        String foreFilePath = Support.getFilePath4Ranking(foreOption);
        String backFilePath = Support.getFilePath4Ranking(backOption);

        Map<String, Map<File, File>> foreFiles = null;
        if (foreOption == RankingOption.D_HUMAN) {
            foreFiles = loadFiles(foreFilePath, backFilePath);
        } else {
            foreFiles = loadFiles(foreFilePath);
        }
        assert backOption != RankingOption.D_HUMAN;
        Map<String, Map<File, File>> backFiles = loadFiles(backFilePath);
        System.out.println("loaded files");

        // we want the interaction-set of both keySets
        Set<String> foreKeys = foreFiles.keySet();
        Set<String> backKeys = backFiles.keySet();
        Set<String> uniqueForeKeys = new HashSet<>(foreKeys);
        Set<String> uniqueBackKeys = new HashSet<>(backKeys);
        uniqueForeKeys.removeAll(backKeys);
        uniqueBackKeys.removeAll(foreKeys);
        for (String key : uniqueForeKeys) foreFiles.remove(key);
        for (String key : uniqueBackKeys) backFiles.remove(key);
        Map<String, Map<File, Double>> scores4ForeFiles = scoreFiles(foreFiles);
        Map<String, Map<File, Double>> scores4BackFiles = scoreFiles(backFiles);
        foreKeys = scores4ForeFiles.keySet();
        backKeys = scores4BackFiles.keySet();
        uniqueForeKeys = new HashSet<>(foreKeys);
        uniqueBackKeys = new HashSet<>(backKeys);
        uniqueForeKeys.removeAll(backKeys);
        uniqueBackKeys.removeAll(foreKeys);
        for (String key : uniqueForeKeys) scores4ForeFiles.remove(key);
        for (String key : uniqueBackKeys) scores4BackFiles.remove(key);
        System.out.println("scored files");

        // we only care ranks info for ForeFiles
        Map<String, Map<File, Fraction>> ranks4ForeFiles = new HashMap<>();
        for (String key : scores4ForeFiles.keySet()) {
            ranks4ForeFiles.put(key, new HashMap<>());
            Map<File, Fraction> rankPairs4ForeFiles = ranks4ForeFiles.get(key);

            Map<File, Double> scorePairs4ForeFiles = scores4ForeFiles.get(key);
            Map<File, Double> scorePairs4BackFiles = scores4BackFiles.get(key);

            List<Double> scoresBoard = new ArrayList<>(scorePairs4BackFiles.values());
            for (File foreFile : scorePairs4ForeFiles.keySet()) {
                Double score4ForeFile = scorePairs4ForeFiles.get(foreFile);
                scoresBoard.add(score4ForeFile);
                scoresBoard.sort(Double::compareTo);
                int numerator = scoresBoard.indexOf(score4ForeFile) + 1;
                int denominator = scoresBoard.size();
                rankPairs4ForeFiles.put(foreFile, new Fraction(numerator, denominator));
                scoresBoard.remove(score4ForeFile);
            }
        }
        System.out.println("ranked files");

        List<Ranking> rankings = new ArrayList<>();
        for (String key : ranks4ForeFiles.keySet()) {
            // mean
            double meanNumerator = 0;
            double meanDenominator = 0;
            for (Fraction fraction : ranks4ForeFiles.get(key).values()) {
                meanNumerator += (int) fraction.numerator;
                // here we add denominators because we know they are all the same value
                meanDenominator += (int) fraction.denominator;
            }
            int size = ranks4ForeFiles.get(key).values().size();
            meanNumerator /= size;
            meanDenominator /= size;
            // SD
            double sumSquaredNumerator = 0;
            for (Fraction fraction : ranks4ForeFiles.get(key).values()) {
                sumSquaredNumerator += Math.pow((int) fraction.numerator, 2);
            }
            double sdNumerator = Math.sqrt(sumSquaredNumerator / size - Math.pow(meanNumerator, 2));
            // median
            List<Double> numerators = new ArrayList<>();
            for (Fraction fraction : ranks4ForeFiles.get(key).values()) {
                numerators.add((double) (int) fraction.numerator);
            }
            double medianNumerator = 0;
            if (size % 2 == 0) {
                medianNumerator = (numerators.get(size / 2 - 1) + numerators.get(size / 2)) / 2;
            } else {
                medianNumerator = numerators.get(size / 2);
            }
            rankings.add(new Ranking(key, meanDenominator, medianNumerator, meanNumerator, sdNumerator));
        }
        StringJoiner stringJoiner = new StringJoiner("-", "RankingTable-", ".csv");
        StringJoiner trainStringJoiner = new StringJoiner("-", "Training(", ")");
        trainStringJoiner.add(option.dataOption.name());
        trainStringJoiner.add(option.patchOption.name());
        stringJoiner.add(trainStringJoiner.toString());
        StringJoiner testStringJoiner = new StringJoiner("-", "Testing(", ")");
        testStringJoiner.add(foreOption.name());
        testStringJoiner.add(backOption.name());
        stringJoiner.add(testStringJoiner.toString());

        String filePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option) + stringJoiner.toString();
        dumpCSV(filePath, rankings);
        System.out.println("dumped files");

        System.out.println("EvaluationResult is saved to " + filePath);
    }

    // consider move to DataHelper and make more general
    private void dumpCSV(String csvFileName, List<Ranking> rankings) {
        List<String> header = new ArrayList<>();
        header.add("entryName");
        header.add("number");
        header.add("median");
        header.add("mean");
        header.add("SD");
        try {
            BufferedWriter writer = java.nio.file.Files.newBufferedWriter(Paths.get(csvFileName));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header.toArray(new String[0])));
            rankings.sort(Comparator.comparing(Ranking::getMean).thenComparing(Ranking::getSD).thenComparing(Ranking::getEntryName));
            for (Ranking ranking : rankings) {
                csvPrinter.printRecord(ranking.getValues());
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Ranking {
        private String entryName;
        private double number;
        private double median;
        private double mean;
        private double SD;

        Ranking(String entryName, double number, double median, double mean, double SD) {
            this.entryName = entryName;
            this.number = number;
            this.median = median;
            this.mean = mean;
            this.SD = SD;
        }

        public String getEntryName() {
            return entryName;
        }

        public double getMean() {
            return mean;
        }

        public double getSD() {
            return SD;
        }

        public List<String> getValues() {
            List<String> strings = new ArrayList<>();
            strings.add(entryName);
            strings.add(String.valueOf(number));
            strings.add(String.valueOf(median));
            strings.add(String.valueOf(mean));
            strings.add(String.valueOf(SD));
            return strings;
        }
    }

    private class Fraction<T> {
        private T numerator;
        private T denominator;

        Fraction(T numerator, T denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        @Override
        public String toString() {
            return numerator + "/" + denominator;
        }
    }
}
