package fr.inria.coming.spoon.repairability;

import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import fr.inria.coming.spoon.utils.TestUtils;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepairabilityTestUtils {
    public static void checkNumberOfRepairInstances(FinalResult result, int totalInputs, int foundInstances) {
        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(totalInputs, revisionsMap.keySet().size());

        int counter = TestUtils.countNumberOfInstances(revisionsMap, RepairabilityAnalyzer.class);

        assertEquals(foundInstances, counter);
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

    public static void checkGroundTruthPatches
            (
                    Class testClass,
                    String patchesFolder,
                    int expectedUndetected,
                    int expectedOverDetected) throws Exception {
        String toolName = testClass.getSimpleName().replace("Test", "");
        String groundTruthPatchesPathInResources = "/repairability_test_files/ground_truth/" + patchesFolder;
        String groundTruthPatchesBasePath =
                URLDecoder.decode(RepairabilityTestUtils.class.getResource
                        (groundTruthPatchesPathInResources).getFile(), "UTF-8");

        List<String> detectedInstances = new ArrayList<>(),
                detectedInstancePatternNames = new ArrayList<>(),
                undetectedInstances = new ArrayList<>(),
                overDetectedInstances = new ArrayList<>(); // diffs with more than one detected instances

        File[] files = new File(groundTruthPatchesBasePath).listFiles();
        for (File file : files) {
//            if (!file.getName().contains("patch1-Math-1115"))
//                continue;
            FinalResult result =
                    RepairabilityTestUtils.runRepairabilityWithParameters
                            (
                                    toolName,
                                    groundTruthPatchesPathInResources + File.separator + file.getName(),
                                    "include_all_instances_for_each_tool:true:exclude_repair_patterns_not_covering_the_whole_diff:true"
                            );

            int numberOfRepairInstances = TestUtils.countNumberOfUniqueInstances(
                    result.getAllResults(), RepairabilityAnalyzer.class);

            if (numberOfRepairInstances > 1) {
                overDetectedInstances.add(file.getName());
            } else if (numberOfRepairInstances < 1) {
                undetectedInstances.add(file.getName());
            } else {
                detectedInstances.add(file.getName());
                detectedInstancePatternNames.add(
                        ((PatternInstancesFromRevision) ((RevisionResult) ((Map.Entry<Object, Object>) result.entrySet().toArray()[0]).getValue())
                                .get("RepairabilityAnalyzer")).getInfoPerDiff().get(0).getInstances().get(0).getPattern().getName()
                );
            }
        }

        assertEquals(expectedOverDetected, overDetectedInstances.size());
        assertEquals(expectedUndetected, undetectedInstances.size());
        /* legitimate causes for undetectedInstances:
        1- ingredient (variable or literal) is extracted from a different scope (not from the same file).
        2- template is extracted from from a different scope (not from the same file).
        */
    }
}