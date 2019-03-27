package fr.inria.prophet4j.defined;

import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.ParameterVector;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Support;

import java.io.File;
import java.util.*;

// https://github.com/kth-tcs/defects4j-repair-reloaded/tree/master/drr-fullcontext
// score and rank patches
public class RepairEvaluator {

    private CodeDiffer codeDiffer;
    private ParameterVector parameterVector;

    public RepairEvaluator(Option option) {
        this.codeDiffer = new CodeDiffer(false, option);
        this.parameterVector = new ParameterVector(option.featureOption);

        String parameterFilePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option) + "ParameterVector";
        this.parameterVector.load(parameterFilePath);
    }

    // example : Map<"Chart/3", Map<buggy file, patched file>>
    private Map<String, Map<File, File>> loadFiles(String dataPath) throws NullPointerException {
        Map<String, Map<File, File>> catalogs = new HashMap<>();
        for (File file : new File(dataPath).listFiles((dir, name) -> !name.startsWith("."))) {
            if (file.getName().equals("patch7-Closure-93-SequenceR")) {
                // report this case to He YE tomorrow
                continue;
            }
            String[] info = file.getName().split("-");
            // typeInfo + "/" + numInfo
            String pathName = info[1] + "/" + info[2];
            if (!catalogs.containsKey(pathName)) {
                catalogs.put(pathName, new HashMap<>());
            }
            Map<File, File> catalog = catalogs.get(pathName);
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
            assert buggyFile != null;
            assert patchedFile != null;
            catalog.put(buggyFile, patchedFile);
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
                double score = 0.0;
                List<FeatureVector> featureVectors = codeDiffer.func4Demo(buggyFile, patchedFile);
                // maybe we should compute the average but not the sum todo consider
                for (FeatureVector featureVector : featureVectors) {
                    score += featureVector.score(parameterVector);
                }
                value.put(patchedFile, score);
            }
        }
        return scores4Files;
    }

    public void func4Demo() {
        // here we handle buggy and patched files but not patch files
        String correctFilesPath = Support.PROPHET4J_DIR + "D_correct/";
        String incorrectFilesPath = Support.PROPHET4J_DIR + "D_incorrect/";

        Map<String, Map<File, File>> correctFiles = loadFiles(correctFilesPath);
        Map<String, Map<File, File>> incorrectFiles = loadFiles(incorrectFilesPath);

        System.out.println("loaded files");

        Map<String, Map<File, Double>> scores4CorrectFiles = scoreFiles(correctFiles);
        Map<String, Map<File, Double>> scores4IncorrectFiles = scoreFiles(incorrectFiles);

        System.out.println("scored files");

        // we only care ranks info 4 CorrectFiles
        Map<String, Map<File, Fraction>> ranks4CorrectFiles = new HashMap<>();
        // we want the interaction-set of both keySets
        Set<String> keys = new HashSet<>(correctFiles.keySet());
        keys.retainAll(incorrectFiles.keySet());
        for (String key : keys) {
            if (!ranks4CorrectFiles.containsKey(key)) {
                ranks4CorrectFiles.put(key, new HashMap<>());
            }
            Map<File, Fraction> rankPairs4CorrectFiles = ranks4CorrectFiles.get(key);

            Map<File, Double> scorePairs4CorrectFiles = scores4CorrectFiles.get(key);
            Map<File, Double> scorePairs4IncorrectFiles = scores4IncorrectFiles.get(key);

            List<Double> scoresBoard = new ArrayList<>(scorePairs4IncorrectFiles.values());
            for (File correctFile : scorePairs4CorrectFiles.keySet()) {
                Double score4CorrectFile = scorePairs4CorrectFiles.get(correctFile);
                scoresBoard.add(score4CorrectFile);
                scoresBoard.sort(Double::compareTo);
                int numerator = scoresBoard.indexOf(score4CorrectFile) + 1;
                int denominator = scoresBoard.size();
                rankPairs4CorrectFiles.put(correctFile, new Fraction(numerator, denominator));
                scoresBoard.remove(score4CorrectFile);
            }
        }

        System.out.println("ranked files");

        // analyze ranks info
        for (String key : ranks4CorrectFiles.keySet()) {
            System.out.println(key);
            for (Fraction fraction : ranks4CorrectFiles.get(key).values()) {
                System.out.println(fraction);
            }
            System.out.println("================");
        }
    }

    private class Fraction {
        private int numerator;
        private int denominator;

        Fraction(int numerator, int denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        @Override
        public String toString() {
            return numerator + "/" + denominator;
        }
    }
    /* Cardumen + Cardumen
Lang/20
1/4
================
Lang/41
1/2
================
Lang/60
1/2
================
Chart/26
3/3
3/3
3/3
================
Closure/73
1/2
2/2
================
Closure/33
2/3
================
Math/82
5/10
7/10
================
Lang/58
1/2
================
Closure/18
1/5
================
Lang/39
3/4
================
Lang/16
3/3
================
Math/85
24/27
8/27
================
Lang/55
8/9
8/9
8/9
================
Math/63
1/11
================
Lang/51
3/8
3/8
3/8
2/8
2/8
3/8
================
Chart/5
2/2
2/2
================
Math/80
29/31
================
Chart/3
2/3
================
Chart/12
4/4
================
Chart/9
1/4
1/4
1/4
================
Closure/115
1/2
================
Math/58
1/2
================
Math/2
7/12
================
Math/71
1/2
================
Math/50
2/4
3/4
1/4
2/4
================
Math/73
4/4
1/4
================
Lang/27
3/3
================
Lang/44
2/2
================
Math/5
4/4
4/4
4/4
4/4
4/4
================
Lang/22
1/5
================
Math/53
2/5
1/5
2/5
================
Lang/43
2/5
2/5
================
Math/32
3/3
================
Lang/45
1/3
================
     */
    /* SANER + SPR
Lang/20
1/4
================
Lang/41
1/2
================
Lang/60
1/2
================
Chart/26
3/3
3/3
3/3
================
Closure/73
2/2
2/2
================
Closure/33
3/3
================
Math/82
5/10
10/10
================
Lang/58
1/2
================
Closure/18
2/5
================
Lang/39
3/4
================
Lang/16
3/3
================
Math/85
24/27
9/27
================
Lang/55
8/9
8/9
8/9
================
Math/63
3/11
================
Lang/51
2/8
2/8
2/8
2/8
2/8
2/8
================
Chart/5
1/2
1/2
================
Math/80
27/31
================
Chart/3
2/3
================
Chart/12
4/4
================
Chart/9
1/4
1/4
2/4
================
Closure/115
1/2
================
Math/58
1/2
================
Math/2
9/12
================
Math/71
1/2
================
Math/50
2/4
4/4
1/4
2/4
================
Math/73
4/4
2/4
================
Lang/27
3/3
================
Lang/44
2/2
================
Math/5
1/4
1/4
4/4
1/4
4/4
================
Lang/22
1/5
================
Math/53
3/5
1/5
3/5
================
Lang/43
2/5
2/5
================
Math/32
3/3
================
Lang/45
1/3
================
     */
}
