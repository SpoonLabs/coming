package fr.inria.coming.spoon.repairability;

import fr.inria.coming.main.ComingMain;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RepairabilityTest {

    @Test
    public void testRepairabilityInterface() throws Exception {

        ComingMain cm = new ComingMain();

        Object result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        "JMutRepair",
                        "-input",
                        "files",
                        "-location",
                        getClass().getResource("/jMutRepairTest/").getFile()});

        assertNotNull(result);
    }

    @Test
    public void testRepairabilityAll() throws Exception {

        ComingMain cm = new ComingMain();

        Object result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        "ALL",
                        "-input",
                        "files",
                        "-location",
                        getClass().getResource("/jMutRepairTest/").getFile()});

        assertNotNull(result);
    }
}