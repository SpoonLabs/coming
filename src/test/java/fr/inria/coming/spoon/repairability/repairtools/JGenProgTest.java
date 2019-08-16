package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

/**
 * TODO: Add more than one class in a file test: patch4-Chart-15-JGenProg2017
 */
public class JGenProgTest {
    @Test
    public void testJGenProgNegative() throws Exception {
        FinalResult result = TestUtills.runRepairability("JGenProg", "/repairability_test_files/JGenProgTest");
        TestUtills.numberOfInstances(result, 1, 0);
    }

    @Test
    public void testJGenProgPositive() throws Exception {
        FinalResult result = TestUtills.runRepairability("JGenProg", "/repairability_test_files/JGenProgPostiveTest");
        TestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJGenProgonArjadata() throws Exception {
        FinalResult result = TestUtills.runRepairability("JGenProg", "/repairability_test_files/arja");
        TestUtills.numberOfInstances(result, 11, 10);
    }


}
