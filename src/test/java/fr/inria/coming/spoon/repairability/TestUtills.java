package fr.inria.coming.spoon.repairability;

import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.repairability.RepairabilityAnalyzer;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestUtills {
    public static void numberOfInstances(FinalResult result, int totalInputs, int foundInstances) {
        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(totalInputs, revisionsMap.keySet().size());

        int counter = 0;
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances = (PatternInstancesFromRevision) rr.getResultFromClass(RepairabilityAnalyzer.class);
            counter += instances.getInfoPerDiff().stream().mapToInt(v -> v.getInstances().size()).sum();
        }

        assertEquals(foundInstances, counter);
    }


    public static FinalResult runRepairability(String toolName, String inputFiles) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-input",
                        "files",
                        "-location",
                        TestUtills.class.getResource(inputFiles).getFile()});

        assertNotNull(result);
        return result;
    }



    public static FinalResult runRepairabilitydiff(String toolName) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-location",
                        " ./repogit4testv0/",
                        "-output",
                        "./out",
                        "-filter",
                        "maxfiles",
                        "-parameters",
                        "max_files_per_commit:1"});

        assertNotNull(result);
        return result;
    }
}
