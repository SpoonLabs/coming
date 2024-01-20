package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import fr.inria.coming.spoon.repairability.RepairabilityTest;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import fr.inria.coming.spoon.utils.TestUtils;
import org.junit.Ignore;
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
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
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
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 0);
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
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 4);
    }

    @Test
    @Ignore // Gumtree3 has changed the numbers of matched instances
    public void testGroundTruthCreatedPatches() throws Exception {
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "Cardumen",
                167, 0);
    }

}
