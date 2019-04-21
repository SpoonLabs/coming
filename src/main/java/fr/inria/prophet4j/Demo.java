package fr.inria.prophet4j;

import fr.inria.prophet4j.dataset.DataManager;
import fr.inria.prophet4j.defined.FeatureLearner;
import fr.inria.prophet4j.defined.RepairEvaluator;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.RankingOption;

import java.util.List;

public class Demo {
    private Option option;

    public Demo(Option option) {
        this.option = option;
    }

    void learn() {
        DataManager dataManager = new DataManager(option);
        FeatureLearner featureLearner = new FeatureLearner(option);
        List<String> filePaths = dataManager.func4Demo();
        featureLearner.func4Demo(filePaths);
        System.out.println("1/1 Done");
    }

    void evaluate() {
        RepairEvaluator repairEvaluator = new RepairEvaluator(option);
//        repairEvaluator.func4Demo(RankingOption.D_CORRECT, RankingOption.D_INCORRECT);
//        System.out.println("1/3 Done");
//        repairEvaluator.func4Demo(RankingOption.D_HUMAN, RankingOption.D_CORRECT);
//        System.out.println("2/3 Done");
//        repairEvaluator.func4Demo(RankingOption.D_HUMAN, RankingOption.D_INCORRECT);
//        System.out.println("3/3 Done");
        repairEvaluator.func4Demo(RankingOption.D_HUMAN, RankingOption.D_INCORRECT);
        System.out.println("1/1 Done");
    }

    private static void jobs4Demo(Option option) {
        option.dataOption = DataOption.CARDUMEN;
        option.patchOption = PatchOption.CARDUMEN;
        new Demo(option).learn();
        new Demo(option).evaluate();
        option.dataOption = DataOption.SANER;
        option.patchOption = PatchOption.SPR;
        new Demo(option).learn();
        new Demo(option).evaluate();
    }

    private static void jobs4ODS(Option option) {
//        try {
//            option.dataOption = DataOption.BEARS;
//            option.patchOption = PatchOption.BEARS;
//            new Demo(option).learn();
//            new Demo(option).evaluate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            option.dataOption = DataOption.BUG_DOT_JAR;
            option.patchOption = PatchOption.BUG_DOT_JAR;
            new Demo(option).learn();
            new Demo(option).evaluate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            option.dataOption = DataOption.BUG_DOT_JAR;
            option.patchOption = PatchOption.SPR;
            new Demo(option).learn();
            new Demo(option).evaluate();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            option.dataOption = DataOption.DEFECTS4J;
//            option.patchOption = PatchOption.DEFECTS4J;
//            new Demo(option).learn();
//            new Demo(option).evaluate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            option.dataOption = DataOption.QUIX_BUGS;
//            option.patchOption = PatchOption.QUIX_BUGS;
//            new Demo(option).learn();
//            new Demo(option).evaluate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    // seem meaningless to use FeatureOption.ENHANCED
    public static void main(String[] args) {
        try {
            Option option = new Option();
            // for Demo
            option.featureOption = FeatureOption.ORIGINAL;
            jobs4Demo(option);
            option.featureOption = FeatureOption.EXTENDED;
            jobs4Demo(option);
//            option.featureOption = FeatureOption.ENHANCED;
//            jobs4Demo(option);
            // for ODS
            option.featureOption = FeatureOption.ORIGINAL;
            jobs4ODS(option);
            option.featureOption = FeatureOption.EXTENDED;
            jobs4ODS(option);
//            option.featureOption = FeatureOption.ENHANCED;
//            jobs4ODS(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Testing(D_HUMAN-D_INCORRECT)
    // o16 e20 Training(CARDUMEN-CARDUMEN)
    // o21 e24 Training(SANER-SPR)
    // o30 e32 Training(BUG_DOT_JAR-REPAIR_THEM_ALL)
    // o17 e19 Training(BUG_DOT_JAR-SPR)
    // todo (try Features in sketch4repair or coming)
    // todo (integrate with Coming)
    // todo (integrate with Repairnator)
    // todo (run on PGA commits and try other patch-generators) ?
    /*
    if we need to improve the performance of FeatureLearner, use CLR(Cyclical Learning Rates)
    first line corresponds original eta, second line corresponds CLR
    // o16 e21 original
    // o19 e23 original
    // o20 e24 extended
    // o20 e25 extended
    // o16 e30 enhanced (appended POS_VF_RF_CT POS_VF_AF_CT AF_RF_CT VF_RF_CT)
    // o17 e24 enhanced (appended POS_VF_RF_CT POS_VF_AF_CT AF_RF_CT VF_RF_CT)
     */
    // config VM: -Xms1024m -Xmx16384m
}
