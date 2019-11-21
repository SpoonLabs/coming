package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import org.junit.Test;

/**
 * TODO: Add more than one class in a file test: patch4-Chart-15-JGenProg2017
 */
public class JGenProgTest {
    @Test
    public void testJGenProgNegative() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JGenProg", "/repairability_test_files/JGenProgTest");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 0);
    }

    @Test
    public void testJGenProgPositive() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JGenProg", "/repairability_test_files/JGenProgPostiveTest");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testGroundTruthCreatedPatches() throws Exception {
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(),"JGenProg2015",
                1, 0);
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(),"JGenProg2017",
                52, 0);
    }
}
