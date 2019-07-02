package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class JMutRepairTest {
    @Test
    public void testJMutRepairTruePositive1() throws Exception {
        FinalResult result = TestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairTest/");
        TestUtills.numberOfInstances(result, 4, 4);
    }

    @Test
    public void testJMutRepairTrueNegatives1() throws Exception {
        FinalResult result = TestUtills.runRepairability("JMutRepair", "/repairability_test_files/NopolTest/");
        TestUtills.numberOfInstances(result, 6, 0);
    }


    @Test
    public void testJMutRepairBinaryTestSS() throws Exception {
        FinalResult result = TestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/ss/");
        TestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestSD() throws Exception {
        FinalResult result = TestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/sd/");
        TestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestDS() throws Exception {
        FinalResult result = TestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/ds/");
        TestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testJMutRepairBinaryTestDD() throws Exception {
        FinalResult result = TestUtills.runRepairability("JMutRepair", "/repairability_test_files/jMutRepairBinaryTypes/dd/");
        TestUtills.numberOfInstances(result, 1, 1);
    }

}
