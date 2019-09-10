package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class ElixirTest {
    @Test
    public void elixirTest() throws Exception {
        FinalResult result = TestUtills.runRepairability("Elixir", "/repairability_test_files/Elixir/");
        TestUtills.numberOfInstances(result, 7, 7);
    }

    @Test
    public void elixirTestonDatasetReal() throws Exception {
        FinalResult result = TestUtills.runRepairability("Elixir", "/repairability_test_files/elixir_data/");
        TestUtills.numberOfInstances(result, 3, 3);
    }

    @Test
    public void elixirTestonDatasetFalse1() throws Exception {
        FinalResult result = TestUtills.runRepairability("Elixir", "/repairability_test_files/JGenProgPostiveTest/");
        TestUtills.numberOfInstances(result, 1, 0);
    }

    @Test
    public void elixirTestonDatasetFalse2() throws Exception {
        FinalResult result = TestUtills.runRepairability("Elixir", "/repairability_test_files/NopolTypes/");
        TestUtills.numberOfInstances(result, 3, 0);
    }
}
