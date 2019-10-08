package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtills;
import org.junit.Test;

public class JKaliTest {
    @Test
    public void JKaliTest() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JKali", "/repairability_test_files/JKali/");
        RepairabilityTestUtills.numberOfInstances(result, 5, 5);
    }


}
