package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class NPEfixTest {

    @Test
    public void NPEfixTest() throws Exception {
        FinalResult result = TestUtills.runRepairability("NPEfix", "/repairability_test_files/NPEfix/");
        TestUtills.numberOfInstances(result, 12, 11);

        //18 is FN
    }

}