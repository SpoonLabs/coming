package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

/**
 * TODO: Add more than one class in a file test: patch4-Chart-15-JGenProg2017
 */
public class ArjaTest {
	@Test
    public void testArja1() throws Exception {
        FinalResult result =
                RepairabilityTestUtils.runRepairabilityWithParameters
                        (
                                "Arja",
                                "/repairability_test_files/arja_extra/arja1",
                                "include_all_instances_for_each_tool:true:exclude_repair_patterns_not_covering_the_whole_diff:true"
                        );
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 0);
    }
	
	@Test
	public void testArja() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Arja", "/repairability_test_files/arja");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 8, 7); 
		// it should be 8 instead of 7 but arja2 is not detected
	}

//    failing test case
//     the added sentence is inside a bigger element so is not detected
	@Test
	public void testArjafailing() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Arja", "/repairability_test_files/arja_test");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
	}

	@Test
	// it returns that the number of arguments in setdataset(dataset) is zero, but
	// is actually 1.
	public void testArja2() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Arja", "/repairability_test_files/arja_test2");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 0);
	}

	@Test
	public void testArja3() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Arja", "/repairability_test_files/arjatest3");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
	}

	@Test
//     because the left/right  hand operators can not be distinguished correctly
	public void testArjafailing4() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Arja", "/repairability_test_files/arjatest4");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 0);
	}

	@Test
	public void testGroundTruthCreatedPatches() throws Exception {
		RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "Arja", 67, 0);
	}

}
