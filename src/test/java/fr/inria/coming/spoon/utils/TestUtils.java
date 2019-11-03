package fr.inria.coming.spoon.utils;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by khesoem on 10/8/2019.
 */
public class TestUtils {
    private static TestUtils _instance;

    public static TestUtils getInstance(){
        if(_instance == null)
            _instance = new TestUtils();
        return _instance;
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

    public static int countNumberOfUniqueInstances(Map<IRevision, RevisionResult> revisionsMap, Class analyzer) {
        List<ChangePatternInstance> allUniqueInstances = new ArrayList<>();
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances =
                    (PatternInstancesFromRevision) rr.getResultFromClass(analyzer);
            for (PatternInstancesFromDiff info : instances.getInfoPerDiff()) {
                for (ChangePatternInstance instance : info.getInstances()) {
                    boolean alreadyAdded = false;
                    for (ChangePatternInstance addedInstance : allUniqueInstances) {
                        if (new HashSet(instance.getActions()).equals(new HashSet(addedInstance.getActions())))
                            alreadyAdded = true;
                    }
                    if (!alreadyAdded)
                        allUniqueInstances.add(instance);
                }
            }
        }
        return allUniqueInstances.size();
    }

    public File getFile(String name) throws UnsupportedEncodingException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(URLDecoder.decode(classLoader.getResource(name).getPath(), "UTF-8"));
        return file;
    }

    public static int countRootOperationsExcludingType(FinalResult finalResult, String excludedType) {
        Map<IRevision, RevisionResult> revisionsMap = finalResult.getAllResults();

        int counter = 0;
        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            DiffResult result =
                    (DiffResult) rr.getResultFromClass(FineGrainDifftAnalyzer.class);
            for (Object diffOfFile : result.getDiffOfFiles().entrySet()) {
                Diff diff = ((Map.Entry<String, Diff>) diffOfFile).getValue();
                List<Operation> rootOps = diff.getRootOperations();
                counter += rootOps.stream().filter(op -> !op.getAction().getName().equals(excludedType)).count();
            }
        }

        return counter;
    }
}
