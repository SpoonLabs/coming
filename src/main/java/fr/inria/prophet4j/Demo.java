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

    private void learn() {
        DataManager dataManager = new DataManager(option);
        FeatureLearner featureLearner = new FeatureLearner(option);
        List<String> filePaths = dataManager.func4Demo();
        featureLearner.func4Demo(filePaths);
    }

    private void evaluate() {
        RepairEvaluator repairEvaluator = new RepairEvaluator(option);
        repairEvaluator.func4Demo(RankingOption.D_CORRECT, RankingOption.D_INCORRECT);
        repairEvaluator.func4Demo(RankingOption.D_HUMAN, RankingOption.D_CORRECT);
        repairEvaluator.func4Demo(RankingOption.D_HUMAN, RankingOption.D_INCORRECT);
    }

    public static void main(String[] args) {
        try {
            Option option = new Option();
//            option.featureOption = FeatureOption.EXTENDED;
            new Demo(option).learn();
            new Demo(option).evaluate();

            option.patchOption = PatchOption.SPR;
            new Demo(option).learn();

            option.dataOption = DataOption.SANER;
            new Demo(option).learn();
            new Demo(option).evaluate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    [original] Cardumen + Cardumen
    19-03-31 09:53:38 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.2657596928390742
    [original] Cardumen + SPR
    19-03-31 10:05:02 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.28822503830686835
    [original] SANER + SPR
    19-03-31 10:26:04 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.008537303389384415
    [extended] Cardumen + Cardumen
    19-03-30 21:13:22 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.2583991587092498
    [extended] Cardumen + SPR
    19-03-30 21:20:57 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.29062909492440814
    [extended] SANER + SPR
    19-03-30 22:12:05 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.010966064135150921
     */
    // todo (improve Feature or FeatureCross)
    // todo (try other patch-generators)
    // todo (run on PGA commits)
    // todo (integrate Coming and Prophet4J)
    // todo (integrate with Repairnator)
}