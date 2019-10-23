package fr.inria.coming.spoon.repairability;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepairabilityTestUtils {
    public static void checkNumberOfRepairInstances(FinalResult result, int totalInputs, int foundInstances) {
        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(totalInputs, revisionsMap.keySet().size());

        int counter = countNumberOfInstances(revisionsMap, RepairabilityAnalyzer.class);

        assertEquals(foundInstances, counter);
    }

    public static int countNumberOfInstances(Map<IRevision, RevisionResult> revisionsMap, Class analyzer) {
        int counter = 0;
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances =
                    (PatternInstancesFromRevision) rr.getResultFromClass(analyzer);
            counter += instances.getInfoPerDiff().stream().mapToInt(v -> v.getInstances().size()).sum();
        }
        return counter;
    }

    public static int countRootOperationsExcludingType(FinalResult finalResult, String excludedType){
        Map<IRevision, RevisionResult> revisionsMap = finalResult.getAllResults();

        int counter = 0;
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            DiffResult result =
                    (DiffResult) rr.getResultFromClass(FineGrainDifftAnalyzer.class);
            for(Object diffOfFile : result.getDiffOfFiles().entrySet()){
                Diff diff = ((Map.Entry<String, Diff>) diffOfFile).getValue();
                List<Operation> rootOps = diff.getRootOperations();
                counter += rootOps.stream().filter(op -> !op.getAction().getName().equals(excludedType)).count();
            }
        }

        return counter;
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
                        URLDecoder.decode(RepairabilityTestUtils.class.getResource(inputFiles).getFile(), "UTF-8")});

        assertNotNull(result);
        return result;
    }

    public static FinalResult runRepairabilityWithParameters
            (
                    String toolName,
                    String inputFiles,
                    String parameters // ex. "include_all_instances_for_each_tool:true:X:false:Y:true"
            ) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-input",
                        "files",
                        "-parameters",
                        parameters,
                        "-location",
                        URLDecoder.decode(RepairabilityTestUtils.class.getResource(inputFiles).getFile(), "UTF-8")});

        assertNotNull(result);
        return result;
    }

    public static FinalResult runRepairabilityGit(String toolName, String inputFiles) throws Exception {
        ComingMain cm = new ComingMain();

        FinalResult result = cm.run(
                new String[]{"-mode",
                        "repairability",
                        "-repairtool",
                        toolName,
                        "-input",
                        "git",
                        "-location",
                        inputFiles});

        assertNotNull(result);
        return result;
    }
}
