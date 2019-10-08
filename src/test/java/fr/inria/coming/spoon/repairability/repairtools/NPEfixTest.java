package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtills;
import org.junit.Test;

public class NPEfixTest {

    @Test
    public void NPEfixTest() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("NPEfix", "/repairability_test_files/NPEfix/");
        RepairabilityTestUtills.numberOfInstances(result, 13, 11);

        //18 is FN
    }

}
