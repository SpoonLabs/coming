package fr.inria.coming.spoon.repairability.repairtools;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtills;
import fr.inria.coming.spoon.utils.TestUtils;
import org.junit.Test;

import java.io.File;

/**
 * Created by khesoem on 10/14/2019.
 */
public class CardumenTest {

    @Test
    public void testCardumen1() throws Exception {
        FinalResult result =
                RepairabilityTestUtills.runRepairabilityWithParameters
                        (
                                "Cardumen",
                                "/repairability_test_files/cardumen/cardumen1",
                                "exclude_repair_patterns_not_covering_the_whole_diff:true"
                        );
        RepairabilityTestUtills.numberOfInstances(result, 1, 1);
    }

    @Test
    public void testCardumen2() throws Exception {
        FinalResult result =
                RepairabilityTestUtills.runRepairabilityWithParameters
                        (
                                "Cardumen",
                                "/repairability_test_files/cardumen/cardumen2",
                                "exclude_repair_patterns_not_covering_the_whole_diff:true"
                        );
        RepairabilityTestUtills.numberOfInstances(result, 1, 0);
    }

    @Test
    public void testCardumen3() throws Exception {
        FinalResult result =
                RepairabilityTestUtills.runRepairabilityWithParameters
                        (
                                "Cardumen",
                                "/repairability_test_files/cardumen/cardumen2",
                                "include_all_instances_for_each_tool:true"
                        );
        RepairabilityTestUtills.numberOfInstances(result, 1, 4);
    }

}
