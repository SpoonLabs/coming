package fr.inria.coming.spoon.repairability;

import fr.inria.coming.main.ComingMain;
import org.junit.Test;

public class RepairabilityTest {

    @Test
    public void testListEntities() {

        ComingMain.main(new String[]{"-mode",
                "repairability",
                "-repairtool",
                "JMutRepair",
                "-input",
                "files",
                "-location",
                getClass().getResource("/jMutRepairTest/").getFile()});
    }
}
