package fr.inria.prophet4j;

import fr.inria.prophet4j.dataset.DataManager;
import fr.inria.prophet4j.learner.FeatureLearner;
import fr.inria.prophet4j.learner.RepairEvaluator;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Structure.Sample;

import java.util.List;

public class Demo {
    private Option option;

    public Demo(Option option) {
        this.option = option;
    }

    void extract() {
        DataManager dataManager = new DataManager(option);
        List<String> filePaths = dataManager.run();
        for (String filePath : filePaths) {
            Sample sample = new Sample(filePath);
            sample.loadFeatureMatrices();
            sample.saveAsJson(option.featureOption);
        }
        System.out.println("1/1 EXTRACTED");
    }

    void learn() {
        DataManager dataManager = new DataManager(option);
        List<String> filePaths = dataManager.run();
        FeatureLearner featureLearner = new FeatureLearner(option);
        featureLearner.run(filePaths);
        System.out.println("1/1 LEARNED");
    }

    void evaluate() {
        RepairEvaluator repairEvaluator = new RepairEvaluator(option);
        repairEvaluator.run();
        System.out.println("1/1 EVALUATED");
    }

    private static void run(Option option) {
        try {
//            new Demo(option).extract();
//            new Demo(option).learn();
            new Demo(option).evaluate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runDemo(Option option) {
        option.dataOption = DataOption.CLOSURE;
        option.patchOption = PatchOption.SPR;
        option.featureOption = FeatureOption.ORIGINAL;
        run(option);
//        option.featureOption = FeatureOption.EXTENDED;
//        run(option);
    }

    private static void runODS1(Option option) {
        option.dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;
        option.patchOption = PatchOption.BUG_DOT_JAR_MINUS_MATH;
        option.featureOption = FeatureOption.ORIGINAL;
        run(option);
        option.featureOption = FeatureOption.EXTENDED;
        run(option);
        option.featureOption = FeatureOption.S4R;
        run(option);
        option.featureOption = FeatureOption.S4RO;
        run(option);
    }

    private static void runODS2(Option option) {
        option.dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;
        option.patchOption = PatchOption.SPR;
        option.featureOption = FeatureOption.ORIGINAL;
        run(option);
        option.featureOption = FeatureOption.EXTENDED;
        run(option);
    }

    // seem meaningless to use FeatureOption.ENHANCED
    public static void main(String[] args) {
        try {
            Option option = new Option();
            runDemo(option);
//            runODS1(option);
//            runODS2(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // if necessary, config Java VM: -Xms1024m -Xmx16384m
    // one idea: using distributed representation instead of crossing features
}
