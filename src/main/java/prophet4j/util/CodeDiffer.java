package prophet4j.util;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gumtree.spoon.diff.operations.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import prophet4j.meta.FeatureStruct.FeatureManager;
import prophet4j.meta.RepairStruct.DiffEntry;
import prophet4j.meta.RepairStruct.Repair;
import prophet4j.meta.RepairType.DiffActionType;
import spoon.reflect.declaration.CtElement;

// based on pdiffer.cpp, ASTDiffer.cpp
public class CodeDiffer {

    private boolean forLearner;
    private static final Logger logger = LogManager.getLogger(CodeDiffer.class.getName());

    public CodeDiffer(boolean forLearner){
        this.forLearner = forLearner;
    }

    private List<DiffEntry> genDiffEntries(Diff diff) throws IndexOutOfBoundsException {
        List<DiffEntry> diffEntries = new ArrayList<>();
        List<Operation> operations = diff.getRootOperations();
        Map<Integer, List<Operation>> locatedOperations = new LinkedHashMap<>();
        // tmp wrapper for gumtree-spoon-ast-diff
        // may be affected by future versions of gumtree-spoon-ast-diff
        for (Operation operation : operations) {
            // we ignore all MoveOperations
            if (operation instanceof MoveOperation) continue;
            Pattern pattern = Pattern.compile(":(\\d+)");
            Matcher matcher = pattern.matcher(operation.toString());
            Integer lineNum = null;
            if(matcher.find()) {
                lineNum = Integer.valueOf(matcher.group(1));
            }
            if (!locatedOperations.containsKey(lineNum)) {
                locatedOperations.put(lineNum, new ArrayList<>());
            }
            locatedOperations.get(lineNum).add(operation);
        }
        for (List<Operation> operationList : locatedOperations.values()) {
            Operation deleteOperation = null;
            Operation insertOperation = null;
            for (Operation operation : operationList) {
                if (operation instanceof DeleteOperation) {
                    deleteOperation = operation;
                } else if (operation instanceof InsertOperation) {
                    insertOperation = operation;
                }
            }
            if (deleteOperation != null && insertOperation != null) {
                DiffActionType type = DiffActionType.ReplaceAction;
                CtElement srcNode = deleteOperation.getSrcNode();
                CtElement dstNode = insertOperation.getSrcNode();
                diffEntries.add(new DiffEntry(type, srcNode, dstNode));
            } else {
                for (Operation operation : operationList) {
                    CtElement srcNode = operation.getSrcNode();
                    CtElement dstNode = operation.getDstNode();
                    DiffActionType type = DiffActionType.UnknownAction;
                    if (operation instanceof DeleteOperation) {
                        // dstNode should be null
                        if (dstNode == null) dstNode = srcNode;
                        type = DiffActionType.DeleteAction;
                    } else if (operation instanceof InsertOperation) {
                        // dstNode should be null
                        if (dstNode == null) dstNode = srcNode;
                        type = DiffActionType.InsertAction;
                    } else if (operation instanceof UpdateOperation) {
                        // both srcNode and dstNode are not null
                        type = DiffActionType.ReplaceAction;
                    }
                    // as we ignored all MoveOperations
                    assert type != DiffActionType.UnknownAction;
                    diffEntries.add(new DiffEntry(type, srcNode, dstNode));
                }
            }
            /* https://github.com/SpoonLabs/gumtree-spoon-ast-diff/issues/55
            In Gumtree, an "Update" operation means that:
            - either the it's a string based element and the string has changed
            - or that only a small fraction of children has changed (to be verified).
            Assume that we have one literal replaced by a method call. This is represented by one deletion and one addition. We can have a higher-level operation "Replace" instead.
            */
        }
        return diffEntries;
    }

    private List<FeatureManager> genFeatureManagers(Diff diff) {
        List<FeatureManager> featureManagers = new ArrayList<>();
        try {
            FeatureExtractor featureExtractor = new FeatureExtractor();
            for (DiffEntry diffEntry : genDiffEntries(diff)) {
                List<Repair> repairs = new ArrayList<>();
                // as RepairGenerator receive diffEntry as parameter, we do not need ErrorLocalizer
                RepairGenerator generator = new RepairGenerator(diffEntry);
                // human repair (at index 0)
                repairs.add(generator.obtainHumanRepair());
                if (forLearner) {
                    // repair candidates (at indexes after 0)
                    repairs.addAll(generator.obtainRepairCandidates());
                }
                for (Repair repair: repairs) {
                    assert(repair.actions.size() > 0);
                    for (CtElement atom : repair.getCandidateAtoms()) {
                        featureManagers.add(featureExtractor.extractFeature(repair, atom));
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.WARN, "diff.commonAncestor() returns null value");
        }
        return featureManagers;
    }

    public List<FeatureManager> func4Demo(File file0, File file1) throws Exception {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(file0, file1);
        return genFeatureManagers(diff);
    }

    // for FeatureExtractorTest.java
    public List<FeatureManager> func4Test(String str0, String str1) {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(str0, str1);
        return genFeatureManagers(diff);
    }
}
