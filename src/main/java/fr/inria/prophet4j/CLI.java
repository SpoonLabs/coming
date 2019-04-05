package fr.inria.prophet4j;

import picocli.CommandLine;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.FeatureOption;

/*
To be able to select different feature sets, eg
./coming -f prophet4j:sketch4repair foo.git
./coming -f prophet4j foo.git

To be able to output the learned probability model:
./coming --output-prob-model prob.json -f prophet4j foo.git

And then one would be able to predict the likelihood of a new patch
./prophet-predictor --prob-model prob.json --patch bar.patch
 */
/*
./prophet4j -t task -d dataOption -p patchOption -f featureOption
 */
@CommandLine.Command(
        version = "1.0",
        name = "Prophet4J",
        footer = "https://github.com/SpoonLabs/coming",
        description = "Evaluate the correctness probability of patch (by learning existing patches)"
)
public class CLI {

    private enum Task {
        LEARN,
        EVALUATE,
    }

    private final String commonInfo = "\ndefault value: ${DEFAULT-VALUE}\nvalid values: ${COMPLETION-CANDIDATES}";

    @CommandLine.Option(
            names = {"--help"},
            usageHelp = true,
            description = "display usage info"
    )
    private boolean displayUsageInfo = false;

    @CommandLine.Option(
            names = {"--version"},
            versionHelp = true,
            description = "display version info"
    )
    private boolean displayVersionInfo = false;

    @CommandLine.Option(
            names = {"-t", "--task"},
            description = "Task" + commonInfo
    )
    private Task task = Task.LEARN;

    @CommandLine.Option(
            names = {"-d", "--data-option"},
            description = "Data Option" + commonInfo
    )
    private DataOption dataOption = DataOption.CARDUMEN;

    @CommandLine.Option(
            names = {"-p", "--patch-option"},
            description = "Patch Option" + commonInfo
    )
    private PatchOption patchOption = PatchOption.CARDUMEN;

    @CommandLine.Option(
            names = {"-f", "--feature-option"},
            description = "Feature Option" + commonInfo
    )
    private FeatureOption featureOption = FeatureOption.ORIGINAL;

    public static void main(String[] args) {
        CLI cli = new CLI();

        CommandLine commandLine = new CommandLine(cli);
        commandLine.parse(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        Option option = new Option();
        option.dataOption = cli.dataOption;
        option.patchOption = cli.patchOption;
        option.featureOption = cli.featureOption;
        switch (cli.task) {
            case LEARN:
                new Demo(option).learn();
                break;
            case EVALUATE:
                new Demo(option).evaluate();
                break;
        }
    }
}
