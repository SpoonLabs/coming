package fr.inria.prophet4j.command;

/*
To be able to select different feature sets, eg
./coming -f prophet4j:sketch4repair foo.git
./coming -f prophet4j foo.git

To be able to output the learned probability model:
./coming --output-prob-model prob.json -f prophet4j foo.git

And then one would be able to predict the likelihood of a new patch
./prophet-predictor --prob-model prob.json --patch bar.patch
 */
public class CLI {
    // maybe complete this later

}
