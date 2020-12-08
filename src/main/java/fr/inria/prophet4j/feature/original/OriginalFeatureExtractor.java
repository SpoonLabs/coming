package fr.inria.prophet4j.feature.original;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.feature.Feature.Position;
import fr.inria.prophet4j.feature.original.OriginalFeature.CrossType;
import fr.inria.prophet4j.feature.original.OriginalFeature.AtomicFeature;
import fr.inria.prophet4j.feature.original.OriginalFeature.RepairFeature;
import fr.inria.prophet4j.feature.original.OriginalFeature.ValueFeature;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.Repair;
import fr.inria.prophet4j.feature.original.util.OriginalFeatureVisitor;
import fr.inria.prophet4j.feature.FeatureExtractor;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

// based on FeatureExtract.cpp, RepairGenerator.cpp
public class OriginalFeatureExtractor implements FeatureExtractor {

    private Map<String, CtElement> valueExprInfo = new HashMap<>();

    private EnumSet<RepairFeature> getRepairFeatures(Repair repair) {
        EnumSet<RepairFeature> repairFeatures = EnumSet.noneOf(RepairFeature.class);
        if (repair.kind == null) {
            // I used null in FeatureExtractorTest.java
            return repairFeatures;
        }
        switch (repair.kind) {
            case IfExitKind:
                // RepairFeature.INSERT_CONTROL_RF == AddControlRepair
                repairFeatures.add(RepairFeature.INSERT_CONTROL_RF);
                break;
            case GuardKind:
            case SpecialGuardKind:
                // RepairFeature.INSERT_GUARD_RF == GuardRepair
                repairFeatures.add(RepairFeature.INSERT_GUARD_RF);
                break;
            case AddInitKind:
            case AddAndReplaceKind:
                // RepairFeature.INSERT_STMT_RF == AddStmtRepair
                repairFeatures.add(RepairFeature.INSERT_STMT_RF);
                break;
            case TightenConditionKind:
            case LoosenConditionKind:
                // RepairFeature.REPLACE_COND_RF == CondRepair
                repairFeatures.add(RepairFeature.REPLACE_COND_RF);
                break;
            case ReplaceKind:
            case ReplaceStringKind:
                // RepairFeature.REPLACE_STMT_RF == ReplaceStmtRepair
                repairFeatures.add(RepairFeature.REPLACE_STMT_RF);
                break;
             //Delete a guard in source file
            case RemovePartialIFKind:
            	 	repairFeatures.add(RepairFeature.REMOVE_PARTIAL_IF);
                break;
             //Delete statements in source file
            case RemoveSTMTKind:
	        	 	repairFeatures.add(RepairFeature.REMOVE_STMT);
	            break;
            case RemoveWholeIFKind:
        	 	repairFeatures.add(RepairFeature.REMOVE_WHOLE_IF);
            break;
            case RemoveWholeBlockKind:
        	 	repairFeatures.add(RepairFeature.REMOVE_WHOLE_BLOCK);
            break;
        }
        return repairFeatures;
    }

    private EnumSet<ValueFeature> getValueFeature(final String valueStr, final Repair repair, Map<String, CtElement> valueExprInfo) {
        EnumSet<ValueFeature> valueFeatures = EnumSet.noneOf(ValueFeature.class);
        if (repair.oldRExpr != null && repair.newRExpr != null) {
            String oldStr = repair.oldRExpr.toString();
            String newStr = repair.newRExpr.toString();
            if (valueStr.equals(newStr))
                valueFeatures.add(ValueFeature.MODIFIED_VF);
            // I can not figure out the meaning of MODIFIED_SIMILAR_VF
            if (oldStr.length() > 0 && newStr.length() > 0) {
                double ratio = ((double)oldStr.length()) / newStr.length();
                if (ratio > 0.5 && ratio < 2 && oldStr.length() > 3 && newStr.length() > 3)
                    if (oldStr.contains(newStr) || newStr.contains(oldStr))
                        valueFeatures.add(ValueFeature.MODIFIED_SIMILAR_VF);
            }
        }
        CtElement element = repair.dstElem;
        if (element != null) {
            CtMethod FD = element.getParent(new TypeFilter<>(CtMethod.class));
            if (FD != null) {
                for (Object parameter: FD.getParameters()) {
                    if (parameter instanceof CtParameter) {
                        CtParameter VD = (CtParameter) parameter;
                        if (VD.getSimpleName().equals(valueStr))
                            valueFeatures.add(ValueFeature.FUNC_ARGUMENT_VF);
                    }
                }
            }
        }
        assert(valueExprInfo.containsKey(valueStr));
        CtElement E = valueExprInfo.get(valueStr);
        if (E instanceof CtVariableAccess || E instanceof CtArrayAccess || E instanceof CtLocalVariable) {
            if (E instanceof CtLocalVariable) {
                valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
            } else {
                valueFeatures.add(ValueFeature.GLOBAL_VARIABLE_VF);
            }
        } else if (E instanceof CtExecutableReference){
            // just make CALLEE_AF be meaningful
            if (((CtExecutableReference) E).getParameters().size() > 0){
                valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
            }
        } else if (E instanceof CtIf){
            // just make R_STMT_COND_AF be meaningful
            valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
        }
//        if (E instanceof CtVariable) {
//            if (E instanceof CtLocalVariable)
//                valueFeatures.add(SchemaFeature.LOCAL_VARIABLE_VF);
//            else
//                valueFeatures.add(SchemaFeature.GLOBAL_VARIABLE_VF);
//        } else if (E instanceof CtVariableReference) {
//            if (E instanceof CtLocalVariableReference)
//                valueFeatures.add(SchemaFeature.LOCAL_VARIABLE_VF);
//            else
//                valueFeatures.add(SchemaFeature.GLOBAL_VARIABLE_VF);
//        }
        if (valueStr.contains("length") || valueStr.contains("size"))
            valueFeatures.add(ValueFeature.SIZE_LITERAL_VF);
        if (E.getElements(new TypeFilter<>(CtField.class)).size() > 0)
            valueFeatures.add(ValueFeature.MEMBER_VF);
        if (E instanceof CtLiteral) {
            Object value = ((CtLiteral)E).getValue();
            if (value instanceof String) {
                valueFeatures.add(ValueFeature.STRING_LITERAL_VF);
            } else if (value instanceof Integer) { // ?
                if ((Integer) value == 0) {
                    valueFeatures.add(ValueFeature.ZERO_CONST_VF);
                } else {
                    valueFeatures.add(ValueFeature.NONZERO_CONST_VF);
                }
            }
        }
        return valueFeatures;
    }

    // this is for CodeDiffer.java
    public FeatureVector extractFeature(Repair repair, CtElement atom) {
        List<CtElement> stmtsC = getCurrentStmts(repair);
        List<CtElement> stmtsF = new ArrayList<>();
        List<CtElement> stmtsL = new ArrayList<>();
        getNearbyStmts(repair, stmtsF, stmtsL); // update values by reference

        Map<String, Set<AtomicFeature>> srcMapC = new HashMap<>();
        Map<String, Set<AtomicFeature>> srcMapF = new HashMap<>();
        Map<String, Set<AtomicFeature>> srcMapL = new HashMap<>();
        Map<String, Set<AtomicFeature>> dstMap = new OriginalFeatureVisitor(valueExprInfo).traverseRepair(repair, atom);

        for (CtElement stmt : stmtsC) {
            Map<String, Set<AtomicFeature>> map = new OriginalFeatureVisitor(valueExprInfo).traverseStmt(stmt);
            map.forEach((k, v) -> srcMapC.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }
        for (CtElement stmt : stmtsF) {
            Map<String, Set<AtomicFeature>> map = new OriginalFeatureVisitor(valueExprInfo).traverseStmt(stmt);
            map.forEach((k, v) -> srcMapF.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }
        for (CtElement stmt : stmtsL) {
            Map<String, Set<AtomicFeature>> map = new OriginalFeatureVisitor(valueExprInfo).traverseStmt(stmt);
            map.forEach((k, v) -> srcMapL.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }

        // this will be merged so we do not care about this value named marked
        FeatureVector featureVector = new FeatureVector();
        // RepairFeatureNum     = RepairFeatureNum                      = 5
        EnumSet<RepairFeature> repairFeatures = getRepairFeatures(repair);
        // ModKind should be synonyms of RepairType
        for (Feature repairFeature : repairFeatures) {
            // RF_CT
            List<Feature> features = new ArrayList<>();
            features.add(repairFeature);
            featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.RF_CT, features));
        }

        // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 450
        for (Feature repairFeature : repairFeatures) {
            if (srcMapC.containsKey("@")) {
                Set<AtomicFeature> atomicFeatures = srcMapC.get("@");
                for (Feature atomicFeature : atomicFeatures) {
                    // POS_AF_RF_CT
                    List<Feature> globalFeatures = new ArrayList<>();
                    globalFeatures.add(Position.POS_C);
                    globalFeatures.add(atomicFeature);
                    globalFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.POS_AF_RF_CT, globalFeatures));
                }
            }
            if (srcMapF.containsKey("@")) {
                Set<AtomicFeature> atomicFeatures = srcMapF.get("@");
                for (Feature atomicFeature : atomicFeatures) {
                    // POS_AF_RF_CT
                    List<Feature> globalFeatures = new ArrayList<>();
                    globalFeatures.add(Position.POS_F);
                    globalFeatures.add(atomicFeature);
                    globalFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.POS_AF_RF_CT, globalFeatures));
                }
            }
            if (srcMapL.containsKey("@")) {
                Set<AtomicFeature> atomicFeatures = srcMapL.get("@");
                for (Feature atomicFeature : atomicFeatures) {
                    // POS_AF_RF_CT
                    List<Feature> globalFeatures = new ArrayList<>();
                    globalFeatures.add(Position.POS_L);
                    globalFeatures.add(atomicFeature);
                    globalFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.POS_AF_RF_CT, globalFeatures));
                }
            }
        }
        srcMapC.remove("@");
        srcMapF.remove("@");
        srcMapL.remove("@");
        dstMap.remove("@");

        // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
        for (String key : dstMap.keySet()) {
            if (valueExprInfo.containsKey(key)) {
                CtElement E = valueExprInfo.get(key);
                if (E instanceof CtLiteral)
                    if (((CtLiteral)E).getValue() instanceof Integer) // ?
                        continue;
            }
            if (srcMapC.containsKey(key)) {
                Set<AtomicFeature> dstAtomicFeatures = dstMap.get(key);
                Set<AtomicFeature> srcAtomicFeatures = srcMapC.get(key);
                for (Feature dstAtomicFeature : dstAtomicFeatures) {
                    for (Feature srcAtomicFeature : srcAtomicFeatures) {
                        // POS_AF_AF_CT
                        List<Feature> varCrossFeatures = new ArrayList<>();
                        varCrossFeatures.add(Position.POS_C);
                        varCrossFeatures.add(srcAtomicFeature);
                        varCrossFeatures.add(dstAtomicFeature);
                        featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.POS_AF_AF_CT, varCrossFeatures));
                    }
                }
            }
            if (srcMapF.containsKey(key)) {
                Set<AtomicFeature> dstAtomicFeatures = dstMap.get(key);
                Set<AtomicFeature> srcAtomicFeatures = srcMapF.get(key);
                for (Feature dstAtomicFeature : dstAtomicFeatures) {
                    for (Feature srcAtomicFeature : srcAtomicFeatures) {
                        // POS_AF_AF_CT
                        List<Feature> varCrossFeatures = new ArrayList<>();
                        varCrossFeatures.add(Position.POS_F);
                        varCrossFeatures.add(srcAtomicFeature);
                        varCrossFeatures.add(dstAtomicFeature);
                        featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.POS_AF_AF_CT, varCrossFeatures));
                    }
                }
            }
            if (srcMapL.containsKey(key)) {
                Set<AtomicFeature> dstAtomicFeatures = dstMap.get(key);
                Set<AtomicFeature> srcAtomicFeatures = srcMapL.get(key);
                for (Feature dstAtomicFeature : dstAtomicFeatures) {
                    for (Feature srcAtomicFeature : srcAtomicFeatures) {
                        // POS_AF_AF_CT
                        List<Feature> varCrossFeatures = new ArrayList<>();
                        varCrossFeatures.add(Position.POS_L);
                        varCrossFeatures.add(srcAtomicFeature);
                        varCrossFeatures.add(dstAtomicFeature);
                        featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.POS_AF_AF_CT, varCrossFeatures));
                    }
                }
            }
        }

        // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360
        for (String key : dstMap.keySet()) {
            Set<AtomicFeature> atomicFeatures = dstMap.get(key);
            Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
            for (Feature atomicFeature : atomicFeatures) {
                for (Feature valueFeature : valueFeatures) {
                    // AF_VF_CT
                    List<Feature> valueCrossFeature = new ArrayList<>();
                    valueCrossFeature.add(atomicFeature);
                    valueCrossFeature.add(valueFeature);
                    featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.AF_VF_CT, valueCrossFeature));
                }
            }
        }
        return featureVector;
    }
    
	/**
	 * This function returns simple P4J features
	 */
	public FeatureVector extractSimpleP4JFeature(Repair repair, CtElement atom) {
		List<CtElement> stmtsC = getCurrentStmts(repair);
		List<CtElement> stmtsF = new ArrayList<>();
		List<CtElement> stmtsL = new ArrayList<>();
		getNearbyStmts(repair, stmtsF, stmtsL);

		Map<String, Set<AtomicFeature>> srcMapC = new HashMap<>();
		Map<String, Set<AtomicFeature>> srcMapF = new HashMap<>();
		Map<String, Set<AtomicFeature>> srcMapL = new HashMap<>();

		for (CtElement stmt : stmtsC) {
			Map<String, Set<AtomicFeature>> map = new OriginalFeatureVisitor(valueExprInfo).traverseStmt(stmt);
			map.forEach((k, v) -> srcMapC.merge(k, v, (v1, v2) -> {
				v1.addAll(v2);
				return v1;
			}));
		}
		for (CtElement stmt : stmtsF) {
			Map<String, Set<AtomicFeature>> map = new OriginalFeatureVisitor(valueExprInfo).traverseStmt(stmt);
			map.forEach((k, v) -> srcMapF.merge(k, v, (v1, v2) -> {
				v1.addAll(v2);
				return v1;
			}));
		}
		for (CtElement stmt : stmtsL) {
			Map<String, Set<AtomicFeature>> map = new OriginalFeatureVisitor(valueExprInfo).traverseStmt(stmt);
			map.forEach((k, v) -> srcMapL.merge(k, v, (v1, v2) -> {
				v1.addAll(v2);
				return v1;
			}));
		}

		FeatureVector featureVector = new FeatureVector();

		List<Feature> CFeatures = new ArrayList<>();
		List<Feature> FFeatures = new ArrayList<>();
		List<Feature> LFeatures = new ArrayList<>();

		/**
		 * current position
		 */
		// repair features
		EnumSet<RepairFeature> repairFeatures = getRepairFeatures(repair);
		for (Feature repairFeature : repairFeatures) {
			CFeatures.add(repairFeature);
		}
		// atomic and value features
		for (String key : srcMapC.keySet()) {
			Set<AtomicFeature> atomicFeatures = srcMapC.get(key);
			for (AtomicFeature af : atomicFeatures) {
				if (!CFeatures.contains(af)) {
					CFeatures.add(af);
				}
			}
			if (!key.contains("@")) {
				Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
				for (ValueFeature vf : valueFeatures) {
					if (!CFeatures.contains(vf)) {
						CFeatures.add(vf);
					}
				}
			}
		}

		/**
		 * former position
		 */
		
		for (String key : srcMapF.keySet()) {
			Set<AtomicFeature> atomicFeatures = srcMapF.get(key);
			for (AtomicFeature af : atomicFeatures) {
				if (!FFeatures.contains(af)) {
					FFeatures.add(af);
				}
			}
		}
		
		/**
		 * later position
		 */

		for (String key : srcMapL.keySet()) {
			Set<AtomicFeature> atomicFeatures = srcMapL.get(key);
			for (AtomicFeature af : atomicFeatures) {
				if (!LFeatures.contains(af)) {
					LFeatures.add(af);
				}
			}
		}

		featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.SRC, CFeatures));
		featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.FORMER, FFeatures));
		featureVector.addFeatureCross(new OriginalFeatureCross(CrossType.LATER, LFeatures));

		return featureVector;
	}

    
    private List<CtElement> getCurrentStmts(Repair repair) {
        List<CtElement> ret = new ArrayList<>();
        CtElement srcElem = repair.srcElem;
        if (!repair.isReplace) {
            ret.add(srcElem);
            return ret;
        }
        else {
            ret.add(srcElem);
            CtStatement ElseB = null;
            if (repair.dstElem instanceof CtIf) {
                CtIf IFS = (CtIf) repair.dstElem;
                if (IFS.getThenStatement() instanceof CtStatementList) {
                    CtStatementList CS = IFS.getThenStatement();
                    ret.addAll(CS.getStatements());
                }
                else
                    ret.add(IFS.getThenStatement());
                ElseB = IFS.getElseStatement();
                if (ElseB != null) {
                    if (ElseB instanceof CtStatementList) {
                        CtStatementList CS = IFS.getThenStatement();
                        ret.addAll(CS.getStatements());
                    }
                    else
                        ret.add(ElseB);
                }
            }
            if (ElseB==null) {
                CtElement parent = srcElem.getParent();
                if (parent instanceof CtStatementList) {
                    CtStatementList CS = (CtStatementList) parent;
                    boolean found = false;
                    for (CtStatement stmt : CS.getStatements()) {
                        if (found) {
                            ret.add(stmt);
                            break;
                        }
                        if (stmt.equals(srcElem))
                            found = true;
                    }
                }
            }
            return ret;
        }
    }

    private void getNearbyStmts(Repair repair, List<CtElement> stmtsF, List<CtElement> stmtsL) {
        final int LOOKUP_DIS = 3;
        CtElement srcElem = repair.srcElem;
        CtElement parent = srcElem.getParent();
        if (parent instanceof CtStatementList) {
            CtStatementList CS = (CtStatementList) parent;
            List<CtStatement> tmp = new ArrayList<>();
            int idx = 0;
            for (CtStatement stmt: CS.getStatements()) {
                if (stmt.equals(srcElem)) {
                    idx = tmp.size();
                }
                tmp.add(stmt);
            }
            int s = 0;
            if (idx > LOOKUP_DIS)
                s = idx - LOOKUP_DIS;
            int e = tmp.size();
            if (idx + LOOKUP_DIS + 1 < tmp.size())
                e = idx + LOOKUP_DIS + 1;
            boolean above = true;
            for (int i = s; i < e; i++) {
                if (!tmp.get(i).equals(srcElem)) {
                    if (above)
                        stmtsF.add(tmp.get(i));
                    else
                        stmtsL.add(tmp.get(i));
                }
                if (tmp.get(i).equals(srcElem))
                    above = false;
            }
        }
        if (!repair.isReplace)
            stmtsL.add(srcElem);
    }
}