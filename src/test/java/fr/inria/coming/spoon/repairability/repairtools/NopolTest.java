package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtills;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test files existence reasons:
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
 * if-update-deep
 * - patch1-Closure-62-Nopol2017
 * - patch1-Math-81-Nopol2017
 * - patch1-Time-14-Nopol2017
 * - patch1-Closure-7-Nopol2017
 */
public class NopolTest {

    @Ignore
    public void testNopolTruePositive1() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Nopol", "/repairability_test_files/NopolTest");
        RepairabilityTestUtills.numberOfInstances(result, 9, 9);
    }

    @Ignore
    public void testNopolTruePositive2() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Nopol", "/repairability_test_files/jMutRepairTest");
        RepairabilityTestUtills.numberOfInstances(result, 4, 4);
    }

    @Test
    public void testNopolTypesInsShallow() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Nopol", "/repairability_test_files/NopolTypes/if_ins_shallow");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testNopolTypesInsDeep() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Nopol", "/repairability_test_files/NopolTypes/if_ins_deep");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testNopolTypesUpdShallow() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Nopol", "/repairability_test_files/jMutRepairBinaryTypes/ss/");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testNopolTypesUpdDeep() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Nopol", "/repairability_test_files/NopolTypes/if_upd_deep");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }


}
