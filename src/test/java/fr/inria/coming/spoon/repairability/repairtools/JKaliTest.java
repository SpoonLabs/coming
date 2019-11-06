package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import fr.inria.coming.spoon.repairability.checkers.impl.DefaultDiffResultChecker;
import org.junit.Test;

public class JKaliTest {
    @Test
    public void JKaliTest() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JKali", "/repairability_test_files/JKali/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 5, 5);
    }

    @Test
    public void testGroundTruthCreatedPatches() throws Exception {
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(),"jKali", new DefaultDiffResultChecker(),
                6, 0, 0);
    }
}
