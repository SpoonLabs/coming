package fr.inria.prophet4j;

import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.utility.dataport.Cardumen;
//import fr.inria.prophet4j.utility.dataport.PGA;
import fr.inria.prophet4j.utility.dataport.SANER;

public class Demo {
    public static void main(String[] args) {
        try {
//            new Cardumen().generateCSV(FeatureOption.ORIGINAL);
            new Cardumen().handleData(FeatureOption.ORIGINAL);
//            new PGA().handleCommits(FeatureOption.ORIGINAL);
            new SANER().handleData(FeatureOption.ORIGINAL);

//            new Cardumen().handleData(FeatureOption.EXTENDED);
//            new SANER().handleData(FeatureOption.EXTENDED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    [original] Cardumen
    19-03-19 12:09:37 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.17570800656099123
    [original] SANER
    19-03-19 12:39:54 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.011003139162889544
    [extended] Cardumen
    19-03-19 12:45:08 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.16407816084612153
    [extended] SANER
    //
     */
    // todo 1. improve Feature (extended version)
    // todo 2. improve FeatureCross (enhanced version)
    // todo 3. improve learner algorithm (or implement other models?)
    // todo (try on other candidate-patches generators)
    // todo (draw graphs on 1k commits for Martin)
    // todo (integrate Coming and Prophet4J)
    // todo (integrate with Repairnator)
}