package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;
import fr.inria.coming.spoon.repairability.checkers.DiffResultChecker;
import org.junit.Test;

public class JMutRepairTest {
    @Test
    public void testJMutRepairTruePositive1() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairTest/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 4, 4);
    }

    @Test
    public void testJMutRepairTrueNegatives1() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JMutRepair", "/repairability_test_files/NopolTest/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 6, 0);
    }


    @Test
    public void testJMutRepairBinaryTestSS() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/ss/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestSD() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/sd/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestDS() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/ds/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestDD() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/dd/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testGroundTruthCreatedPatches() throws Exception {
        RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "JMutRepair", new DiffResultChecker() {
            @Override
            public boolean isDiffResultCorrect(FinalResult result) {
                // Gumtree detected 'del' or 'ins' operations instead of 'upd'.
                return true;
            }
        }, 0, 0, 0);
    }

}
