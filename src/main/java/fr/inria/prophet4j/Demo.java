package fr.inria.prophet4j;

import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.utility.dataport.Cardumen;
//import fr.inria.prophet4j.utility.dataport.PGA;
//import fr.inria.prophet4j.utility.dataport.SANER;

public class Demo {
    public static void main(String[] args) {
        final boolean doShuffle = false;
        final FeatureOption featureOption = FeatureOption.ORIGINAL;
        try {
            // generate .csv files for HeYE
//            new Cardumen().generateCSV(featureOption);
            // handle ideal patches from kth-tcs/overfitting-analysis
//            new Cardumen().handleData(doShuffle, featureOption);
            // real commits from Git files (how to filter out functional changes from revision changes?)
//            new PGA().handleCommits(doShuffle, featureOption);
            // handle diff files from monperrus/bug-fixes-saner16
//            new SANER().handleData(doShuffle, featureOption);

            // improve 1 features & 3 learner algorithm
            // baseline (original)
            //
            //
            // baseline (extended)
            //
            //
            new Cardumen().handleData(doShuffle, FeatureOption.ORIGINAL);
            new Cardumen().handleData(doShuffle, FeatureOption.EXTENDED);

            // improve 2 candidates patches generator
            // baseline (original)
            //
            //
            // baseline (extended)
            //
            //
//            new SANER().handleData(doShuffle, FeatureOption.ORIGINAL);
//            new SANER().handleData(doShuffle, FeatureOption.EXTENDED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /* todo: improve results
        1. consider features which are more expressive
        2. improve the way of generating candidates patches
        3. improve the learner algorithm
     */
    // todo: draw graphs on 1k commits for Martin
    // todo: the plan for integrating Coming and Prophet4J
}