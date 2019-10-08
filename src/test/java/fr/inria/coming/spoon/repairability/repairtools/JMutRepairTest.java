package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtills;
import org.junit.Test;

public class JMutRepairTest {
    @Test
    public void testJMutRepairTruePositive1() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairTest/");
        RepairabilityTestUtills.numberOfInstances(result, 4, 4);
    }

    @Test
    public void testJMutRepairTrueNegatives1() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JMutRepair", "/repairability_test_files/NopolTest/");
        RepairabilityTestUtills.numberOfInstances(result, 6, 0);
    }


    @Test
    public void testJMutRepairBinaryTestSS() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/ss/");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestSD() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/sd/");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestDS() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/ds/");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestDD() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/dd/");
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

}
