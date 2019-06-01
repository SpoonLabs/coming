package fr.inria.prophet4j.feature.original.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.inria.prophet4j.feature.original.OriginalFeature.AtomicFeature;
import fr.inria.prophet4j.utility.Structure.Repair;

import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLabelledFlowBreak;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.CtScanner;

// based on FeatureExtract.cpp
public class OriginalFeatureVisitor {
    boolean isReplace = false;
    Map<String, CtElement> valueExprInfo;
    Map<String, Set<AtomicFeature>> resMap = new HashMap<>();

    public OriginalFeatureVisitor(Map<String, CtElement> valueExprInfo) {
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
            putValueFeature(atom, AtomicFeature.ABST_V_AF);
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
                    putValueFeature(null, AtomicFeature.STMT_LOOP_AF);
                    putValueFeature(element, AtomicFeature.STMT_LOOP_AF);
                }
                if (element instanceof CtCFlowBreak || element instanceof CtExpression && element.getParent() instanceof CtCFlowBreak) {
                    if (element instanceof CtLabelledFlowBreak || element instanceof CtExpression && element.getParent() instanceof CtLabelledFlowBreak) {
//                        assert !isReplace;
                        putValueFeature(null, AtomicFeature.STMT_LABEL_AF);
                        putValueFeature(element, AtomicFeature.STMT_LABEL_AF);
                    } else {
                        putValueFeature(null, isReplace ? AtomicFeature.R_STMT_CONTROL_AF : AtomicFeature.STMT_CONTROL_AF);
                        putValueFeature(element, isReplace ? AtomicFeature.R_STMT_CONTROL_AF : AtomicFeature.STMT_CONTROL_AF);
                    }
                }
                if (element instanceof CtAssignment || element instanceof CtExpression && element.getParent() instanceof CtAssignment) {
                    putValueFeature(null, isReplace ? AtomicFeature.R_STMT_ASSIGN_AF : AtomicFeature.STMT_ASSIGN_AF);
                    putValueFeature(element, isReplace ? AtomicFeature.R_STMT_ASSIGN_AF : AtomicFeature.STMT_ASSIGN_AF);
                }
                if (element instanceof CtInvocation || element instanceof CtExpression && element.getParent() instanceof CtInvocation) {
                    putValueFeature(null, isReplace ? AtomicFeature.R_STMT_CALL_AF : AtomicFeature.STMT_CALL_AF);
                    putValueFeature(element, isReplace ? AtomicFeature.R_STMT_CALL_AF : AtomicFeature.STMT_CALL_AF);
                }
                if (element instanceof CtIf || element instanceof CtExpression && element.getParent() instanceof CtIf) {
                    putValueFeature(null, isReplace ? AtomicFeature.R_STMT_COND_AF : AtomicFeature.STMT_COND_AF);
                    putValueFeature(element, isReplace ? AtomicFeature.R_STMT_COND_AF : AtomicFeature.STMT_COND_AF);
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
                putValueFeature(LHS, AtomicFeature.ASSIGN_LHS_AF);
                putValueFeature(LHS, AtomicFeature.CHANGED_AF);
                if (RHS instanceof CtLiteral) {
                    Object v = ((CtLiteral)RHS).getValue();
                    if (v instanceof Integer) {
                        if ((Integer) v == 0) {
                            putValueFeature(LHS, AtomicFeature.ASSIGN_ZERO_AF);
                        }
                        putValueFeature(LHS, AtomicFeature.ASSIGN_CONST_AF);
                    }
                }
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
                        putValueFeature(LHS, AtomicFeature.OP_ADD_AF);
                        putValueFeature(RHS, AtomicFeature.OP_ADD_AF);
                        break;
                    case MINUS:
                        putValueFeature(LHS, AtomicFeature.OP_SUB_AF);
                        putValueFeature(RHS, AtomicFeature.OP_SUB_AF);
                        break;
                    case MUL:
                        putValueFeature(LHS, AtomicFeature.OP_MUL_AF);
                        putValueFeature(RHS, AtomicFeature.OP_MUL_AF);
                        break;
                    case DIV:
                        putValueFeature(LHS, AtomicFeature.OP_DIV_AF);
                        putValueFeature(RHS, AtomicFeature.OP_DIV_AF);
                        break;
                    case MOD:
                        putValueFeature(LHS, AtomicFeature.OP_MOD_AF);
                        putValueFeature(RHS, AtomicFeature.OP_MOD_AF);
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
                        putValueFeature(LHS, AtomicFeature.OP_ADD_AF);
                        putValueFeature(RHS, AtomicFeature.OP_ADD_AF);
                        break;
                    case MINUS:
                        putValueFeature(LHS, AtomicFeature.OP_SUB_AF);
                        putValueFeature(RHS, AtomicFeature.OP_SUB_AF);
                        break;
                    case MUL:
                        putValueFeature(LHS, AtomicFeature.OP_MUL_AF);
                        putValueFeature(RHS, AtomicFeature.OP_MUL_AF);
                        break;
                    case DIV:
                        putValueFeature(LHS, AtomicFeature.OP_DIV_AF);
                        putValueFeature(RHS, AtomicFeature.OP_DIV_AF);
                        break;
                    case MOD:
                        putValueFeature(LHS, AtomicFeature.OP_MOD_AF);
                        putValueFeature(RHS, AtomicFeature.OP_MOD_AF);
                        break;
                    case LE:
                        putValueFeature(LHS, AtomicFeature.OP_LE_AF);
                        putValueFeature(RHS, AtomicFeature.OP_LE_AF);
                        break;
                    case LT:
                        putValueFeature(LHS, AtomicFeature.OP_LT_AF);
                        putValueFeature(RHS, AtomicFeature.OP_LT_AF);
                        break;
                    case GE:
                        putValueFeature(LHS, AtomicFeature.OP_GE_AF);
                        putValueFeature(RHS, AtomicFeature.OP_GE_AF);
                        break;
                    case GT:
                        putValueFeature(LHS, AtomicFeature.OP_GT_AF);
                        putValueFeature(RHS, AtomicFeature.OP_GT_AF);
                        break;
                    case EQ:
                        putValueFeature(LHS, AtomicFeature.OP_EQ_AF);
                        putValueFeature(RHS, AtomicFeature.OP_EQ_AF);
                        break;
                    case NE:
                        putValueFeature(LHS, AtomicFeature.OP_NE_AF);
                        putValueFeature(RHS, AtomicFeature.OP_NE_AF);
                        break;
                }
            }

            @Override
            public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
                super.visitCtUnaryOperator(operator);
                CtExpression operand = operator.getOperand();
                switch (operator.getKind()) {
                    case POS:
                        putValueFeature(operand, AtomicFeature.OP_ADD_AF);
                        break;
                    case NEG:
                        putValueFeature(operand, AtomicFeature.OP_SUB_AF);
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