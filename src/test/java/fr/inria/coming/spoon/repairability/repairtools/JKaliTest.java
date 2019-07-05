package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class JKaliTest {
    @Test
    public void testJKali1() throws Exception {
        FinalResult result = TestUtills.runRepairability("JKaliTest", "/repairability_test_files/JKali/");
        TestUtills.numberOfInstances(result, 5, 5);
    }


}
