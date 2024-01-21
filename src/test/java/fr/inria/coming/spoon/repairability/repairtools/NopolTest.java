package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test files existence reasons:
 * <p>
 * if-ins-shallow:
 * - patch1-Chart-13-Nopol2017
 * - patch1-Time-4-Nopol2017
 * - patch1-Closure-22-Nopol2017
 * <p>
 * if-ins-deep:
 * - patch1-Math-42-Nopol2017
 * - patch1-Closure-28-Nopol2017
 * <p>
 * if-update-deep
 * - patch1-Closure-62-Nopol2017
 * - patch1-Math-81-Nopol2017
 * - patch1-Time-14-Nopol2017
 * - patch1-Closure-7-Nopol2017
 */
public class NopolTest {

    @Ignore
    public void testNopolTruePositive1() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("Nopol", "/repairability_test_files/NopolTest");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 9, 9);
    }

    @Ignore
    public void testNopolTruePositive2() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("Nopol", "/repairability_test_files/jMutRepairTest");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 4, 4);
    }

    @Test
    public void testNopolTypesInsShallow() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("Nopol", "/repairability_test_files/NopolTypes/if_ins_shallow");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testNopolTypesInsDeep() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("Nopol", "/repairability_test_files/NopolTypes/if_ins_deep");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testNopolTypesUpdShallow() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("Nopol", "/repairability_test_files/jMutRepairBinaryTypes/ss/");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    public void testNopolTypesUpdDeep() throws Exception {
        FinalResult result = RepairabilityTestUtils.runRepairability("Nopol", "/repairability_test_files/NopolTypes/if_upd_deep");
        RepairabilityTestUtils.checkNumberOfRepairInstances(result, 1, 1);
    }

    @Test
    @Ignore // Gumtree3 has changed the numbers of matched instances
	public void testGroundTruthCreatedPatches() throws Exception {
    	RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "Nopol2015", 0, 0);
		RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "Nopol2017", 2, 0);
	}
    
}
