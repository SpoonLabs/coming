package prophet4j.unused;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import prophet4j.defined.FeatureStruct.*;

import java.io.IOException;

// based on Main.cpp
public class Main {
    // todo: test cli
    // if no init value, default values for String should be "", for boolean should be false, for int should be 0 ... (I guess)
    @Parameters(index = "0", paramLabel = "conf-filename", description = "Specify the configure filename")
    private String ConfigFilename; // Positional
    @Option(names = {"--log"}, paramLabel = "log-filename", description = "Specify the logfile for this run!")
    private String LogFileName = "repair.log";
    @Option(names = {"-r", "--run"}, paramLabel = "work dir", description = "Run with particular work dir, if it exists, it will resume the execution.")
    private String RunWorkDir = "";

    @Option(names = {"--skip-profile-build"}, description = "Skip To Build Profiler")
    private boolean SkipProfileBuild = false;
    @Option(names = {"--no-clean-up"}, description = "Do not clean work dir after finish")
    private boolean NoCleanUp = false;
    @Option(names = {"--init-only"}, description = "Just initialize the work directory verify + localization!")
    private boolean InitOnly = false;
    @Option(names = {"--skip-verify"}, description = "Skip verify the work directory with test cases!")
    private boolean SkipVerify = false;
    @Option(names = {"--naive"}, description = "Run naive repair that only inserts return/ delete statements/branches!")
    private boolean NaiveRepair = false;
    // todo: the below two should be exclusive, so how to ... ?
    @Option(names = {"--print-fix-only"}, paramLabel = "output-file", description = "Do not test and generate repair, print all fix candidates only")
    private String PrintFixOnly = "";
    @Option(names = {"--print-loc-only"}, paramLabel = "output-file", description = "Only run error localization and print candidate locations")
    private String PrintLocalizationOnly = "";

    @Option(names = {"--try-at-least"}, description = "The number of mutations we try before we terminate.")
    private int TryAtLeast = 0;
    @Option(names = {"--full-explore"}, description = "Terminate only when we tried everything in the search space.")
    private boolean FullExplore = false;
    @Option(names = {"--full-synthesis"}, description = "Try all conditions instead of choosing the first one.")
    private boolean FullSynthesis = false;
    @Option(names = {"--summary-file"}, description = "Output the list of generated patch id and scores to the file, sorted!")
    private String SummaryFile = "";
    @Option(names = {"--consider-all"}, description = "Consider all possible files.")
    private boolean ConsiderAll = false;
    @Option(names = {"--first-n-loc"}, description = "Consider first n location, default 5000.")
    private int FirstNLoc = 5000;
    @Option(names = {"--feature-para"}, paramLabel = "feature-parameter-file", description = "Specify the feature parameter file. Disable learning if not specified.")
    private String ParameterFile = "";
    @Option(names = {"--no-feature"}, paramLabel = "just-use-localization-distribution", description = "Rank candidates with only localization distribution!")
    private boolean NoFeature = false;
    @Option(names = {"--random"}, description = "Just use random search over the space.")
    private boolean Random = false;
    @Option(names = {"--geop"}, paramLabel = "flip probability", description = "Flip Probability of Geometric Computations!")
    private double GeoP = 0.02;
    @Option(names = {"--timeout"}, description = "Soft timeout limit in hours")
    private int Timeout = 0;

    // these options will be removed
    @Option(names = {"--vl"}, paramLabel = "verbose-level", description = "How many info will output to screen 0-10(0min-10max).")
    private int VerboseLevel = 2;
    @Option(names = {"--ll"}, paramLabel = "log-level", description = "How many info will output to log 0-10(0min-10max).")
    private int LogLevel = 2;

    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    // .java .patch
    public static void main(String[] args) {
        // it is not good to have this variable name
        Main main = new Main();
        new CommandLine(main).parse(args);

        // todo: set log file for logger, namely unused.LogFileName
        // todo: replace with Timer (mainly for timeout check in RepairSearchEngine)
//        reset_timer();

        if ((main.ConfigFilename.equals("")) && (main.RunWorkDir.equals(""))) {
            // see https://logging.apache.org/log4j/2.x/manual/customloglevels.html
            logger.log(Level.FATAL, "Must specify configuration file or existing working directory!");
            System.exit(1);
        }

        logger.log(Level.INFO, "Initialize the program!\n");
        BenchProgram P;
        if (!main.ConfigFilename.equals("")) {
            // parameters list for this construct function could be checked to simplify
            P = new BenchProgram(main.ConfigFilename, main.RunWorkDir, main.InitOnly || main.NoCleanUp || !main.RunWorkDir.equals(""));
            if (!main.RunWorkDir.equals("")) {
                String cmd = "cp -f " + main.ConfigFilename + " " + main.RunWorkDir + "/repair.conf";
                try {
                    Process process = Runtime.getRuntime().exec(cmd);
                    int status = process.waitFor();
                    assert (status == 0);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else
            P = new BenchProgram(main.RunWorkDir);

        if (!main.SkipVerify) {
            logger.log(Level.INFO, "Verify Test Cases\n");
            boolean ret = P.verifyTestCases();
            if (!ret) {
                logger.log(Level.INFO, "Repair error: Verification failed!\n");
                System.exit(1);
            }
            logger.log(Level.INFO, "Done Verification\n");
        }

        ConfigFile config = P.getCurrentConfig();
        String localizer = config.getStr("localizer");
        String[] bugged_file = config.getStr("bugged_file").split("\\s+");

        // todo: LOCALIZE (check spoonlibs/mopol)
        /*
        ErrorLocalizer L = null;
        if (localizer == null)
            L = new ErrorLocalizer.NaiveErrorLocalizer(P);
        else if (localizer.equals("profile")) {
            if (existFile(P -> getLocalizationResultFilename()))
                L = new ErrorLocalizer.ProfileErrorLocalizer(P, P -> getLocalizationResultFilename());
            else if (unused.SkipProfileBuild)
                L = new ErrorLocalizer.ProfileErrorLocalizer(P, bugged_file, true);
            else
                L = new ErrorLocalizer.ProfileErrorLocalizer(P, bugged_file, false);
        }
        if (unused.InitOnly) {
            System.exit(0);
        }
        if (!unused.PrintLocalizationOnly.equals("")) {
            L->printResult(unused.PrintLocalizationOnly);
            System.exit(0);
        } */

        // EXTRACT
        ParameterVector FP = null;
        boolean learning = false;
        if (!main.ParameterFile.equals("") || main.NoFeature) {
            FP = new ParameterVector();
            if (!main.NoFeature) {
                // read in offline learning result, namely ParameterVector
//                std::ifstream fin(ParameterFile.getValue(), std::ifstream::in);
//                if (fin.is_open()) {
//                    fin >> *FP;
//                    fin.close();
//                }
            }
            learning = true;
        }

        // todo: REPAIR
        /*
        RepairSearchEngine E = new RepairSearchEngine(P, L, unused.NaiveRepair, learning, FP);
        if (!unused.ConsiderAll)
            E.setBuggedFile(bugged_file);
        E.setLocLimit(unused.FirstNLoc);
        E.setGeoP(unused.GeoP);
        E.setRandom(unused.Random);
        E.setSummaryFile(unused.SummaryFile);
        if (unused.Timeout != 0)
            E.setTimeoutLimit((unused.Timeout) * 60 * 60);

        int ret;
        boolean fix_only = false;
        if (!unused.PrintFixOnly.equals(""))
            fix_only = true;
        if (fix_only) {
            ret = E.run(unused.PrintFixOnly, 0, fix_only, false);
        } else {
            String fixed_out_file = config -> getStr("fixed_out_file");
            if (fixed_out_file.equals(""))
                fixed_out_file = "__fixed_";

            if (unused.FullExplore)
                ret = E.run(fixed_out_file, 1000000000, false, unused.FullSynthesis);
            else
                ret = E.run(fixed_out_file, unused.TryAtLeast, false, unused.FullSynthesis);
        }
        System.exit(ret); */
    }
}
