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
//            new SANER().handleData(FeatureOption.ORIGINAL);

//            new Cardumen().handleData(FeatureOption.EXTENDED);
//            new SANER().handleData(FeatureOption.EXTENDED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /*
    Cardumen [original]
    19-03-16 13:10:28 INFO FeatureLearner:138 - 5-fold Cross Validation: 0.5518005017720364
    Cardumen [extended]
    //
    SANER [original]
    //
    SANER [extended]
    //
     */
    /* todo (improve results)
        1. improve Feature (extended version)
        2. improve FeatureCross (enhanced version)
        3. improve learner algorithm
     */
    // todo (try on other candidate-patches generators)
    // todo (draw graphs on 1k commits for Martin)
    // todo (integrate Coming and Prophet4J)
    // todo (integrate with Repairnator) ?
}