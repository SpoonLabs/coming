package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test files existence reasons
 *
 * if-ins-shallow:
 * - patch1-Chart-13-Nopol2017
 * - patch1-Time-4-Nopol2017
 * - patch1-Closure-22-Nopol2017
 *
 * if-ins-deep:
 * - patch1-Math-42-Nopol2017
 * - patch1-Closure-28-Nopol2017
 *
 * if-update
 * - patch1-Closure-62-Nopol2017
 * - patch1-Math-81-Nopol2017
 * - patch1-Time-14-Nopol2017
 * - patch1-Closure-7-Nopol2017
 * 
 */
public class NopolTest {

    @Test
    public void testNopolBasic() throws Exception {

        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        "Nopol",
                        "-input",
                        "files",
                        "-location",
                        getClass().getResource("/jMutRepairTest/").getFile()});

        assertNotNull(result);

        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(12, revisionsMap.keySet().size());

        int counter = 0;
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances = (PatternInstancesFromRevision) rr.getResultFromClass(RepairabilityAnalyzer.class);
            counter += instances.getInfoPerDiff().stream().mapToInt(v -> v.getInstances().size()).sum();
        }

        assertEquals(12, counter);

    }
}
