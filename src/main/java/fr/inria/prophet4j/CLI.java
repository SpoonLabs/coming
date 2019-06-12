package fr.inria.prophet4j;

import picocli.CommandLine;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.FeatureOption;

/*
how to build coming.jar
mvn install:install-file -Dfile=lib/gumtree-spoon-ast-diff-0.0.3-SNAPSHOT-jar-with-dependencies.jar -DgeneratePom=true -DgroupId=fr.inria.spirals -DartifactId=gumtree-spoon-ast-diff -Dversion=0.0.3-SNAPSHOT -Dpackaging=jar
mvn -Dskiptest package
how to run Prophet4J
java -classpath coming-0.1-SNAPSHOT-jar-with-dependencies.jar fr.inria.prophet4j.CLI
 */
@CommandLine.Command(
        version = "0.1",
        name = "Prophet4J",
        footer = "https://github.com/SpoonLabs/coming",
        description = "Evaluate the correctness probability of patch (by learning existing patches)"
)

public class CLI {

    private enum Task {
        EXTRACT,
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
    private Task task = Task.EXTRACT;

    @CommandLine.Option(
            names = {"-d", "--data-option"},
            description = "Data Option" + commonInfo
    )
    private DataOption dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;

    @CommandLine.Option(
            names = {"-p", "--patch-option"},
            description = "Patch Option" + commonInfo
    )
    private PatchOption patchOption = PatchOption.BUG_DOT_JAR_MINUS_MATH;

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

        // check the validity
        if (option.dataOption == DataOption.SANER) {
            if (option.patchOption != PatchOption.SPR) {
                System.out.println("dataOption=SANER is valid only when patchOption=SPR");
                return;
            }
        }
        if (option.patchOption == PatchOption.SPR) {
            if (option.featureOption == FeatureOption.S4R || option.featureOption == FeatureOption.S4RO) {
                System.out.println("patchOption=SPR is valid except when featureOption=S4R/S4RO");
            }
        }

        System.out.println(option);
        System.out.println("TASK: " + cli.task.name());

        switch (cli.task) {
            case EXTRACT:
                new Demo(option).extract();
                break;
            case LEARN:
                new Demo(option).learn();
                break;
            case EVALUATE:
                new Demo(option).evaluate();
                break;
        }
    }
}
