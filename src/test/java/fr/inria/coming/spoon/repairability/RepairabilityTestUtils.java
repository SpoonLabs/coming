package fr.inria.coming.spoon.repairability;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import fr.inria.coming.spoon.repairability.checkers.DiffResultChecker;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepairabilityTestUtils {
    public static void checkNumberOfRepairInstances(FinalResult result, int totalInputs, int foundInstances) {
        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(totalInputs, revisionsMap.keySet().size());

        int counter = countNumberOfInstances(revisionsMap, RepairabilityAnalyzer.class);

        assertEquals(foundInstances, counter);
    }

    public static int countNumberOfInstances(Map<IRevision, RevisionResult> revisionsMap, Class analyzer) {
        int counter = 0;
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances =
                    (PatternInstancesFromRevision) rr.getResultFromClass(analyzer);
            counter += instances.getInfoPerDiff().stream().mapToInt(v -> v.getInstances().size()).sum();
        }
        return counter;
    }


    public static FinalResult runRepairability(String toolName, String inputFiles) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-input",
                        "files",
                        "-location",
                        URLDecoder.decode(RepairabilityTestUtils.class.getResource(inputFiles).getFile(), "UTF-8")});

        assertNotNull(result);
        return result;
    }

    public static FinalResult runRepairabilityWithParameters
            (
                    String toolName,
                    String inputFiles,
                    String parameters // ex. "include_all_instances_for_each_tool:true:X:false:Y:true"
            ) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-input",
                        "files",
                        "-parameters",
                        parameters,
                        "-location",
                        URLDecoder.decode(RepairabilityTestUtils.class.getResource(inputFiles).getFile(), "UTF-8")});

        assertNotNull(result);
        return result;
    }

    public static FinalResult runRepairabilityGit(String toolName, String inputFiles) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-input",
                        "git",
                        "-location",
                        inputFiles});

        assertNotNull(result);
        return result;
    }

    public static void checkGroundTruthPatches(Class toolTestClass, DiffResultChecker diffResultChecker) throws Exception {
        String toolName = toolTestClass.getSimpleName().replace("Test", "");
        String groundTruthPatchesPathInResources = "/repairability_test_files/ground_truth/" + toolName;
        String groundTruthPatchesBasePath =
                URLDecoder.decode(RepairabilityTestUtils.class.getResource
                        (groundTruthPatchesPathInResources).getFile(), "UTF-8");

        List<String> detectedInstances = new ArrayList<>(),
                undetectedInstances = new ArrayList<>(),
                overDetectedInstances = new ArrayList<>(); // diffs with more than one detected instances

        File[] files = new File(groundTruthPatchesBasePath).listFiles();
        for (File file : files) {
            FinalResult result =
                    RepairabilityTestUtils.runRepairabilityWithParameters
                            (
                                    toolName,
                                    groundTruthPatchesPathInResources + File.separator + file.getName(),
                                    "include_all_instances_for_each_tool:true:exclude_repair_patterns_not_covering_the_whole_diff:true"
                            );

            if (!diffResultChecker.isDiffResultCorrect(result)) {
                // Gumtree did not work correctly
                continue;
            }

            int numberOfRepairInstances = RepairabilityTestUtils.countNumberOfInstances(
                    result.getAllResults(), RepairabilityAnalyzer.class);

            if (numberOfRepairInstances > 1) {
                overDetectedInstances.add(file.getName());
            } else if (numberOfRepairInstances < 1) {
                undetectedInstances.add(file.getName());
            } else {
                detectedInstances.add(file.getName());
            }
        }

        assertEquals(overDetectedInstances.size(), 0);

        assertEquals(undetectedInstances.size(), 49);
        /* legitimate causes for undetectedInstances:
        1- ingredient (variable or literal) is extracted from a different scope (not from the same file).
        2- template is extracted from from a different scope (not from the same file).
        */
    }
}
