package fr.inria.prophet4j;

import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.utility.dataport.Cardumen;
//import fr.inria.prophet4j.utility.dataport.PGA;
import fr.inria.prophet4j.utility.dataport.SANER;

public class Demo {
    public static void main(String[] args) {
        try {
//            new Cardumen().generateCSV(FeatureOption.ORIGINAL);
//            new Cardumen().handleData(FeatureOption.ORIGINAL);
//            new PGA().handleCommits(FeatureOption.ORIGINAL);
//            new SANER().handleData(FeatureOption.ORIGINAL);

            new Cardumen().handleData(FeatureOption.EXTENDED);
            new SANER().handleData(FeatureOption.EXTENDED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    [original] Cardumen
    19-03-18 18:19:18 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.547542176751451
    [original] SANER
    19-03-18 18:31:12 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.37790482926831814
    [extended] Cardumen
    19-03-18 18:34:33 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.5543498117663634
    [extended] SANER
    19-03-18 18:49:28 INFO FeatureLearner:181 - 5-fold Cross Validation: 0.38709105299340946
     */
    // todo 1. improve Feature (extended version) this week?
    // todo 2. improve FeatureCross (enhanced version) next week?
    // todo 3. improve learner algorithm (or implement other models?) when?
    // todo (try on other candidate-patches generators)
    // todo (draw graphs on 1k commits for Martin)
    // todo (integrate Coming and Prophet4J)
    // todo (integrate with Repairnator)
}