package prophet4j.feature;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import picocli.CommandLine.Option;
import prophet4j.defined.FeatureStruct.Cache;
import prophet4j.defined.FeatureStruct.Feature;
import prophet4j.defined.FeatureStruct.FeatureManager;
import prophet4j.defined.FeatureStruct.ValueToFeatureMapTy;
import prophet4j.defined.FeatureType;
import prophet4j.defined.FeatureType.AtomicFeature;
import prophet4j.defined.FeatureType.JointType;
import prophet4j.defined.FeatureType.Position;
import prophet4j.defined.FeatureType.RepairFeature;
import prophet4j.defined.FeatureType.ValueFeature;
import prophet4j.defined.RepairStruct.Repair;
import prophet4j.defined.RepairType.RepairActionKind;
import prophet4j.defined.RepairType.RepairCandidateKind;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

// this is the new implementation following the implementation of Prophet4C
// based on FeatureExtract.cpp, RepairGenerator.cpp
public class FeatureExtractor {

    // check these variables
    @Option(names = {"--disable-mod"}, description = "Disable Modification Features.")
    private boolean DisableModificationFeatures = false;
    @Option(names = {"--disable-sema-cross"}, description = "Disable Semantic Features, setting them all zero!")
    private boolean DisableSemanticCrossFeatures = false;
    @Option(names = {"--disable-sema-value"}, description = "Disable Semantic-Value Features, setting them all zero!")
    private boolean DisableSemanticValueFeatures = false;

    Map<String, CtElement> valueExprInfo = new HashMap<>();
    Cache cache = new Cache();

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
//        System.out.println("----getRepairTypes----");
//        System.out.println(repairFeatures);
        return repairFeatures;
    }

    private EnumSet<ValueFeature> getValueFeature(final String v_str, final Repair repair, CtElement abst_v, Map<String, CtElement> valueExprInfo) {
        EnumSet<ValueFeature> valueFeatures = EnumSet.noneOf(ValueFeature.class);
        CtElement element = repair.actions.get(0).ast_node;
    /*if (abst_v != NULL) {
        std::string abst_v_str = stmtToString(*ast, abst_v);
        if (abst_v_str == v_str)
            ret.insert(AbstCondVF);
    }*/
        if (repair.oldRExpr != null && repair.newRExpr != null) {
            String old_v_str = repair.oldRExpr.toString();
            String new_v_str = repair.newRExpr.toString();
            if (v_str.equals(new_v_str))
                valueFeatures.add(ValueFeature.MODIFIED_VF);
            if (old_v_str.length()>0 && new_v_str.length()>0) {
                double ratio = ((double)old_v_str.length()) / new_v_str.length();
                if (ratio > 0.5 && ratio < 2 && old_v_str.length() > 3 && new_v_str.length() > 3)
                    if (old_v_str.contains(new_v_str) || new_v_str.contains(old_v_str))
                        valueFeatures.add(ValueFeature.MODIFIED_SIMILAR_VF);
            }
        }
        CtMethod FD = element.getParent(new TypeFilter<>(CtMethod.class));
        if (FD != null) {
            for (Object it: FD.getParameters()) {
                if (it instanceof CtParameter) {
                    CtParameter VD = (CtParameter) it;
                    if (VD.getSimpleName().equals(v_str))
                        valueFeatures.add(ValueFeature.FUNC_ARGUMENT_VF);
                }
            }
        }
        if (v_str.contains("length") || v_str.contains("size"))
            valueFeatures.add(ValueFeature.SIZE_LITERAL_VF);
        assert(valueExprInfo.containsKey(v_str));
//        CtStatement E = stripParenAndCast(valueExprInfo.get(v_str));
        CtElement E = valueExprInfo.get(v_str);
        System.out.println("<" + E + ">");
        // fixme: distinguish GLOBAL_VARIABLE_VF from LOCAL_VARIABLE_VF
        if (E instanceof CtVariableAccess || E instanceof CtArrayAccess) {
            valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
        }
//        if (E instanceof CtVariable) {
//            if (E instanceof CtLocalVariable)
//                valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
//            else
//                valueFeatures.add(ValueFeature.GLOBAL_VARIABLE_VF);
//        } else if (E instanceof CtVariableReference) {
//            if (E instanceof CtLocalVariableReference)
//                valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
//            else
//                valueFeatures.add(ValueFeature.GLOBAL_VARIABLE_VF);
//        }
        // fixme: i feel this is not correct
        if (E.getElements(new TypeFilter<>(CtField.class)).size() > 0)
            valueFeatures.add(ValueFeature.MEMBER_VF);
        if (E instanceof CtLiteral) {
            Object ev = ((CtLiteral)E).getValue();
            if (ev instanceof String) {
                valueFeatures.add(ValueFeature.STRING_LITERAL_VF);
            } else if (ev instanceof Integer) { // ?
                if ((Integer) ev == 0) {
                    valueFeatures.add(ValueFeature.ZERO_CONST_VF);
                } else {
                    valueFeatures.add(ValueFeature.NONZERO_CONST_VF);
                }
            }
        }

        // A special case, this should only happen when doing
        // string const replacement
        if (isAbstractStub(E)) {
            assert(repair.kind == RepairCandidateKind.ReplaceStringKind);
            valueFeatures.add(ValueFeature.MODIFIED_VF);
            valueFeatures.add(ValueFeature.STRING_LITERAL_VF);
        }
        return valueFeatures;
    }

    private List<CtElement> getStmtList(CtElement statement) {
        List<CtElement> stmtList = new ArrayList<>();
        if (statement instanceof CtClass || statement instanceof CtMethod) {
            stmtList.add(statement);
        } else {
            CtElement parent = statement.getParent();
            List<CtStatement> tmpList = parent.getElements(new TypeFilter<>(CtStatement.class));
            if (parent instanceof CtStatement) {
                tmpList.remove(0);
            }
            stmtList.addAll(tmpList);
        }
        return stmtList;
    }

    private int getPivot(List<CtElement> srcStmtList, List<CtElement> dstStmtList) {
        int pivot = Math.min(srcStmtList.size(), dstStmtList.size());
        for (int i = 0; i < Math.min(srcStmtList.size(), dstStmtList.size()); i++) {
            if (!srcStmtList.get(i).equals(dstStmtList.get(i))) {
                pivot = i;
                break;
            }
        }
        return pivot;
    }

    // this is for CodeDiffer.java
    public FeatureManager extractFeature(Repair repair, CtElement expr) {
        // getImmediateFollowStmts() getLocalStmts()
//        List<CtStatement> srcStmtList = getStmtList((CtStatement)diffEntry.srcElem);
//        List<CtStatement> dstStmtList = getStmtList((CtStatement)diffEntry.dstElem);
//        int pivot = getPivot(srcStmtList, dstStmtList);

        FeatureManager featureManager = new FeatureManager();
        ValueToFeatureMapTy resv = new ValueToFeatureMapTy();
        ValueToFeatureMapTy resv_loc = new ValueToFeatureMapTy();
        ValueToFeatureMapTy resv_loc1 = new ValueToFeatureMapTy();
        ValueToFeatureMapTy resv_loc2 = new ValueToFeatureMapTy();

        // RepairFeatureNum     = RepairFeatureNum                      = 5
        EnumSet<RepairFeature> repairFeatures = getRepairFeatures(repair);
        // ModKind should be synonyms of RepairType
        for (FeatureType repairFeature : repairFeatures) {
            // RF_JT
            List<FeatureType> features = new ArrayList<>();
            features.add(repairFeature);
            featureManager.addFeature(new Feature(JointType.RF_JT, features));
        }

        FeatureVisitor FEV = new FeatureVisitor(valueExprInfo);
        FEV.traverseRC(repair, expr);
        resv = FEV.getFeatureResult();
        System.out.println("!resv!");
//        System.out.println(resv.map);
        List<CtElement> loc_stmts = getImmediateFollowStmts(repair);
        List<CtElement> loc1_stmts = new ArrayList<>();
        List<CtElement> loc2_stmts = new ArrayList<>();
        getLocalStmts(repair, loc1_stmts, loc2_stmts);
        resv_loc.map.clear();
        resv_loc1.map.clear();
        resv_loc2.map.clear();
        for (CtElement it : loc_stmts) {
            if (cache.map.containsKey(it))
                orMap(resv_loc, cache.map.get(it));
            else {
                FEV = new FeatureVisitor(valueExprInfo);
                FEV.traverseStmt(it);
                ValueToFeatureMapTy resM = FEV.getFeatureResult();
                System.out.println("!resM!");
//                System.out.println(resM.map);
                orMap(resv_loc, resM);
                cache.map.put(it, resM);
            }
        }
        for (CtElement it : loc1_stmts) {
            if (cache.map.containsKey(it))
                orMap(resv_loc1, cache.map.get(it));
            else {
                FEV = new FeatureVisitor(valueExprInfo);
                FEV.traverseStmt(it);
                ValueToFeatureMapTy resM = FEV.getFeatureResult();
                System.out.println("!resM-1!");
//                System.out.println(resM.map);
                orMap(resv_loc1, resM);
                cache.map.put(it, resM);
            }
        }
        for (CtElement it : loc2_stmts) {
            if (cache.map.containsKey(it))
                orMap(resv_loc2, cache.map.get(it));
            else {
                FEV = new FeatureVisitor(valueExprInfo);
                FEV.traverseStmt(it);
                ValueToFeatureMapTy resM = FEV.getFeatureResult();
                System.out.println("!resM-2!");
//                System.out.println(resM.map);
                orMap(resv_loc2, resM);
                cache.map.put(it, resM);
            }
        }

        // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 450
        for (FeatureType repairFeature : repairFeatures) {
            if (resv_loc.map.containsKey("")) {
                Set<AtomicFeature> atomicFeatures = resv_loc.map.get("");
                for (FeatureType atomicFeature : atomicFeatures) {
                    // POS_AF_RF_JT
                    List<FeatureType> globalFeatures = new ArrayList<>();
                    globalFeatures.add(Position.POS_C);
                    globalFeatures.add(atomicFeature);
                    globalFeatures.add(repairFeature);
                    featureManager.addFeature(new Feature(JointType.POS_AF_RF_JT, globalFeatures));
                }
            }
            if (resv_loc1.map.containsKey("")) {
                Set<AtomicFeature> atomicFeatures = resv_loc1.map.get("");
                for (FeatureType atomicFeature : atomicFeatures) {
                    // POS_AF_RF_JT
                    List<FeatureType> globalFeatures = new ArrayList<>();
                    globalFeatures.add(Position.POS_P);
                    globalFeatures.add(atomicFeature);
                    globalFeatures.add(repairFeature);
                    featureManager.addFeature(new Feature(JointType.POS_AF_RF_JT, globalFeatures));
                }
            }
            if (resv_loc2.map.containsKey("")) {
                Set<AtomicFeature> atomicFeatures = resv_loc2.map.get("");
                for (FeatureType atomicFeature : atomicFeatures) {
                    // POS_AF_RF_JT
                    List<FeatureType> globalFeatures = new ArrayList<>();
                    globalFeatures.add(Position.POS_N);
                    globalFeatures.add(atomicFeature);
                    globalFeatures.add(repairFeature);
                    featureManager.addFeature(new Feature(JointType.POS_AF_RF_JT, globalFeatures));
                }
            }
        }
        resv.map.remove("");
        resv_loc.map.remove("");
        resv_loc1.map.remove("");
        resv_loc2.map.remove("");

        // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
        for (String key : resv.map.keySet()) {
            if (valueExprInfo.containsKey(key)) {
                CtElement E = valueExprInfo.get(key);
                if (E instanceof CtLiteral)
                    if (((CtLiteral)E).getValue() instanceof Integer) // ?
                        continue;
            }
            if (resv_loc.map.containsKey(key)) {
                Set<AtomicFeature> dstAtomicFeatures = resv.map.get(key);
                Set<AtomicFeature> srcAtomicFeatures = resv_loc.map.get(key);
                for (FeatureType dstAtomicFeature : dstAtomicFeatures) {
                    for (FeatureType srcAtomicFeature : srcAtomicFeatures) {
                        // POS_AF_AF_JT
                        List<FeatureType> varCrossFeatures = new ArrayList<>();
                        varCrossFeatures.add(Position.POS_C);
                        varCrossFeatures.add(srcAtomicFeature);
                        varCrossFeatures.add(dstAtomicFeature);
                        featureManager.addFeature(new Feature(JointType.POS_AF_AF_JT, varCrossFeatures));
                    }
                }
            }
            if (resv_loc1.map.containsKey(key)) {
                Set<AtomicFeature> dstAtomicFeatures = resv.map.get(key);
                Set<AtomicFeature> srcAtomicFeatures = resv_loc1.map.get(key);
                for (FeatureType dstAtomicFeature : dstAtomicFeatures) {
                    for (FeatureType srcAtomicFeature : srcAtomicFeatures) {
                        // POS_AF_AF_JT
                        List<FeatureType> varCrossFeatures = new ArrayList<>();
                        varCrossFeatures.add(Position.POS_P);
                        varCrossFeatures.add(srcAtomicFeature);
                        varCrossFeatures.add(dstAtomicFeature);
                        featureManager.addFeature(new Feature(JointType.POS_AF_AF_JT, varCrossFeatures));
                    }
                }
            }
            if (resv_loc2.map.containsKey(key)) {
                Set<AtomicFeature> dstAtomicFeatures = resv.map.get(key);
                Set<AtomicFeature> srcAtomicFeatures = resv_loc2.map.get(key);
                for (FeatureType dstAtomicFeature : dstAtomicFeatures) {
                    for (FeatureType srcAtomicFeature : srcAtomicFeatures) {
                        // POS_AF_AF_JT
                        List<FeatureType> varCrossFeatures = new ArrayList<>();
                        varCrossFeatures.add(Position.POS_N);
                        varCrossFeatures.add(srcAtomicFeature);
                        varCrossFeatures.add(dstAtomicFeature);
                        featureManager.addFeature(new Feature(JointType.POS_AF_AF_JT, varCrossFeatures));
                    }
                }
            }
        }

        System.out.println(">>>>>>");
        // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360
        for (String key : resv.map.keySet()) {
            System.out.println(key);
            Set<AtomicFeature> atomicFeatures = resv.map.get(key);
            // fixme: todo: check getValueFeature
            Set<ValueFeature> valueFeatures = getValueFeature(key, repair, expr, valueExprInfo);
            System.out.println(atomicFeatures.size() + " " + atomicFeatures);
            System.out.println(valueFeatures.size()  + " " + valueFeatures);
            for (FeatureType atomicFeature : atomicFeatures) {
                for (FeatureType valueFeature : valueFeatures) {
                    // AF_VF_JT
                    List<FeatureType> valueCrossFeature = new ArrayList<>();
                    valueCrossFeature.add(atomicFeature);
                    valueCrossFeature.add(valueFeature);
                    featureManager.addFeature(new Feature(JointType.AF_VF_JT, valueCrossFeature));
                }
            }
        }
        System.out.println("<<<<<<");
        System.out.println(featureManager);

//        // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 450
//        System.out.println("GlobalFeature");
//        for (int index = Math.max(0, pivot - 3); index < Math.min(pivot + 4, srcStmtList.size()); index++) {
//            // s in Feature Extraction Algorithm
//            CtElement focusedStmt = srcStmtList.get(index);
//            FeatureType position = index < pivot ? Position.POS_P : (index > pivot ? Position.POS_N : Position.POS_C);
//            // StmtKind should be one subset of AtomicFeature
//            for (FeatureType atomicKind : getAtomicKinds(null, focusedStmt, operation instanceof UpdateOperation)) {
//                for (FeatureType repairType : repairKinds) {
//                    // POS_AF_RF_JT
//                    List<FeatureType> globalFeatures = new ArrayList<>();
//                    globalFeatures.add(position);
//                    globalFeatures.add(atomicKind);
//                    globalFeatures.add(repairType);
//                    featureManager.addFeature(new Feature(JointType.POS_AF_RF_JT, globalFeatures));
//                }
//            }
//        }
//
//        if (pivot < dstStmtList.size()) { // handle DELETE action
//            // n in Feature Extraction Algorithm
//            CtElement patchedStmt = dstStmtList.get(pivot);
//            // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
//            System.out.println("VarCrossFeature + ValueCrossFeature");
//            for (CtElement atom : getAtoms(patchedStmt)) {
//                EnumSet<AtomicFeature> dstAtomicFeatures = getAtomicKinds(atom, patchedStmt, operation instanceof UpdateOperation);
//                for (int index = Math.max(0, pivot - 3); index < Math.min(pivot + 4, srcStmtList.size()); index++) {
//                    CtElement focusedStmt = srcStmtList.get(index);
//                    EnumSet<AtomicFeature> srcAtomicFeatures = getAtomicKinds(atom, focusedStmt, operation instanceof UpdateOperation);
//                    FeatureType position = index < pivot ? Position.POS_P : (index > pivot ? Position.POS_N : Position.POS_C);
//                    for (FeatureType dstAtomicKind : dstAtomicFeatures) {
//                        for (FeatureType srcAtomicKind : srcAtomicFeatures) {
//                            // POS_AF_AF_JT
//                            List<FeatureType> varCrossFeatures = new ArrayList<>();
//                            varCrossFeatures.add(position);
//                            varCrossFeatures.add(srcAtomicKind);
//                            varCrossFeatures.add(dstAtomicKind);
//                            featureManager.addFeature(new Feature(JointType.POS_AF_AF_JT, varCrossFeatures));
//                        }
//                    }
//                }
//                // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360
//                // this is not mentioned at prophet paper but implemented in prophet code
//                if (pivot < srcStmtList.size()) { // handle DELETE action
//                    for (AtomicFeature atomicFeature : dstAtomicFeatures) {
//                        for (FeatureType valueKind : getValueKinds(atomicFeature, srcStmtList.get(pivot), patchedStmt)) {
//                            // AF_VF_JT
//                            List<FeatureType> valueCrossFeature = new ArrayList<>();
//                            valueCrossFeature.add(atomicFeature);
//                            valueCrossFeature.add(valueKind);
//                            featureManager.addFeature(new Feature(JointType.AF_VF_JT, valueCrossFeature));
//                        }
//                    }
//                }
//            }
//        }
        return featureManager;
    }

    private List<CtElement> getImmediateFollowStmts(Repair repair) {
        List<CtElement> ret = new ArrayList<>();
        CtElement loc_stmt = repair.actions.get(0).loc_stmt;
        if (repair.actions.get(0).kind != RepairActionKind.ReplaceMutationKind) {
            ret.add(loc_stmt);
            return ret;
        }
        else {
            ret.add(loc_stmt);
            CtStatement ElseB = null;
            if (repair.actions.get(0).ast_node instanceof CtIf) {
                CtIf IFS = (CtIf) repair.actions.get(0).ast_node;
                if (IFS.getThenStatement() instanceof CtStatementList) {
                    CtStatementList CS = (CtStatementList) IFS.getThenStatement();
                    ret.addAll(CS.getStatements());
                }
                else
                    ret.add(IFS.getThenStatement());
                ElseB = IFS.getElseStatement();
                if (ElseB != null) {
                    if (ElseB instanceof CtStatementList) {
                        CtStatementList CS = (CtStatementList) IFS.getThenStatement();
                        ret.addAll(CS.getStatements());
                    }
                    else
                        ret.add(ElseB);
                }
            }
            if (ElseB==null) {
                if (loc_stmt.getParent() instanceof CtStatementList) {
                    CtStatementList CS = (CtStatementList) loc_stmt.getParent();
                    boolean found = false;
                    for (CtStatement it : CS.getStatements()) {
                        if (found) {
                            ret.add(it);
                            break;
                        }
                        if (it.equals(loc_stmt))
                            found = true;
                    }
                }
            }
            return ret;
        }
    }

    private void getLocalStmts(Repair repair, List<CtElement> ret_before, List<CtElement> ret_after) {
        final int LOOKUP_DIS = 3;
        ret_before.clear();
        ret_after.clear();
        CtElement loc_stmt = repair.actions.get(0).loc_stmt;
        // Grab all compound stmt that is around the original stmt
        if (loc_stmt.getParent() instanceof CtStatementList) {
            CtStatementList CS = (CtStatementList) loc_stmt.getParent();
            List<CtStatement> tmp = new ArrayList<>();
            int idx = 0;
            boolean found = false;
            for (CtStatement it: CS.getStatements()) {
                if (it.equals(loc_stmt)) {
                    found = true;
                    idx = tmp.size();
                }
                tmp.add(it);
            }
            assert(found);
            int s = 0;
            if (idx > LOOKUP_DIS)
                s = idx - LOOKUP_DIS;
            int e = tmp.size();
            if (idx + LOOKUP_DIS + 1 < tmp.size())
                e = idx + LOOKUP_DIS + 1;
            boolean before = true;
            for (int i = s; i < e; i++) {
                if (tmp.get(i).equals(loc_stmt)) {
                    if (before)
                        ret_before.add(tmp.get(i));
                    else
                        ret_after.add(tmp.get(i));
                }
                if (tmp.get(i).equals(loc_stmt))
                    before = false;
            }
        }
        if (repair.actions.get(0).kind != RepairActionKind.ReplaceMutationKind)
            ret_after.add(loc_stmt);
    }

    private boolean isAbstractStub(CtElement E) {
        if (E instanceof CtInvocation) {
            CtInvocation CE = (CtInvocation) E;
            CtExecutableReference FD = CE.getExecutable();
            // fixme...
            return FD.isImplicit();
        } else {
            return false;
        }
    }

    private void orMap(ValueToFeatureMapTy m1, final ValueToFeatureMapTy m2) {
        for (String k: m2.map.keySet()) {
            if(!m1.map.containsKey(k)) {
                m1.map.put(k, m2.map.get(k));
            } else {
                m1.map.get(k).addAll(m2.map.get(k));
            }
        }
    }
}
