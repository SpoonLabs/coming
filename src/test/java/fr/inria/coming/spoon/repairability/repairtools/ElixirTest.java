package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtills;
import org.junit.Test;

public class ElixirTest {
    @Test
    public void elixirTest() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Elixir", "/repairability_test_files/Elixir/");
        RepairabilityTestUtills.numberOfInstances(result, 8, 8);
    }

    @Test
    public void elixirTestonDatasetReal() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Elixir", "/repairability_test_files/elixir_data/");
        RepairabilityTestUtills.numberOfInstances(result, 3, 3);
    }

    @Test
    public void elixirTestonDatasetFalse1() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Elixir", "/repairability_test_files/JGenProgPostiveTest/");
        RepairabilityTestUtills.numberOfInstances(result, 1, 0);
    }

    @Test
    public void elixirTestonDatasetFalse2() throws Exception {
        FinalResult result = RepairabilityTestUtills.runRepairability("Elixir", "/repairability_test_files/NopolTypes/");
        RepairabilityTestUtills.numberOfInstances(result, 3, 0);
    }
}
