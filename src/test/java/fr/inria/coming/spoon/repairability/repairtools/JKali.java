package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class JKali {
    @Test
    public void testJMutRepairTruePositive1() throws Exception {
        FinalResult result = TestUtills.runRepairability("JKali", "/repairability_test_files/JKali/");
        TestUtills.numberOfInstances(result, 5, 5);
    }


}
