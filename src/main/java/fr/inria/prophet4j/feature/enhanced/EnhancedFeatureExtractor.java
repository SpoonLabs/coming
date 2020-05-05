package fr.inria.prophet4j.feature.enhanced;

import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.feature.Feature.Position;
import fr.inria.prophet4j.feature.FeatureExtractor;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.Repair;
import fr.inria.prophet4j.feature.enhanced.util.EnhancedFeatureVisitor;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature.AtomicFeature;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature.CrossType;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature.RepairFeature;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature.ValueFeature;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

// based on FeatureExtract.cpp, RepairGenerator.cpp
public class EnhancedFeatureExtractor implements FeatureExtractor {

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
        if (valueStr.contains("get"))
            valueFeatures.add(ValueFeature.LI_GET_VF);
        if (valueStr.contains("size"))
            valueFeatures.add(ValueFeature.LI_SIZE_VF);
        if (valueStr.contains("length"))
            valueFeatures.add(ValueFeature.LI_LENGTH_VF);
        if (valueStr.contains("equals"))
            valueFeatures.add(ValueFeature.LI_EQUALS_VF);
        if (E.getElements(new TypeFilter<>(CtField.class)).size() > 0)
            valueFeatures.add(ValueFeature.MEMBER_VF);
        if (E instanceof CtLiteral) {
            Object value = ((CtLiteral)E).getValue();
            if (value == null) {
                valueFeatures.add(ValueFeature.LV_NULL_VF);
            }
            if (value instanceof Byte) {
                if ((Byte) value == 0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_BYTE_VF);
            } else if (value instanceof Character) {
                if ((Character) value == 0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_CHAR_VF);
            } else if (value instanceof Short) {
                if ((Short) value == 0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_SHORT_VF);
            } else if (value instanceof Integer) {
                if ((Integer) value == 0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_INT_VF);
            } else if (value instanceof Long) {
                if ((Long) value == 0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_LONG_VF);
            } else if (value instanceof Float) {
                if ((Float) value == 0.0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_FLOAT_VF);
            } else if (value instanceof Double) {
                if ((Double) value == 0.0) {
                    valueFeatures.add(ValueFeature.LV_ZERO_VF);
                }
                valueFeatures.add(ValueFeature.LT_DOUBLE_VF);
            } else if (value instanceof Boolean) {
                valueFeatures.add(ValueFeature.LT_BOOLEAN_VF);
            } else if (value instanceof Enum) {
                valueFeatures.add(ValueFeature.LT_ENUM_VF);
            } else if (value instanceof String) {
                if (((String) value).equals("")) {
                    valueFeatures.add(ValueFeature.LV_BLANK_VF);
                }
                valueFeatures.add(ValueFeature.LT_STRING_VF);
            } else if (value instanceof List) {
                if (((List) value).isEmpty()) {
                    valueFeatures.add(ValueFeature.LV_EMPTY_VF);
                }
                valueFeatures.add(ValueFeature.LT_LIST_VF);
            } else if (value instanceof Map) {
                if (((Map) value).isEmpty()) {
                    valueFeatures.add(ValueFeature.LV_EMPTY_VF);
                }
                valueFeatures.add(ValueFeature.LT_MAP_VF);
            } else if (value instanceof Queue) {
                if (((Queue) value).isEmpty()) {
                    valueFeatures.add(ValueFeature.LV_EMPTY_VF);
                }
                valueFeatures.add(ValueFeature.LT_QUEUE_VF);
            } else if (value instanceof Set) {
                if (((Set) value).isEmpty()) {
                    valueFeatures.add(ValueFeature.LV_EMPTY_VF);
                }
                valueFeatures.add(ValueFeature.LT_SET_VF);
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
        Map<String, Set<AtomicFeature>> dstMap = new EnhancedFeatureVisitor(valueExprInfo).traverseRepair(repair, atom);

        for (CtElement stmt : stmtsC) {
            Map<String, Set<AtomicFeature>> map = new EnhancedFeatureVisitor(valueExprInfo).traverseStmt(stmt);
            map.forEach((k, v) -> srcMapC.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }
        for (CtElement stmt : stmtsF) {
            Map<String, Set<AtomicFeature>> map = new EnhancedFeatureVisitor(valueExprInfo).traverseStmt(stmt);
            map.forEach((k, v) -> srcMapF.merge(k, v, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }
        for (CtElement stmt : stmtsL) {
            Map<String, Set<AtomicFeature>> map = new EnhancedFeatureVisitor(valueExprInfo).traverseStmt(stmt);
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
            featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.RF_CT, features));
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
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_AF_RF_CT, globalFeatures));
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
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_AF_RF_CT, globalFeatures));
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
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_AF_RF_CT, globalFeatures));
                }
            }
        }

        // AtomFeatureNum * RepairFeatureNum
        for (Feature repairFeature : repairFeatures) {
            if (dstMap.containsKey("@")) {
                Set<AtomicFeature> atomicFeatures = dstMap.get("@");
                for (Feature atomicFeature : atomicFeatures) {
                    // AF_RF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(atomicFeature);
                    newlyAddedFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.AF_RF_CT, newlyAddedFeatures));
                }
            }
        }

        srcMapC.remove("@");
        srcMapF.remove("@");
        srcMapL.remove("@");
        dstMap.remove("@");

        // 3 * ValueFeatureNum * RepairFeatureNum
        for (Feature repairFeature : repairFeatures) {
            for (String key : srcMapC.keySet()) {
                Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
                for (Feature valueFeature : valueFeatures) {
                    // POS_VF_RF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(Position.POS_C);
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_VF_RF_CT, newlyAddedFeatures));
                }
            }
            for (String key : srcMapF.keySet()) {
                Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
                for (Feature valueFeature : valueFeatures) {
                    // POS_VF_RF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(Position.POS_F);
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_VF_RF_CT, newlyAddedFeatures));
                }
            }
            for (String key : srcMapL.keySet()) {
                Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
                for (Feature valueFeature : valueFeatures) {
                    // POS_VF_RF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(Position.POS_L);
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_VF_RF_CT, newlyAddedFeatures));
                }
            }
        }

        // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
        for (String key : dstMap.keySet()) {
            // what is the meaning of this block ?
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
                        featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_AF_AF_CT, varCrossFeatures));
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
                        featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_AF_AF_CT, varCrossFeatures));
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
                        featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_AF_AF_CT, varCrossFeatures));
                    }
                }
            }
        }

        // 3 * ValueFeatureNum * AtomFeatureNum
        for (String key : srcMapC.keySet()) {
            Set<AtomicFeature> atomicFeatures = srcMapC.get(key);
            Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
            for (Feature valueFeature : valueFeatures) {
                for (Feature atomicFeature : atomicFeatures) {
                    // POS_VF_AF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(Position.POS_C);
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(atomicFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_VF_AF_CT, newlyAddedFeatures));
                }
            }
        }
        for (String key : srcMapF.keySet()) {
            Set<AtomicFeature> atomicFeatures = srcMapF.get(key);
            Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
            for (Feature valueFeature : valueFeatures) {
                for (Feature atomicFeature : atomicFeatures) {
                    // POS_VF_AF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(Position.POS_F);
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(atomicFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_VF_AF_CT, newlyAddedFeatures));
                }
            }
        }
        for (String key : srcMapL.keySet()) {
            Set<AtomicFeature> atomicFeatures = srcMapL.get(key);
            Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
            for (Feature valueFeature : valueFeatures) {
                for (Feature atomicFeature : atomicFeatures) {
                    // POS_VF_AF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(Position.POS_L);
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(atomicFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.POS_VF_AF_CT, newlyAddedFeatures));
                }
            }
        }

        // ValueFeatureNum * RepairFeatureNum
        for (Feature repairFeature : repairFeatures) {
            for (String key : dstMap.keySet()) {
                Set<ValueFeature> valueFeatures = getValueFeature(key, repair, valueExprInfo);
                for (Feature valueFeature : valueFeatures) {
                    // VF_RF_CT
                    List<Feature> newlyAddedFeatures = new ArrayList<>();
                    newlyAddedFeatures.add(valueFeature);
                    newlyAddedFeatures.add(repairFeature);
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.VF_RF_CT, newlyAddedFeatures));
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
                    featureVector.addFeatureCross(new EnhancedFeatureCross(CrossType.AF_VF_CT, valueCrossFeature));
                }
            }
        }
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
            boolean found = false;
            for (CtStatement stmt: CS.getStatements()) {
                if (stmt.equals(srcElem)) {
                    found = true;
                    idx = tmp.size();
                }
                tmp.add(stmt);
            }
            assert(found);
            int s = 0;
            if (idx > LOOKUP_DIS)
                s = idx - LOOKUP_DIS;
            int e = tmp.size();
            if (idx + LOOKUP_DIS + 1 < tmp.size())
                e = idx + LOOKUP_DIS + 1;
            boolean above = true;
            for (int i = s; i < e; i++) {
                if (tmp.get(i).equals(srcElem)) {
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

	@Override
	public FeatureVector extractSimpleP4JFeature(Repair repair, CtElement atom) {
		// TODO Auto-generated method stub
		return null;
	}
}
