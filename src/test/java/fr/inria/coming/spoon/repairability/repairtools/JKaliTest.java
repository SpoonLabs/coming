package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import org.junit.Test;

public class JKaliTest {
    @Test
    public void JKaliTest() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JKali", "/repairability_test_files/JKali/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 5, 5);
    }


}
