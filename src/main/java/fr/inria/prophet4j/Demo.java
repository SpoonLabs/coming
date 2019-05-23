package fr.inria.prophet4j;

import fr.inria.prophet4j.dataset.DataManager;
import fr.inria.prophet4j.learner.FeatureLearner;
import fr.inria.prophet4j.learner.RepairEvaluator;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Structure.Sample;

import java.util.List;

public class Demo {
    private Option option;

    public Demo(Option option) {
        this.option = option;
    }

    void learn() {
        DataManager dataManager = new DataManager(option);
        FeatureLearner featureLearner = new FeatureLearner(option);
        List<String> filePaths = dataManager.run();
        featureLearner.run(filePaths);
        System.out.println("1/1 LEARNED");
    }

    void evaluate() {
        RepairEvaluator repairEvaluator = new RepairEvaluator(option);
//        repairEvaluator.run(RankingOption.D_CORRECT, RankingOption.D_INCORRECT);
//        System.out.println("1/3 Done");
//        repairEvaluator.run(RankingOption.D_HUMAN, RankingOption.D_CORRECT);
//        System.out.println("2/3 Done");
//        repairEvaluator.run(RankingOption.D_HUMAN, RankingOption.D_INCORRECT);
//        System.out.println("3/3 Done");
        repairEvaluator.run();
        System.out.println("1/1 EVALUATED");
    }

    // todo split learn to be extract and learn
    void distillJson() {
        DataManager dataManager = new DataManager(option);
        List<String> filePaths = dataManager.run();
        for (String filePath : filePaths) {
            Sample sample = new Sample(filePath);
            sample.loadFeatureMatrices();
            sample.saveAsJson(option.featureOption);
        }
        System.out.println("1/1 DISTILLED");
    }

    private static void jobsDemo(Option option) {
        try {
            option.dataOption = DataOption.CARDUMEN;
            option.patchOption = PatchOption.CARDUMEN;
            new Demo(option).learn();
            new Demo(option).evaluate();
//            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            option.dataOption = DataOption.SANER;
            option.patchOption = PatchOption.SPR;
            new Demo(option).learn();
            new Demo(option).evaluate();
//            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void jobsODS(Option option) {
        try {
            option.dataOption = DataOption.BUG_DOT_JAR;
            option.patchOption = PatchOption.BUG_DOT_JAR;
            new Demo(option).learn();
            new Demo(option).evaluate();
//            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            option.dataOption = DataOption.BUG_DOT_JAR;
            option.patchOption = PatchOption.SPR;
            new Demo(option).learn();
            new Demo(option).evaluate();
//            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            option.dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;
            option.patchOption = PatchOption.BUG_DOT_JAR_MINUS_MATH;
            new Demo(option).learn();
            new Demo(option).evaluate();
//            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            option.dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;
            option.patchOption = PatchOption.SPR;
            new Demo(option).learn();
            new Demo(option).evaluate();
//            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void jobsS4R(Option option) {
//        try {
//            option.dataOption = DataOption.CARDUMEN;
//            option.patchOption = PatchOption.CARDUMEN;
//            new Demo(option).learn();
//            new Demo(option).evaluate();
//            new Demo(option).distillJson();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            option.dataOption = DataOption.BUG_DOT_JAR;
//            option.patchOption = PatchOption.BUG_DOT_JAR;
//            new Demo(option).learn();
//            new Demo(option).evaluate();
//            new Demo(option).distillJson();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            option.dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;
            option.patchOption = PatchOption.BUG_DOT_JAR_MINUS_MATH;
            new Demo(option).learn();
            new Demo(option).evaluate();
            new Demo(option).distillJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // seem meaningless to use FeatureOption.ENHANCED
    public static void main(String[] args) {
        try {
            Option option = new Option();
            // for Demo
//            option.featureOption = FeatureOption.ORIGINAL;
//            jobsDemo(option);
//            option.featureOption = FeatureOption.EXTENDED;
//            jobsDemo(option);
//            // for ODS
//             option.featureOption = FeatureOption.ORIGINAL;
//             jobsODS(option);
//             option.featureOption = FeatureOption.EXTENDED;
//             jobsODS(option);
//            // for S4R
//            option.featureOption = FeatureOption.S4R;
//            jobsS4R(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // todo try YE's features
    // todo try feature-ext+s4r
    // todo integrate with coming
    // todo document prophet4j
    /* todo sync S4R every week until one stable version
     * 1 check commits since 05/22
     * 2 note conflicting commits
     * 3 comment testing files
     */
    /*
    to improve the performance of FeatureLearner, use CLR(Cyclical Learning Rates) or autoML
     */
    // config VM: -Xms1024m -Xmx16384m
    // name alias:
    // original <==> prophet4j
    // extended <==> feature-ext
    // sketch4repair <==> feature-s4r
}
