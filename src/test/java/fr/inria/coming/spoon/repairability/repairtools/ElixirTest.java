package fr.inria.coming.spoon.repairability.repairtools;

import org.junit.Test;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;

public class ElixirTest {
	@Test
	public void elixirTest() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Elixir", "/repairability_test_files/Elixir/");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 8, 8);
	}

	@Test
	public void elixirTestOnDatasetReal() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Elixir",
				"/repairability_test_files/elixir_data/");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 3, 3);
	}

	@Test
	public void elixirTestOnDatasetFalse1() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Elixir",
				"/repairability_test_files/JGenProgPostiveTest/");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
	}

	@Test
	public void elixirTestOnDatasetFalse2() throws Exception {
		FinalResult result = RepairabilityTestUtils.runRepairability("Elixir", "/repairability_test_files/NopolTypes/");
		RepairabilityTestUtils.checkNumberOfRepairInstances(result, 3, 0);
	}

	@Test
	public void testGroundTruthCreatedPatches() throws Exception {
		RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "Elixir", 10, 0);
	}

}
