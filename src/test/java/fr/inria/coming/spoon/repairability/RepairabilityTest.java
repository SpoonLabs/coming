package fr.inria.coming.spoon.repairability;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RepairabilityTest {

    @Test
    public void testRepairabilityInterface() throws Exception {
        TestUtills.runRepairability("JMutRepair", "/repairability_test_files/one/");
    }

    @Test
    public void testDiffResults() throws Exception {
        TestUtills.runRepairabilityPrint("ALL", "repogit4testv0");
    }

    @Test
    public void testOneInstancePerRevision() throws Exception {

        FinalResult result = TestUtills.runRepairability("ALL", "/repairability_test_files/mixed/");

        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(2, revisionsMap.keySet().size());

        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances = (PatternInstancesFromRevision) rr.getResultFromClass(RepairabilityAnalyzer.class);

            // for each revision
            Set<String> toolsSeen = new HashSet<>();
            for (PatternInstancesFromDiff v : instances.getInfoPerDiff()) {
                for (ChangePatternInstance patternInstance : v.getInstances()) {
                    String toolName = patternInstance.getPattern().getName().split(File.pathSeparator)[0];
                    assertFalse(toolsSeen.contains(toolName)); // to check if the same tool hasn't been seen in this particular revison
                    toolsSeen.add(toolName);
                }
            }
        }
    }

}