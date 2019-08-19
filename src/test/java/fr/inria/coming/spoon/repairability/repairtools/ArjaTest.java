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
        TestUtills.numberOfInstances(result, 7, 6);
    }

    //failing test case
//    @Test
//    public void testArjafailing() throws Exception {
//        FinalResult result = TestUtills.runRepairability("Arja", "/repairability_test_files/arja_test");
//        TestUtills.numberOfInstances(result, 1, 1);
//    }
//
//    @Test
//    public void testArjafailing2() throws Exception {
//        FinalResult result = TestUtills.runRepairability("Arja", "/repairability_test_files/arja_test2");
//        TestUtills.numberOfInstances(result, 1, 1);
//    }


//    @Test
//    //it can not find the LO and RO in the source file although they  exist in the file
//    public void testArjafailing3() throws Exception {
//        FinalResult result = TestUtills.runRepairability("Arja", "/repairability_test_files/arjatest3");
//        TestUtills.numberOfInstances(result, 1, 1);
//    }

//    @Test
////     because the left/right  hand operators can not be distinguished correctly
//    public void testArjafailing4() throws Exception {
//        FinalResult result = TestUtills.runRepairability("Arja", "/repairability_test_files/arjatest4");
//        TestUtills.numberOfInstances(result, 1, 1);
//    }

}
