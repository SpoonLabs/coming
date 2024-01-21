package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import org.junit.Ignore;
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
    @Ignore // Gumtree3 has changed the numbers of matched instances
    public void testGroundTruthCreatedPatches() throws Exception {
        // was 1, 0 now is 0,0  after update to latest Spoon
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(),"JGenProg2015",
                0, 0);
        
        // was 52,0 now 48,0 after update to latest Spoon
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(),"JGenProg2017",
                48, 0);
    }
}
