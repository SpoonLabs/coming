package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

/**
 * Test files existence reasons
 * <p>
 * if-ins-shallow:
 * - patch1-Chart-13-Nopol2017
 * - patch1-Time-4-Nopol2017
 * - patch1-Closure-22-Nopol2017
 * <p>
 * if-ins-deep:
 * - patch1-Math-42-Nopol2017
 * - patch1-Closure-28-Nopol2017
 * <p>
 * if-update
 * - patch1-Closure-62-Nopol2017
 * - patch1-Math-81-Nopol2017
 * - patch1-Time-14-Nopol2017
 * - patch1-Closure-7-Nopol2017
 */
public class NopolTest {

    @Test
    public void testNopolTruePositive1() throws Exception {
        FinalResult result = TestUtills.runRepairability("Nopol", "/repairability_test_files/NopolTest");
        TestUtills.numberOfInstances(result, 9, 9);
    }

    @Test
    public void testNopolTruePositive2() throws Exception {
        FinalResult result = TestUtills.runRepairability("Nopol", "/repairability_test_files/jMutRepairTest");
        TestUtills.numberOfInstances(result, 4, 4);
    }
}
