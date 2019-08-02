package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.TestUtills;
import org.junit.Test;

public class elixir {
    @Test
    public void elixirTest() throws Exception {
        FinalResult result = TestUtills.runRepairability("elixir", "/repairability_test_files/elixir/");
        TestUtills.numberOfInstances(result, 5, 4);
    }
}
