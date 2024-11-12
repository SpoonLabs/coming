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
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.DiffEntry;
import fr.inria.prophet4j.utility.Structure.Repair;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeatureExtractor;
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
import org.eclipse.jgit.errors.NotSupportedException;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// based on pdiffer.cpp, ASTDiffer.cpp
public class CodeDiffer {

    private boolean byGenerator;
    private Option option;
    private String pathName;
    private boolean cross=true;
    private static final Logger logger = LogManager.getLogger(CodeDiffer.class.getName());

    public CodeDiffer(boolean byGenerator, Option option) {
        this.byGenerator = byGenerator;
        this.option = option;
        this.pathName = "";
    }
    
    public CodeDiffer(boolean byGenerator, Option option, boolean cross) {
        this.byGenerator = byGenerator;
        this.option = option;
        this.pathName = "";
        this.cross = cross;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    private FeatureExtractor newFeatureExtractor() {
        FeatureExtractor featureExtractor = null;
        switch (option.featureOption) {
            case ENHANCED:
                featureExtractor = new EnhancedFeatureExtractor();
                break;
            case EXTENDED:
                throw new RuntimeException("class removed by Martin for cleaning");
            case ORIGINAL:
                featureExtractor = new OriginalFeatureExtractor();
                break;
            case S4R:
                logger.warn("S4R should not call newFeatureExtractor");
                break;
            case S4RO:
                throw new RuntimeException("removed see https://github.com/SpoonLabs/coming/issues/235");
        }
        return featureExtractor;
    }

    private RepairGenerator newRepairGenerator(DiffEntry diffEntry) {
        RepairGenerator repairGenerator = null;
        switch (option.featureOption) {
            case ENHANCED:
                throw new RuntimeException("class removed by Martin was exact duplicate of ExtendedRepairGenerator");
            case EXTENDED:
                throw new RuntimeException("class removed by Martin for cleaning");
            case ORIGINAL:
                repairGenerator = new OriginalRepairGenerator(diffEntry);
                break;
            case S4R:
                throw new RuntimeException("S4R should not call newRepairGenerator");
            case S4RO:
                throw new RuntimeException("removed see https://github.com/SpoonLabs/coming/issues/235");
        }
        return repairGenerator;
    }

    private List<DiffEntry> genDiffEntries(Diff diff) throws IndexOutOfBoundsException {
        List<DiffEntry> diffEntries = new ArrayList<>();
        List<Operation> operations = diff.getRootOperations();
        Map<Integer, Operation> deleteOperations = new HashMap<>();
        Map<Integer, Operation> insertOperations = new HashMap<>();
        // tmp wrapper for gumtree-spoon-ast-diff
        // may be affected by future versions of gumtree-spoon-ast-diff
        for (Operation operation : operations) {
            Pattern pattern = Pattern.compile(":(\\d+)");
            Matcher matcher = pattern.matcher(operation.toString());
            if (operation instanceof DeleteOperation) {
                if (matcher.find()) {
                    deleteOperations.put(Integer.valueOf(matcher.group(1)), operation);
                }
            } else if (operation instanceof InsertOperation) {
                if (matcher.find()) {
                    insertOperations.put(Integer.valueOf(matcher.group(1)), operation);
                }
            } else if (operation instanceof MoveOperation) {
                if (matcher.find()) {
                    deleteOperations.put(Integer.valueOf(matcher.group(1)), operation);
                }
                if (matcher.find()) {
                    insertOperations.put(Integer.valueOf(matcher.group(1)), operation);
                }
            } else if (operation instanceof UpdateOperation) {
                if (matcher.find()) {
                    deleteOperations.put(Integer.valueOf(matcher.group(1)), operation);
                    insertOperations.put(Integer.valueOf(matcher.group(1)), operation);
                }
            }
        }
        Set<Integer> lineNums = new HashSet<>();
        lineNums.addAll(deleteOperations.keySet());
        lineNums.addAll(insertOperations.keySet());
        Operation DEL = null;
        for (Integer lineNum : lineNums) {
        		Operation operation = deleteOperations.get(lineNum);       		
        		if(operation!=null && "delete-node".equals(operation.getAction().getName())) {
        			DEL = operation;
        		}       		 
        	    Operation deleteOperation = deleteOperations.get(lineNum);
            Operation insertOperation = insertOperations.get(lineNum);

            DiffType type = null;
            CtElement srcNode = null;
            CtElement dstNode = null;
            if (deleteOperation != null && insertOperation != null) {
                type = DiffType.UpdateType;
                srcNode = deleteOperation.getSrcNode(); // ...
                dstNode = insertOperation.getDstNode(); // ...
                if (insertOperation instanceof InsertOperation) {
                    dstNode = insertOperation.getSrcNode(); // ...
                }
            } else if (deleteOperation != null && DEL != null && insertOperation == null ) {
            		Boolean pureDelete = true;
            		for(Operation op :operations) {
            			if(!"DEL".equals(op.getAction().getName())) {
            				pureDelete = false;
            			}
            		}
            		if(pureDelete) {
            			type = DiffType.DeleteType;
            		}else {
            			type = DiffType.PartialDeleteType;
            		}                                            
                srcNode = DEL.getSrcNode(); // ...
                dstNode = deleteOperation.getDstNode(); // null
                if (srcNode == null) srcNode = dstNode;
                if (dstNode == null) dstNode = srcNode;
            } else if (insertOperation != null) {
                type = DiffType.InsertType;
                srcNode = insertOperation.getSrcNode(); // ...
                dstNode = insertOperation.getDstNode(); // null
                if (srcNode == null) srcNode = dstNode;
                if (dstNode == null) dstNode = srcNode;
            }
            // distinguish functionality changes from revision changes
            if (srcNode instanceof CtClass || srcNode instanceof CtMethod ||
                    dstNode instanceof CtClass || dstNode instanceof CtMethod) {
                continue;
            }
            diffEntries.add(new DiffEntry(type, srcNode, dstNode));
        }
        return diffEntries;
    }

    // this is only for compatible with S4R, therefore we do not handle MoveOperation
    private List<DiffEntry> genDiffEntry(Operation operation) throws IndexOutOfBoundsException {
        List<DiffEntry> diffEntries = new ArrayList<>();
        Map<Integer, Operation> deleteOperations = new HashMap<>();
        Map<Integer, Operation> insertOperations = new HashMap<>();
        // tmp wrapper for gumtree-spoon-ast-diff
        // may be affected by future versions of gumtree-spoon-ast-diff
        Pattern pattern = Pattern.compile(":(\\d+)");
        Matcher matcher = pattern.matcher(operation.toString());
        if (operation instanceof DeleteOperation) {
            if (matcher.find()) {
                deleteOperations.put(Integer.valueOf(matcher.group(1)), operation);
            }
        } else if (operation instanceof InsertOperation) {
            if (matcher.find()) {
                insertOperations.put(Integer.valueOf(matcher.group(1)), operation);
            }
        } else if (operation instanceof MoveOperation) {
            if (matcher.find()) {
                deleteOperations.put(Integer.valueOf(matcher.group(1)), operation);
            }
            if (matcher.find()) {
                insertOperations.put(Integer.valueOf(matcher.group(1)), operation);
            }
        } else if (operation instanceof UpdateOperation) {
            if (matcher.find()) {
                deleteOperations.put(Integer.valueOf(matcher.group(1)), operation);
                insertOperations.put(Integer.valueOf(matcher.group(1)), operation);
            }
        }

        Set<Integer> lineNums = new HashSet<>();
        lineNums.addAll(deleteOperations.keySet());
        lineNums.addAll(insertOperations.keySet());
        for (Integer lineNum : lineNums) {
            Operation deleteOperation = deleteOperations.get(lineNum);
            Operation insertOperation = insertOperations.get(lineNum);

            DiffType type = null;
            CtElement srcNode = null;
            CtElement dstNode = null;
            if (deleteOperation != null && insertOperation != null) {
                type = DiffType.UpdateType;
                srcNode = deleteOperation.getSrcNode(); // ...
                dstNode = insertOperation.getDstNode(); // ...
                if (insertOperation instanceof InsertOperation) {
                    dstNode = insertOperation.getSrcNode(); // ...
                }
            } else if (deleteOperation != null) {
                type = DiffType.DeleteType;
                srcNode = deleteOperation.getSrcNode(); // ...
                dstNode = deleteOperation.getDstNode(); // null
                if (srcNode == null) srcNode = dstNode;
                if (dstNode == null) dstNode = srcNode;
            } else if (insertOperation != null) {
                type = DiffType.InsertType;
                srcNode = insertOperation.getSrcNode(); // ...
                dstNode = insertOperation.getDstNode(); // null
                if (srcNode == null) srcNode = dstNode;
                if (dstNode == null) dstNode = srcNode;
            }
            // distinguish functionality changes from revision changes
            if (srcNode instanceof CtClass || srcNode instanceof CtMethod ||
                    dstNode instanceof CtClass || dstNode instanceof CtMethod) {
                continue;
            }
            diffEntries.add(new DiffEntry(type, srcNode, dstNode));
        }
        return diffEntries;
    }


    private List<FeatureMatrix> genFeatureMatrices(Diff diff, String fileKey) {
        List<FeatureMatrix> featureMatrices = new ArrayList<>();
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
                List<FeatureVector> featureVectors = new ArrayList<>();
                for (JsonElement featuresOfOperation : featuresOperationList) {
                    // the first one in newFiles is human patch
                    FeatureVector featureVector = new FeatureVector();
                    JsonObject jso = featuresOfOperation.getAsJsonObject();
                    for (S4RFeature.CodeFeature codeFeature : S4RFeature.CodeFeature.values()) {
                        JsonElement property = jso.get(codeFeature.toString());
                        if (property != null) {
                            try {
                                JsonPrimitive value = property.getAsJsonPrimitive();
                                String str = value.getAsString();

                                if (str.equalsIgnoreCase("true")) {
                                    // handle boolean-form features
                                    List<Feature> features = new ArrayList<>();
                                    features.add(codeFeature);
                                    FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, 1.0);
                                    featureVector.addFeatureCross(featureCross);
                                } else if (str.equalsIgnoreCase("false")) {
                                    // handle boolean-form features
                                    List<Feature> features = new ArrayList<>();
                                    features.add(codeFeature);
                                    FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, 0.0);
                                    featureVector.addFeatureCross(featureCross);
                                } else {
                                    // handle numerical-form features
                                    try {
                                        double degree = Double.parseDouble(value.getAsString());
                                        List<Feature> features = new ArrayList<>();
                                        features.add(codeFeature);
                                        FeatureCross featureCross = new S4RFeatureCross(S4RFeature.CrossType.CF_CT, features, degree);
                                        featureVector.addFeatureCross(featureCross);
                                    } catch (Exception e) {
//                                        e.printStackTrace();
                                    }
                                }
                            } catch (IllegalStateException e) {
//                                logger.error("Not a JSON Primitive");
                            }
                        }
                    }
                    featureVectors.add(featureVector);
                }
                featureMatrices.add(new FeatureMatrix(true, fileKey, featureVectors));
            } else if (option.featureOption == FeatureOption.S4RO) {
                throw new RuntimeException("removed see https://github.com/SpoonLabs/coming/issues/235");
            } else {
                // RepairGenerator receive diffEntry as parameter, so we do not need ErrorLocalizer
                {
                    FeatureExtractor featureExtractor = newFeatureExtractor();
                    List<FeatureVector> featureVectors = new ArrayList<>();
                    for (DiffEntry diffEntry : genDiffEntries(diff)) {
                    		if (diffEntry==null|| diffEntry.srcNode==null) {
                    			continue;
                    		}
                        RepairGenerator generator = newRepairGenerator(diffEntry);
                        {
                            Repair repair = generator.obtainHumanRepair();
                            FeatureVector featureVector = new FeatureVector();
                            for (CtElement atom : repair.getCandidateAtoms()) {
                            		if(cross) {
                                featureVector.merge(featureExtractor.extractFeature(repair, atom));
                            		} else {
                            			featureVector = featureExtractor.extractSimpleP4JFeature(repair, atom);
                            		}
                            }
                            featureVectors.add(featureVector);
                        }
                    }
                    featureMatrices.add(new FeatureMatrix(true, fileKey, featureVectors));
                }
                if (byGenerator) {
                    // only in this case, featureMatrices.size() > 1
                    // we only consider this case where each SPR repair owns one diffEntry
                    // as we learn by comparing feature-vectors and evaluate by feature-matrix scores
                    // also we do not need to consider the potential issue of combinatorial explosion
                    FeatureExtractor featureExtractor = newFeatureExtractor();
                    for (DiffEntry diffEntry : genDiffEntries(diff)) {
                        RepairGenerator generator = newRepairGenerator(diffEntry);
                        for (Repair repair : generator.obtainRepairCandidates()) {
                            for (CtElement atom : repair.getCandidateAtoms()) {
                                List<FeatureVector> featureVectors = new ArrayList<>();
                                featureVectors.add(featureExtractor.extractFeature(repair, atom));
                                featureMatrices.add(new FeatureMatrix(false, fileKey, featureVectors));
                            }
//                            List<FeatureVector> featureVectors = new ArrayList<>();
//                            FeatureVector featureVector = new FeatureVector();
//                            for (CtElement atom : repair.getCandidateAtoms()) {
//                                featureVector.merge(featureExtractor.extractFeature(repair, atom));
//                            }
//                            featureVectors.add(featureVector);
//                            featureMatrices.add(new FeatureMatrix(false, fileKey, featureVectors));
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            logger.log(Level.WARN, "diff.commonAncestor() returns null value");
        }
        return featureMatrices;
    }

    // for DataLoader, we do not need to obtainRepairCandidates as they are given
    // byGenerator = false as long as this func gets called
    public List<FeatureMatrix> runByPatches(File oldFile, List<File> newFiles) {
        List<FeatureMatrix> featureMatrices = new ArrayList<>();
        for (File newFile : newFiles) {
            featureMatrices.addAll(runByGenerator(oldFile, newFile));
        }
        // correct the prop "marked" for all featureMatrices except for the first one
        for (int idx = 1; idx < featureMatrices.size(); idx++) {
            featureMatrices.get(idx).correctMarked();
        }
        return featureMatrices;
    }

    public List<FeatureMatrix> runByGenerator(File oldFile, File newFile) {
        //System.out.println("oldFile: " + oldFile.getPath());
        List<FeatureMatrix> featureMatrices = new ArrayList<>();
        try {
            AstComparator comparator = new AstComparator();
            Diff diff = comparator.compare(oldFile, newFile);
            String filePath = newFile.getPath();
            int leftIndex = filePath.indexOf(pathName) + pathName.length();
            int rightIndex = filePath.lastIndexOf("/");
            String fileKey = filePath.substring(leftIndex + 1, rightIndex);
            fileKey = fileKey.replace("/", "-");
            System.out.println("diff "+oldFile.getAbsolutePath()+" "+newFile.getAbsolutePath());
            featureMatrices.addAll(genFeatureMatrices(diff, fileKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureMatrices;
    }

    // for FeatureExtractorTest.java
    public List<FeatureMatrix> runByGenerator(String oldStr, String newStr) {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(oldStr, newStr);
        //System.out.println(diff.toString());
        List<FeatureMatrix> featureMatrices = genFeatureMatrices(diff, "");
        assert featureMatrices.size() == 1;
        return featureMatrices;
    }
}
