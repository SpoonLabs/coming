package fr.inria.prophet4j;

import fr.inria.prophet4j.dataset.DataManager;
import fr.inria.prophet4j.defined.FeatureLearner;
import fr.inria.prophet4j.defined.RepairEvaluator;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;

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
        repairEvaluator.func4Demo();
    }

    public static void main(String[] args) {
        try {
            Option option = new Option();
//            new Demo(option).learn();
//            new Demo(option).evaluate();

            option.patchOption = PatchOption.SPR;
//            new Demo(option).learn();
//            new Demo(option).evaluate();

            option.dataOption = DataOption.SANER;
//            new Demo(option).learn();
//            new Demo(option).evaluate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    [original] Cardumen + Cardumen
    19-03-24 12:59:17 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.2716190501574554
    [original] Cardumen + SPR
    // i found NaN todo check
    [original] SANER + SPR
    19-03-24 12:48:47 INFO FeatureLearner:222 - 5-fold Cross Validation: 0.008142896242219158
     */
    // todo (improve Feature or FeatureCross)
    // todo (try on other candidate-patches generators)
    // todo (draw graphs on 1k commits for Martin)
    // todo (integrate Coming and Prophet4J)
    // todo (integrate with Repairnator)
}