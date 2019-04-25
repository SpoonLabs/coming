package fr.inria.prophet4j.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeFeatureDetector;
import fr.inria.coming.codefeatures.FeatureAnalyzer;
import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.feature.FeatureExtractor;
import fr.inria.prophet4j.feature.RepairGenerator;
import fr.inria.prophet4j.feature.S4R.S4RFeature;
import fr.inria.prophet4j.feature.S4R.S4RFeatureCross;
import fr.inria.prophet4j.utility.Structure.DiffType;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.DiffEntry;
import fr.inria.prophet4j.utility.Structure.Repair;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeatureExtractor;
import fr.inria.prophet4j.feature.enhanced.EnhancedRepairGenerator;
import fr.inria.prophet4j.feature.extended.ExtendedFeatureExtractor;
import fr.inria.prophet4j.feature.extended.ExtendedRepairGenerator;
import fr.inria.prophet4j.feature.original.OriginalFeatureExtractor;
import fr.inria.prophet4j.feature.original.OriginalRepairGenerator;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            case ENHANCED:
                featureExtractor = new EnhancedFeatureExtractor();
                break;
            case EXTENDED:
                featureExtractor = new ExtendedFeatureExtractor();
                break;
            case ORIGINAL:
                featureExtractor = new OriginalFeatureExtractor();
                break;
            case S4R:
                logger.warn("S4R should not call newFeatureExtractor");
                break;
        }
        return featureExtractor;
    }

    private RepairGenerator newRepairGenerator(DiffEntry diffEntry) {
        RepairGenerator repairGenerator = null;
        switch (option.featureOption) {
            case ENHANCED:
                repairGenerator = new EnhancedRepairGenerator(diffEntry);
                break;
            case EXTENDED:
                repairGenerator = new ExtendedRepairGenerator(diffEntry);
                break;
            case ORIGINAL:
                repairGenerator = new OriginalRepairGenerator(diffEntry);
                break;
            case S4R:
                logger.warn("S4R should not call newRepairGenerator");
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
        // used for the case of SKETCH4REPAIR
        FeatureAnalyzer featureAnalyzer = new FeatureAnalyzer();
        CodeFeatureDetector cresolver = new CodeFeatureDetector();
        try {
            if (option.featureOption == FeatureOption.S4R) {
                // based on L152-186 at FeatureAnalyzer.java
                JsonObject file = new JsonObject();
                try {
                    JsonArray changesArray = new JsonArray();
                    file.add("features", changesArray);
                    List<Operation> ops = diff.getRootOperations();
                    for (Operation operation : ops) {
                        try {
                            CtElement affectedCtElement = featureAnalyzer.getLeftElement(operation);
                            if (affectedCtElement != null) {
                                Cntx iContext = cresolver.analyzeFeatures(affectedCtElement);
                                changesArray.add(iContext.toJSON());
                            }
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                // based on L61-79 at FeaturesOnD4jTest.java
                JsonElement elAST = file.get("features");
//        			assertNotNull(elAST);
//		        	assertTrue(elAST instanceof JsonArray);
                JsonArray featuresOperationList = (JsonArray) elAST;
//			        assertTrue(featuresOperationList.size() > 0);
                for (JsonElement featuresOfOperation : featuresOperationList) {
                    // the first one in newFiles is human patch
                    FeatureVector featureVector = new FeatureVector(true);
                    JsonObject jso = featuresOfOperation.getAsJsonObject();
                    for (S4RFeature.CodeFeature codeFeature : S4RFeature.CodeFeature.values()) {
                        JsonElement property = jso.get(codeFeature.toString());
                        if (property != null) {
                            try {
                                JsonPrimitive value = property.getAsJsonPrimitive();
                                String str = value.getAsString();

                                if (str.equalsIgnoreCase("true")) {
                                    // handle boolean-form ones
                                    List<Feature> features = new ArrayList<>();
                                    features.add(codeFeature);
                                    FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, 1.0);
                                    featureVector.addFeatureCross(featureCross);
//                                } else if (str.equalsIgnoreCase("false")) {
//                                    // handle boolean-form ones
//                                    List<Feature> features = new ArrayList<>();
//                                    features.add(codeFeature);
//                                    FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, 0.0);
//                                    featureVector.addFeatureCross(featureCross);
//                                } else {
//                                    // handle numerical-form ones
//                                    try {
//                                        double degree = Double.parseDouble(value.getAsString());
//                                        List<Feature> features = new ArrayList<>();
//                                        features.add(codeFeature);
//                                        FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, degree);
//                                        featureVector.addFeatureCross(featureCross);
//                                    } catch (Exception e) {
////                                        e.printStackTrace();
//                                    }
                                }
                            } catch (IllegalStateException e) {
//                                logger.error("Not a JSON Primitive");
                            }
                        }
                    }
                    featureVectors.add(featureVector);
                }
            } else {
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
                        // we could also try on other patches-generators
                        for (Repair repair: generator.obtainRepairCandidates()) {
                            FeatureVector featureVector = new FeatureVector(false);
                            for (CtElement atom : repair.getCandidateAtoms()) {
                                featureVector.merge(featureExtractor.extractFeature(repair, atom));
                            }
                            featureVectors.add(featureVector);
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.WARN, "diff.commonAncestor() returns null value");
        }
        return featureVectors;
    }

    // for DataLoader, we do not need to obtainRepairCandidates as they are given
    public List<FeatureVector> runByPatches(File oldFile, List<File> newFiles) {
        AstComparator comparator = new AstComparator();
        List<FeatureVector> featureVectors = new ArrayList<>();
        // used for the case of SKETCH4REPAIR
        FeatureAnalyzer featureAnalyzer = new FeatureAnalyzer();
        CodeFeatureDetector cresolver = new CodeFeatureDetector();
        for (int i = 0; i < newFiles.size(); i++) {
            try {
                Diff diff = comparator.compare(oldFile, newFiles.get(i));
                if (option.featureOption == FeatureOption.S4R) {
                    // based on L152-186 at FeatureAnalyzer.java
                    JsonObject file = new JsonObject();
                    try {
                        JsonArray changesArray = new JsonArray();
                        file.add("features", changesArray);
                        if (diff == null) {
                            continue;
                        }
                        List<Operation> ops = diff.getRootOperations();
                        for (Operation operation : ops) {
                            try {
                                CtElement affectedCtElement = featureAnalyzer.getLeftElement(operation);
                                if (affectedCtElement != null) {
                                    Cntx iContext = cresolver.analyzeFeatures(affectedCtElement);
                                    changesArray.add(iContext.toJSON());
                                }
                            } catch (Exception e) {
//                                e.printStackTrace();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    // based on L61-79 at FeaturesOnD4jTest.java
                    JsonElement elAST = file.get("features");
//        			assertNotNull(elAST);
//		        	assertTrue(elAST instanceof JsonArray);
                    JsonArray featuresOperationList = (JsonArray) elAST;
//			        assertTrue(featuresOperationList.size() > 0);
                    for (JsonElement featuresOfOperation : featuresOperationList) {
                        // the first one in newFiles is human patch
                        FeatureVector featureVector = new FeatureVector(i == 0);
                        JsonObject jso = featuresOfOperation.getAsJsonObject();
                        for (S4RFeature.CodeFeature codeFeature : S4RFeature.CodeFeature.values()) {
                            JsonElement property = jso.get(codeFeature.toString());
                            if (property != null) {
                                try {
                                    JsonPrimitive value = property.getAsJsonPrimitive();
                                    String str = value.getAsString();

                                    if (str.equalsIgnoreCase("true")) {
                                        // handle boolean-form ones
                                        List<Feature> features = new ArrayList<>();
                                        features.add(codeFeature);
                                        FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, 1.0);
                                        featureVector.addFeatureCross(featureCross);
//                                    } else if (str.equalsIgnoreCase("false")) {
//                                        // handle boolean-form ones
//                                        List<Feature> features = new ArrayList<>();
//                                        features.add(codeFeature);
//                                        FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, 0.0);
//                                        featureVector.addFeatureCross(featureCross);
//                                    } else {
//                                        // handle numerical-form ones
//                                        try {
//                                            double degree = Double.parseDouble(value.getAsString());
//                                            List<Feature> features = new ArrayList<>();
//                                            features.add(codeFeature);
//                                            FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, degree);
//                                            featureVector.addFeatureCross(featureCross);
//                                        } catch (Exception e) {
////                                        e.printStackTrace();
//                                        }
                                    }
                                } catch (IllegalStateException e) {
//                                    logger.error("Not a JSON Primitive");
                                }
                            }
                        }
                        featureVectors.add(featureVector);
                    }
                } else {
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
                }
            } catch (IndexOutOfBoundsException e) {
                logger.log(Level.WARN, "diff.commonAncestor() returns null value");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return featureVectors;
    }

    public List<FeatureVector> runByGenerator(File oldFile, File newFile) {
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
    public List<FeatureVector> runByGenerator(String oldStr, String newStr) {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(oldStr, newStr);
        return genFeatureVectors(diff);
    }

//    public Double scorePatch(File oldFile, File newFile, ParameterVector parameterVector) {
//        double score = 0;
//        try {
//            AstComparator comparator = new AstComparator();
//            Diff diff = comparator.compare(oldFile, newFile);
//            List<FeatureVector> featureVectors = new ArrayList<>();
//
//            try {
//                FeatureExtractor featureExtractor = newFeatureExtractor();
//                for (DiffEntry diffEntry : genDiffEntries(diff)) {
//                    // as RepairGenerator receive diffEntry as parameter, we do not need ErrorLocalizer
//                    RepairGenerator generator = newRepairGenerator(diffEntry);
//                    {
//                        Repair repair = generator.obtainHumanRepair();
//                        FeatureVector featureVector = new FeatureVector(true);
//                        for (CtElement atom : repair.getCandidateAtoms()) {
//                            featureVector.merge(featureExtractor.extractFeature(repair, atom));
//                        }
//                        featureVectors.add(featureVector);
//                    }
//                }
//            } catch (IndexOutOfBoundsException e) {
//                logger.log(Level.WARN, "diff.commonAncestor() returns null value");
//            }
//
//            // sometimes one patch file patches multi-defects
//            for (FeatureVector featureVector : featureVectors) {
//                score += featureVector.score(parameterVector);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return score;
//    }
//
//    public List<File> rankPatches(File oldFile, List<File> newFiles, ParameterVector parameterVector) {
//        List<Map.Entry<File, Double>> entryList = new ArrayList<>();
//        for (File newFile : newFiles) {
//            entryList.add(new HashMap.SimpleEntry<>(newFile, scorePatch(oldFile, newFile, parameterVector)));
//        }
//        entryList.sort(Comparator.comparingDouble(Map.Entry::getValue));
//        return entryList.stream().map(Map.Entry::getKey).collect(Collectors.toList());
//    }
}
