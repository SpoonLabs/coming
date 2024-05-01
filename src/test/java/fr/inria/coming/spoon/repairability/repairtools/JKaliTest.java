package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import org.junit.Ignore;
import org.junit.Test;

public class JKaliTest {
    @Test
    public void JKaliTest() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JKali", "/repairability_test_files/JKali/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 5, 5);
    }

    @Test
    @Ignore // Gumtree3 has changed the numbers of matched instances
    public void testGroundTruthCreatedPatches() throws Exception {
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(),"jKali",
                6, 0);
    }
}
