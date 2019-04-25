package fr.inria.prophet4j.learner;

import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.utility.Structure;
import fr.inria.prophet4j.feature.extended.ExtendedFeatureCross;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Option.RankingOption;
import fr.inria.prophet4j.utility.Structure.ParameterVector;
import fr.inria.prophet4j.feature.original.OriginalFeatureCross;
import fr.inria.prophet4j.utility.Support;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// used to generate csv files for YE
public class Tool {
    private Option option;

    private Tool(Option option) {
        this.option = option;
    }

    private void genWeightsCSV() {
        String filePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option);
        String inputFilePath = filePath + "ParameterVector";
        String[] tmp = Support.getFilePath(Support.DirType.FEATURE_DIR, option).split("/");
        String prefix = tmp[tmp.length - 1];
        String outputFilePath = filePath + prefix + "feature-weights.csv";

        ParameterVector parameterVector = new ParameterVector(option.featureOption);
        parameterVector.load(inputFilePath);

        List<String> header = new ArrayList<>();
        List<String> values = new ArrayList<>();
        header.add("info");
        values.add("weights");
        for (int idx = 0; idx < parameterVector.size(); idx++) {
            FeatureCross featureCross;
            switch (option.featureOption) {
                case ORIGINAL:
                    featureCross = new OriginalFeatureCross(idx);
                    header.add(featureCross.getFeatures().toString());
                    break;
                case EXTENDED:
                    featureCross = new ExtendedFeatureCross(idx);
                    header.add(featureCross.getFeatures().toString());
                    break;
                default:
                    System.out.println("Out of Expectation");
                    break;
            }
            values.add(String.valueOf(parameterVector.get(idx)));
        }

        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header.toArray(new String[0])));
            // only one line of data
            csvPrinter.printRecord(values);
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void genVectorsCSV(RankingOption rankingOption) {
        String filePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option);
        String inputFilePath = filePath + "ParameterVector";
        String[] tmp = Support.getFilePath(Support.DirType.FEATURE_DIR, option).split("/");
        String prefix = tmp[tmp.length - 1] + "[" + rankingOption.name().toLowerCase() + "]";
        String outputFilePath = filePath + prefix + "feature-vectors.csv";

        ParameterVector parameterVector = new ParameterVector(option.featureOption);
        parameterVector.load(inputFilePath);

        List<String> header = new ArrayList<>();
        List<String> values = new ArrayList<>();
        header.add("info");
//        values.add("features");
        for (int idx = 0; idx < parameterVector.size(); idx++) {
            FeatureCross featureCross;
            switch (option.featureOption) {
                case ORIGINAL:
                    featureCross = new OriginalFeatureCross(idx);
                    header.add(featureCross.getFeatures().toString());
                    break;
                case EXTENDED:
                    featureCross = new ExtendedFeatureCross(idx);
                    header.add(featureCross.getFeatures().toString());
                    break;
                default:
                    System.out.println("Out of Expectation");
                    break;
            }
            values.add("0");
        }

        String rankingFilePath = Support.getFilePath4Ranking(rankingOption);

        RepairEvaluator repairEvaluator = new RepairEvaluator(option);
        Map<String, Map<File, File>> rankingFiles = repairEvaluator.loadFiles(rankingFilePath);

        List<List<String>> valueLists = new ArrayList<>();
        CodeDiffer codeDiffer = new CodeDiffer(false, option);
        Map<String, Map<File, Double>> scores4Files = new HashMap<>();
        for (String key : rankingFiles.keySet()) {
            if (!scores4Files.containsKey(key)) {
                scores4Files.put(key, new HashMap<>());
            }
            Map<File, File> pairs = rankingFiles.get(key);
            for (File buggyFile : pairs.keySet()) {
                File patchedFile = pairs.get(buggyFile);
                List<Structure.FeatureVector> featureVectors = codeDiffer.runByGenerator(buggyFile, patchedFile);
                // maybe we should compute the average but not the sum todo consider
                for (Structure.FeatureVector featureVector : featureVectors) {
                    List<String> valueList = new ArrayList<>(values);
                    List<FeatureCross> featureCrosses = featureVector.getFeatureCrosses();
                    for (FeatureCross featureCross : featureCrosses) {
                        valueList.set(featureCross.getId(), "1");
                    }
                    valueList.add(0, patchedFile.getParentFile().getParentFile().getName());
                    valueLists.add(valueList);
                }
            }
        }

        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header.toArray(new String[0])));
            // only one line of data
            for (List<String> valueList : valueLists) {
                csvPrinter.printRecord(valueList);
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Option option = new Option();
        Tool tool = new Tool(option);
        option.dataOption = DataOption.BUG_DOT_JAR;
        option.patchOption = PatchOption.BUG_DOT_JAR;

        option.featureOption = FeatureOption.ORIGINAL;
        tool.genWeightsCSV();
        tool.genVectorsCSV(RankingOption.D_CORRECT);
        tool.genVectorsCSV(RankingOption.D_INCORRECT);

        option.featureOption = FeatureOption.EXTENDED;
        tool.genWeightsCSV();
        tool.genVectorsCSV(RankingOption.D_CORRECT);
        tool.genVectorsCSV(RankingOption.D_INCORRECT);
    }
}
