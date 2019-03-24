package fr.inria.prophet4j.defined;

import fr.inria.prophet4j.defined.Structure.DiffType;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.ParameterVector;
import fr.inria.prophet4j.defined.Structure.DiffEntry;
import fr.inria.prophet4j.defined.Structure.Repair;
import fr.inria.prophet4j.defined.extended.ExtendedFeatureExtractor;
import fr.inria.prophet4j.defined.extended.ExtendedRepairGenerator;
import fr.inria.prophet4j.defined.original.OriginalFeatureExtractor;
import fr.inria.prophet4j.defined.original.OriginalRepairGenerator;
import fr.inria.prophet4j.utility.Option;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// based on pdiffer.cpp, ASTDiffer.cpp
public class CodeDiffer {

    private boolean forLearner;
    private Option option;
    private static final Logger logger = LogManager.getLogger(CodeDiffer.class.getName());

    public CodeDiffer(boolean forLearner, Option option){
        this.forLearner = forLearner;
        this.option = option;
    }

    private FeatureExtractor newFeatureExtractor() {
        FeatureExtractor featureExtractor = null;
        switch (option.featureOption) {
            case EXTENDED:
                featureExtractor = new ExtendedFeatureExtractor();
                break;
            case ORIGINAL:
                featureExtractor = new OriginalFeatureExtractor();
                break;
        }
        return featureExtractor;
    }

    private RepairGenerator newRepairGenerator(DiffEntry diffEntry) {
        RepairGenerator repairGenerator = null;
        switch (option.featureOption) {
            case EXTENDED:
                repairGenerator = new ExtendedRepairGenerator(diffEntry);
                break;
            case ORIGINAL:
                repairGenerator = new OriginalRepairGenerator(diffEntry);
                break;
        }
        return repairGenerator;
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
                CtElement srcNode = deleteOperation.getSrcNode();
                CtElement dstNode = insertOperation.getSrcNode();
                DiffType type = DiffType.ReplaceType;
//                System.out.println(srcNode);
//                System.out.println(dstNode);
//                System.out.println("++++++++");
                // human rule to distinguish functionality changes from revision changes
                if (srcNode instanceof CtClass || srcNode instanceof CtMethod ||
                        dstNode instanceof CtClass || dstNode instanceof CtMethod) {
                    continue;
                }
                diffEntries.add(new DiffEntry(type, srcNode, dstNode));
            } else {
                for (Operation operation : operationList) {
                    CtElement srcNode = operation.getSrcNode();
                    CtElement dstNode = operation.getDstNode();
                    DiffType type = DiffType.UnknownType;
                    if (operation instanceof DeleteOperation) {
                        if (srcNode == null) srcNode = dstNode;
                        if (dstNode == null) dstNode = srcNode;
                        type = DiffType.DeleteType;
                    } else if (operation instanceof InsertOperation) {
                        if (srcNode == null) srcNode = dstNode;
                        if (dstNode == null) dstNode = srcNode;
                        type = DiffType.InsertType;
                    } else if (operation instanceof UpdateOperation) {
                        // both srcNode and dstNode are not null
                        type = DiffType.ReplaceType;
                    }
                    // as we ignored all MoveOperations
                    assert type != DiffType.UnknownType;
//                    System.out.println(srcNode);
//                    System.out.println(dstNode);
//                    System.out.println("++++++++");
                    // human rule to distinguish functionality changes from revision changes
                    if (srcNode instanceof CtClass || srcNode instanceof CtMethod ||
                            dstNode instanceof CtClass || dstNode instanceof CtMethod) {
                        continue;
                    }
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

    private List<FeatureVector> genFeatureVectors(Diff diff) {
        List<FeatureVector> featureVectors = new ArrayList<>();
        try {
            FeatureExtractor featureExtractor = newFeatureExtractor();
            for (DiffEntry diffEntry : genDiffEntries(diff)) {
                // as RepairGenerator receive diffEntry as parameter, we do not need ErrorLocalizer
                RepairGenerator generator = newRepairGenerator(diffEntry);
                {
                    Repair repair = generator.obtainHumanRepair();
                    FeatureVector featureVector = new FeatureVector(true);
                    for (CtElement atom : repair.getCandidateAtoms()) {
                        featureVector.merge(featureExtractor.extractFeature(repair, atom));
                    }
                    featureVectors.add(featureVector);
                }
                if (forLearner) {
                    for (Repair repair: generator.obtainRepairCandidates()) {
                        FeatureVector featureVector = new FeatureVector(false);
                        for (CtElement atom : repair.getCandidateAtoms()) {
                            featureVector.merge(featureExtractor.extractFeature(repair, atom));
                        }
                        featureVectors.add(featureVector);
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.WARN, "diff.commonAncestor() returns null value");
        }
        return featureVectors;
    }

    // for DataLoader, we do not need to obtainRepairCandidates as they are given
    public List<FeatureVector> func4Cardumen(File oldFile, List<File> newFiles) {
        AstComparator comparator = new AstComparator();
        List<FeatureVector> featureVectors = new ArrayList<>();
        for (int i = 0; i < newFiles.size(); i++) {
            try {
                Diff diff = comparator.compare(oldFile, newFiles.get(i));
                FeatureExtractor featureExtractor = newFeatureExtractor();
                for (DiffEntry diffEntry : genDiffEntries(diff)) {
                    // as RepairGenerator receive diffEntry as parameter, we do not need ErrorLocalizer
                    RepairGenerator generator = newRepairGenerator(diffEntry);
                    Repair repair = generator.obtainHumanRepair();
                    // the first one in newFiles is human patch
                    FeatureVector featureVector = new FeatureVector(i == 0);
                    for (CtElement atom : repair.getCandidateAtoms()) {
                        featureVector.merge(featureExtractor.extractFeature(repair, atom));
                    }
                    featureVectors.add(featureVector);
                }
            } catch (IndexOutOfBoundsException e) {
                logger.log(Level.WARN, "diff.commonAncestor() returns null value");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return featureVectors;
    }

    public List<FeatureVector> func4Demo(File oldFile, File newFile) {
        try {
            AstComparator comparator = new AstComparator();
            Diff diff = comparator.compare(oldFile, newFile);
            return genFeatureVectors(diff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // for FeatureExtractorTest.java
    public List<FeatureVector> func4Test(String oldStr, String newStr) {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(oldStr, newStr);
        return genFeatureVectors(diff);
    }

    public Double scorePatch(File oldFile, File newFile, ParameterVector parameterVector) {
        double score = 0;
        try {
            AstComparator comparator = new AstComparator();
            Diff diff = comparator.compare(oldFile, newFile);
            List<FeatureVector> featureVectors = new ArrayList<>();

            try {
                FeatureExtractor featureExtractor = newFeatureExtractor();
                for (DiffEntry diffEntry : genDiffEntries(diff)) {
                    // as RepairGenerator receive diffEntry as parameter, we do not need ErrorLocalizer
                    RepairGenerator generator = newRepairGenerator(diffEntry);
                    {
                        Repair repair = generator.obtainHumanRepair();
                        FeatureVector featureVector = new FeatureVector(true);
                        for (CtElement atom : repair.getCandidateAtoms()) {
                            featureVector.merge(featureExtractor.extractFeature(repair, atom));
                        }
                        featureVectors.add(featureVector);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                logger.log(Level.WARN, "diff.commonAncestor() returns null value");
            }

            // sometimes one patch file patches multi-defects
            for (FeatureVector featureVector : featureVectors) {
                score += featureVector.score(parameterVector);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }

    public List<File> rankPatches(File oldFile, List<File> newFiles, ParameterVector parameterVector) {
        List<Map.Entry<File, Double>> entryList = new ArrayList<>();
        for (File newFile : newFiles) {
            entryList.add(new HashMap.SimpleEntry<>(newFile, scorePatch(oldFile, newFile, parameterVector)));
        }
        entryList.sort(Comparator.comparingDouble(Map.Entry::getValue));
        return entryList.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
