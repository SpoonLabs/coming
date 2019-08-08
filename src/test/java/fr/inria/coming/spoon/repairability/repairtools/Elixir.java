package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class Elixir {
    @Test
    public void elixirTest() throws Exception {
        FinalResult result = TestUtills.runRepairability("Elixir", "/repairability_test_files/Elixir/");
        TestUtills.numberOfInstances(result, 8, 7);
    }


    @Test
    public void elixirTestonDatasetReal() throws Exception {
        FinalResult result = TestUtills.runRepairability("Elixir", "/repairability_test_files/elixir_data/");
        TestUtills.numberOfInstances(result, 4, 4);
    }
}