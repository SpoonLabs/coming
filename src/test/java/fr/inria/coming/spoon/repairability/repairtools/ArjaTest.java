package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

/**
 * TODO: Add more than one class in a file test: patch4-Chart-15-JGenProg2017
 */
public class ArjaTest {
    @Test
    public void testArja() throws Exception {
        FinalResult result = TestUtills.runRepairability("Arja", "/repairability_test_files/arja");
        TestUtills.numberOfInstances(result, 11, 9);
    }
}
