package fr.inria.prophet4j.feature.enhanced.util;

import fr.inria.prophet4j.utility.Structure.Repair;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeature.AtomicFeature;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// based on FeatureExtract.cpp
public class EnhancedFeatureVisitor {
    boolean isReplace = false;
    Map<String, CtElement> valueExprInfo;
    Map<String, Set<AtomicFeature>> resMap = new HashMap<>();

    public EnhancedFeatureVisitor(Map<String, CtElement> valueExprInfo) {
        this.valueExprInfo = valueExprInfo;
    }

    private void putValueFeature(CtElement v, AtomicFeature af) {
        if (v == null) {
            if (!resMap.containsKey("@")) {
                resMap.put("@", new HashSet<>());
            }
            resMap.get("@").add(af);
        }
        else {
//            CtExpression e = stripParenAndCast(v);
//            std::string tmp = stmtToString(*ast, e);
            String tmp = v.toString();
            // i can not know why there is one return here
//            if (v instanceof CtAssignment) {
//                return;
//            }
            // CtInvocation or CtExecutable todo check
//            if (v.getElements(new TypeFilter<>(CtInvocation.class)).size() > 0 && !isAbstractStub(v)) {
//                return;
//            }
            if (!resMap.containsKey(tmp)) {
                resMap.put(tmp, new HashSet<>());
            }
            resMap.get(tmp).add(af);
            if (!valueExprInfo.containsKey(tmp)) {
                valueExprInfo.put(tmp, v);
            }
        }
    }

    public Map<String, Set<AtomicFeature>> traverseRepair(Repair repair, CtElement atom) { // traverseRC
        if (atom != null) {
            // for the return value of getCandidateAtoms() when null is not its only element
            putValueFeature(atom, AtomicFeature.ABSTRACT_V_AF);
        }
        isReplace = repair.isReplace;
        // meaningless todo check
//        if (repair.kind == RepairKind.TightenConditionKind ||
//                repair.kind == RepairKind.LoosenConditionKind ||
//                repair.kind == RepairKind.GuardKind ||
//                repair.kind == RepairKind.SpecialGuardKind) {
//            if (repair.actions.get(0).dstElem instanceof CtIf) {
//                CtIf IFS = (CtIf) repair.actions.get(0).dstElem;
//                putValueFeature(null, AtomicFeature.R_STMT_COND_AF);
//                CtExpression cond = IFS.getCondition();
//                traverseStmt(cond);
//            }
//        }
//        else {
        return traverseStmt(repair.dstElem);
//        }
    }

    public Map<String, Set<AtomicFeature>> traverseStmt(CtElement S) {
        CtScanner scanner = new CtScanner() {
            @Override
            public void scan(CtElement element) { // VisitExpr
                super.scan(element);
                if (element instanceof CtLoop || element instanceof CtExpression && element.getParent() instanceof CtLoop) {
//                    assert !isReplace;
                    putValueFeature(null, AtomicFeature.STMT_LOOP_SF);
                    putValueFeature(element, AtomicFeature.STMT_LOOP_SF);
                }
                if (element instanceof CtCFlowBreak || element instanceof CtExpression && element.getParent() instanceof CtCFlowBreak) {
                    if (element instanceof CtLabelledFlowBreak || element instanceof CtExpression && element.getParent() instanceof CtLabelledFlowBreak) {
//                        assert !isReplace;
                        putValueFeature(null, AtomicFeature.STMT_LABEL_SF);
                        putValueFeature(element, AtomicFeature.STMT_LABEL_SF);
                    } else {
                        putValueFeature(null, isReplace ? AtomicFeature.R_STMT_CONTROL_SF : AtomicFeature.STMT_CONTROL_SF);
                        putValueFeature(element, isReplace ? AtomicFeature.R_STMT_CONTROL_SF : AtomicFeature.STMT_CONTROL_SF);
                    }
                }
                if (element instanceof CtAssignment || element instanceof CtExpression && element.getParent() instanceof CtAssignment) {
                    putValueFeature(null, isReplace ? AtomicFeature.R_STMT_ASSIGN_SF : AtomicFeature.STMT_ASSIGN_SF);
                    putValueFeature(element, isReplace ? AtomicFeature.R_STMT_ASSIGN_SF : AtomicFeature.STMT_ASSIGN_SF);
                }
                if (element instanceof CtInvocation || element instanceof CtExpression && element.getParent() instanceof CtInvocation) {
                    putValueFeature(null, isReplace ? AtomicFeature.R_STMT_CALL_SF : AtomicFeature.STMT_CALL_SF);
                    putValueFeature(element, isReplace ? AtomicFeature.R_STMT_CALL_SF : AtomicFeature.STMT_CALL_SF);
                }
                if (element instanceof CtIf || element instanceof CtExpression && element.getParent() instanceof CtIf) {
                    putValueFeature(null, isReplace ? AtomicFeature.R_STMT_COND_SF : AtomicFeature.STMT_COND_SF);
                    putValueFeature(element, isReplace ? AtomicFeature.R_STMT_COND_SF : AtomicFeature.STMT_COND_SF);
                }
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) { // VisitCallExpr
                super.visitCtInvocation(invocation);
                CtElement callee = invocation.getExecutable();
                putValueFeature(callee, AtomicFeature.CALLEE_AF);
                for (CtExpression it : invocation.getArguments()) {
                    putValueFeature(it, AtomicFeature.CALL_ARGUMENT_AF);
                }
            }

            @Override
            public <T> void visitCtField(CtField<T> f) {
                super.visitCtField(f);
//                if (f instanceof CtArrayAccess)
//                    putValueFeature(f, AtomicFeature.DEREF_AF);
                putValueFeature(f, AtomicFeature.MEMBER_ACCESS_AF);
            }

            @Override
            public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
                super.visitCtFieldReference(reference);
//                if (reference instanceof CtArrayAccess)
//                    putValueFeature(reference, AtomicFeature.DEREF_AF);
                putValueFeature(reference, AtomicFeature.MEMBER_ACCESS_AF);
            }

            @Override
            public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignment) {
                super.visitCtAssignment(assignment);
                CtExpression LHS = assignment.getAssigned();
                CtExpression RHS = assignment.getAssignment();
                putValueFeature(LHS, AtomicFeature.AOP_ASSIGN_AF);
                putValueFeature(RHS, AtomicFeature.AOP_ASSIGN_AF);
                putValueFeature(LHS, AtomicFeature.CHANGED_AF);
                if (RHS instanceof CtArrayAccess) {
                    putValueFeature(LHS, AtomicFeature.DEREF_AF);
                    putValueFeature(RHS, AtomicFeature.INDEX_AF);
                }
            }

            @Override
            public <T, A extends T> void visitCtOperatorAssignment(CtOperatorAssignment<T, A> assignment) {
                super.visitCtOperatorAssignment(assignment);
                CtExpression LHS = assignment.getAssigned();
                CtExpression RHS = assignment.getAssignment();
                switch (assignment.getKind()) {
                    case PLUS:
                        putValueFeature(LHS, AtomicFeature.AOP_PLUS_AF);
                        putValueFeature(RHS, AtomicFeature.AOP_PLUS_AF);
                        break;
                    case MINUS:
                        putValueFeature(LHS, AtomicFeature.AOP_MINUS_AF);
                        putValueFeature(RHS, AtomicFeature.AOP_MINUS_AF);
                        break;
                    case MUL:
                        putValueFeature(LHS, AtomicFeature.AOP_MUL_AF);
                        putValueFeature(RHS, AtomicFeature.AOP_MUL_AF);
                        break;
                    case DIV:
                        putValueFeature(LHS, AtomicFeature.AOP_DIV_AF);
                        putValueFeature(RHS, AtomicFeature.AOP_DIV_AF);
                        break;
                    case MOD:
                        putValueFeature(LHS, AtomicFeature.AOP_MOD_AF);
                        putValueFeature(RHS, AtomicFeature.AOP_MOD_AF);
                        break;
                }
                putValueFeature(LHS, AtomicFeature.CHANGED_AF);
            }

            @Override
            public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
                super.visitCtBinaryOperator(operator);
                CtExpression LHS = operator.getLeftHandOperand();
                CtExpression RHS = operator.getRightHandOperand();
                switch (operator.getKind()) {
                    case PLUS:
                        putValueFeature(LHS, AtomicFeature.BOP_PLUS_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_PLUS_AF);
                        break;
                    case MINUS:
                        putValueFeature(LHS, AtomicFeature.BOP_MINUS_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_MINUS_AF);
                        break;
                    case MUL:
                        putValueFeature(LHS, AtomicFeature.BOP_MUL_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_MUL_AF);
                        break;
                    case DIV:
                        putValueFeature(LHS, AtomicFeature.BOP_DIV_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_DIV_AF);
                        break;
                    case MOD:
                        putValueFeature(LHS, AtomicFeature.BOP_MOD_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_MOD_AF);
                        break;
                    case LE:
                        putValueFeature(LHS, AtomicFeature.BOP_LE_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_LE_AF);
                        break;
                    case LT:
                        putValueFeature(LHS, AtomicFeature.BOP_LT_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_LT_AF);
                        break;
                    case GE:
                        putValueFeature(LHS, AtomicFeature.BOP_GE_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_GE_AF);
                        break;
                    case GT:
                        putValueFeature(LHS, AtomicFeature.BOP_GT_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_GT_AF);
                        break;
                    case EQ:
                        putValueFeature(LHS, AtomicFeature.BOP_EQ_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_EQ_AF);
                        break;
                    case NE:
                        putValueFeature(LHS, AtomicFeature.BOP_NE_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_NE_AF);
                        break;
                    case AND:
                        putValueFeature(LHS, AtomicFeature.BOP_AND_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_AND_AF);
                        break;
                    case OR:
                        putValueFeature(LHS, AtomicFeature.BOP_OR_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_OR_AF);
                        break;
                    case INSTANCEOF:
                        putValueFeature(LHS, AtomicFeature.BOP_INSTANCEOF_AF);
                        putValueFeature(RHS, AtomicFeature.BOP_INSTANCEOF_AF);
                        break;
                }
            }

            @Override
            public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
                super.visitCtUnaryOperator(operator);
                CtExpression operand = operator.getOperand();
                switch (operator.getKind()) {
                    case NOT:
                        putValueFeature(operand, AtomicFeature.UOP_NOT_AF);
                        break;
                    case POS:
                        putValueFeature(operand, AtomicFeature.UOP_POS_AF);
                        break;
                    case NEG:
                        putValueFeature(operand, AtomicFeature.UOP_NEG_AF);
                        break;
                    case PREINC:
                    case POSTINC:
                        putValueFeature(operand, AtomicFeature.UOP_INC_AF);
                        putValueFeature(operand, AtomicFeature.CHANGED_AF);
                        break;
                    case PREDEC:
                    case POSTDEC:
                        putValueFeature(operand, AtomicFeature.UOP_DEC_AF);
                        putValueFeature(operand, AtomicFeature.CHANGED_AF);
                        break;
                }
            }

            @Override
            public <T> void visitCtSuperAccess(CtSuperAccess<T> f) {
                super.visitCtSuperAccess(f);
                putValueFeature(f, AtomicFeature.REF_SUPER_AF);
            }

            @Override
            public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
                super.visitCtThisAccess(thisAccess);
                putValueFeature(thisAccess, AtomicFeature.REF_THIS_AF);
            }
        };
        scanner.scan(S);
        return getFeatureResult();
    }

    // i really do not know why we need to remove some atomic features
    private Map<String, Set<AtomicFeature>> getFeatureResult() {
        // meaningless todo check
//        if (res.map.containsKey("@")) {
//            Set<AtomicFeature> tmp = res.map.get("@");
//            if (tmp.contains(AtomicFeature.STMT_LOOP_AF)) {
//                tmp.remove(AtomicFeature.STMT_COND_AF);
//                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
//                tmp.remove(AtomicFeature.STMT_CALL_AF);
//                tmp.remove(AtomicFeature.STMT_CONTROL_AF);
//                tmp.remove(AtomicFeature.STMT_LABEL_AF);
//            }
//            else if (tmp.contains(AtomicFeature.STMT_COND_AF)) {
//                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
//                tmp.remove(AtomicFeature.STMT_CALL_AF);
//                tmp.remove(AtomicFeature.STMT_CONTROL_AF);
//                tmp.remove(AtomicFeature.STMT_LABEL_AF);
//            }
//            else if (tmp.contains(AtomicFeature.STMT_LABEL_AF)) {
//                tmp.remove(AtomicFeature.STMT_CONTROL_AF);
//                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
//                tmp.remove(AtomicFeature.STMT_CALL_AF);
//            }
//            else if (tmp.contains(AtomicFeature.STMT_CONTROL_AF)) {
//                tmp.remove(AtomicFeature.STMT_CALL_AF);
//                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
//            }
//            else if (tmp.contains(AtomicFeature.STMT_ASSIGN_AF)) {
//                tmp.remove(AtomicFeature.STMT_CALL_AF);
//            }
//        }
        return resMap;
    }
}