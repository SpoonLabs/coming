package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import org.junit.Test;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

/**
 * Created by khesoem on 10/14/2019.
 */
public class CardumenTest {

    @Test
    public void testCardumen1() throws Exception {
        FinalResult result =
                RepairabilityTestUtils.runRepairabilityWithParameters
                        (
                                "Cardumen",
                                "/repairability_test_files/cardumen/cardumen1",
                                "exclude_repair_patterns_not_covering_the_whole_diff:true"
                        );
        RepairabilityTestUtils.checkNumberOfInstances(result, 1, 1);
    }

    @Test
    public void testCardumen2() throws Exception {
        FinalResult result =
                RepairabilityTestUtils.runRepairabilityWithParameters
                        (
                                "Cardumen",
                                "/repairability_test_files/cardumen/cardumen2",
                                "exclude_repair_patterns_not_covering_the_whole_diff:true"
                        );
        RepairabilityTestUtils.checkNumberOfInstances(result, 1, 0);
    }

    @Test
    public void testCardumen3() throws Exception {
        FinalResult result =
                RepairabilityTestUtils.runRepairabilityWithParameters
                        (
                                "Cardumen",
                                "/repairability_test_files/cardumen/cardumen2",
                                "include_all_instances_for_each_tool:true"
                        );
        RepairabilityTestUtils.checkNumberOfInstances(result, 1, 4);
    }

    @Test
    public void testGroundTruthCreatedPatches() throws Exception {
        String toolName = getClass().getSimpleName().replace("Test", "");
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

            int notUpdateRootOps = RepairabilityTestUtils.countRootOperationsExcludingType(result, "UPD");
            if (notUpdateRootOps > 0) {
                // Gumtree did not work correctly and found 'del' or 'ins' operations instead of 'upd'.
                continue;
            }

            int numberOfInstances = RepairabilityTestUtils.countNumberOfInstances(result.getAllResults());

            if (numberOfInstances > 1) {
                overDetectedInstances.add(file.getName());
            } else if (numberOfInstances < 1) {
                undetectedInstances.add(file.getName());
            } else {
                detectedInstances.add(file.getName());
            }
        }

        assertEquals(overDetectedInstances.size(), 0);

        assertEquals(undetectedInstances.size(), 49);
        /* legitimate causes for undetectedInstances:
        1- ingredient is extracted from a different scope (not from the same file).
        2- template is extracted from from a different scope (not from the same file).
        */
    }

}
