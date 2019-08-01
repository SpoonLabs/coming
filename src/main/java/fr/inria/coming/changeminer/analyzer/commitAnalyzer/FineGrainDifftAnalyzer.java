package fr.inria.coming.changeminer.analyzer.commitAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DiffException;
import com.github.difflib.patch.Patch;
import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.analyzer.DiffEngineFacade;
import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * Commit analyzer: It searches fine grain changes.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class FineGrainDifftAnalyzer implements Analyzer<IRevision> {

    Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());
    DiffEngineFacade cdiff = new DiffEngineFacade();

    protected GranuralityType granularity;

    /**
     *
     */
    public FineGrainDifftAnalyzer() {
        granularity = GranuralityType.valueOf(ComingProperties.getProperty("GRANULARITY"));
    }

    /**
     * Analyze a commit finding instances of changes return a Map<FileCommit, List>
     */
    public AnalysisResult<IRevision> analyze(IRevision revision) {

        List<IRevisionPair> javaFiles = revision.getChildren();

        Map<String, Diff> diffOfFiles = new HashMap<>();

        log.info("\n*****\nCommit: " + revision.getName());


        for (IRevisionPair<String> fileFromRevision : javaFiles) {

            String left = fileFromRevision.getPreviousVersion();
            String right = fileFromRevision.getNextVersion();

            String leftName = fileFromRevision.getPreviousName();
            String rightName = fileFromRevision.getName();
            System.out.println("In fine grain analyzer...............................................................");


//            System.out.println(leftName);
//            System.out.println(rightName);

            //build simple lists of the lines of the two testfiles
//            List<String> original = null;
//            try {
//                original = Files.readAllLines(new File(leftName).toPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            List<String> revised = null;
//            try {
//                revised = Files.readAllLines(new File(rightName).toPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            Diff diff = compare(left, right, leftName, rightName);
            if (diff != null) {
                diffOfFiles.put(fileFromRevision.getName(), diff);
                //compute the patch: this is the diffutils part
                Patch<String> patch = null;

                try {
                    patch = DiffUtils.diff(Arrays.asList(left), Arrays.asList(right));
                } catch (com.github.difflib.algorithm.DiffException e) {
                    e.printStackTrace();
                }

                System.out.println("patch .............");
                System.out.println(patch.getDeltas());
                System.out.println("end of patch .............");
                break;
            }
            break;
        }

        return new DiffResult<IRevision, Diff>(revision, diffOfFiles);
    }


    @Override
    public AnalysisResult analyze(IRevision input, RevisionResult previousResult) {
        // Not considered the previous results in this analyzer.
        return this.analyze(input);
    }

    public Diff compare(String left, String right) {
        return this.compare(left, right, "leftFile", "rightFile");
    }

    public Diff compare(String left, String right, GranuralityType granularity) {
        return this.compare(left, right, "leftFile", "rightFile");
    }

    public Diff compare(String left, String right, String leftName, String rightName) {
        if (!left.trim().isEmpty()) {

            List<Operation> operations;

            try {

                Diff diff = cdiff.compareContent(left, right, leftName, rightName);

                operations = diff.getRootOperations();

                if (operations == null
                        || operations.size() > ComingProperties.getPropertyInteger("MAX_AST_CHANGES_PER_FILE")
                        || operations.size() < ComingProperties.getPropertyInteger("MIN_AST_CHANGES_PER_FILE")) {
                    log.debug(
                            "FileRevision with Max number of Root AST Changes. Discating it. Total:" + operations.size()
                                    + " max: " + ComingProperties.getPropertyInteger("MAX_AST_CHANGES_PER_FILE"));
                    return null;
                }

                if (operations.size() > 0) {

                    return diff;
                }
            } catch (Exception e) {
                log.error("Exception e: " + e);
                e.printStackTrace();

            }
        }
        return null;
    }

    public Diff getDiff(File left, File right) throws Exception {

        DiffEngineFacade cdiff = new DiffEngineFacade();
        Diff d = cdiff.compareFiles(left, right, GranuralityType.SPOON);
        return d;
    }

    private Future<Diff> getDiffInFuture(ExecutorService executorService, File left, File right) {

        Future<Diff> future = executorService.submit(() -> {
            DiffEngineFacade cdiff = new DiffEngineFacade();
            Diff d = cdiff.compareFiles(left, right, GranuralityType.SPOON);
            return d;
        });
        return future;
    }

    public Diff getdiffFuture(File left, File right) throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Diff> future = getDiffInFuture(executorService, left, right);

        Diff resukltDiff = null;
        try {
            resukltDiff = future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) { // <-- possible error cases
            log.error("job was interrupted");
        } catch (ExecutionException e) {
            log.error("caught exception: " + e.getCause());
        } catch (TimeoutException e) {
            log.error("timeout");
        }

        executorService.shutdown();
        return resukltDiff;

    }

}
