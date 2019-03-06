package prophet4j;

import prophet4j.port.Cardumen;
import prophet4j.port.PGA;
import prophet4j.port.SANER;

public class Demo {
    public static void main(String[] args) {
        try {
            // real commits from Git files (how to filter out functional changes from revision changes?)
//            new PGA().handleCommits();
            // handle diff files from monperrus/bug-fixes-saner16
            new SANER().handleData();
            // handle ideal patches from kth-tcs/overfitting-analysis
//            new Cardumen().handleData();
            // generate .csv files for HeYE
//            new Cardumen().generateCSV();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    // todo: run FeatureLearner on SANER data & debug
    // todo: run FeatureLearner on PGA data & debug
    // todo: draw graphs on 1k commits for Martin
    // todo: the plan for integrating Coming and Prophet4J
    /*
    To be able to select different feature sets, eg
    ./coming -f prophet4j:sketch4repair foo.git
    ./coming -f prophet4j foo.git

    To be able to output the learned probability model:
    ./coming --output-prob-model prob.json -f prophet4j foo.git

    And then one would be able to predict the likelihood of a new patch
    ./prophet-predictor --prob-model prob.json --patch bar.patch
     */
    /* todo: future plan to improve performance
        1. design other features or feature-vectors
        2. improve the way of generating candidates patches
     */
}