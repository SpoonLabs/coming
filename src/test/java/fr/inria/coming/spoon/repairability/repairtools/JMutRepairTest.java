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

public class JMutRepairTest {
    @Test
    public void testJMutRepairBasic() throws Exception {

        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        "JMutRepair",
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

        assertEquals(3, counter);

    }
}
