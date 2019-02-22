package prophet4j.feature;

import java.util.HashSet;
import java.util.Map;

import prophet4j.defined.FeatureStruct.ValueToFeatureMapTy;
import prophet4j.defined.FeatureType.AtomicFeature;
import prophet4j.defined.RepairStruct.Repair;
import prophet4j.defined.RepairType.RepairActionKind;
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

// based on FeatureExtract.cpp todo: what is the difference between CtXXX and CtXXXReference?
public class FeatureVisitor {
//        typedef std::multimap<unsigned int, unsigned int> HelperMapTy;
//        HelperMapTy binOpHelper, uOpHelper, caOpHelper;
    ValueToFeatureMapTy res = new ValueToFeatureMapTy();
    boolean isReplace = false;
    Map<String, CtElement> valueExprInfo;

    public FeatureVisitor(Map<String, CtElement> valueExprInfo) {
        this.valueExprInfo = valueExprInfo;
    }

    private void putValueFeature(CtElement v, AtomicFeature af) {
//        System.out.println("??"+v+">>"+af);
        if (v == null) {
            if (!res.map.containsKey("")) {
                res.map.put("", new HashSet<>());
            }
            res.map.get("").add(af);
        }
        else {
//            CtExpression e = stripParenAndCast(v);
//            std::string tmp = stmtToString(*ast, e);
            String tmp = v.toString();
            // i can not know why there is one return here
//            if (v instanceof CtAssignment) {
//                return;
//            }
            // todo: CtInvocation or CtExecutable ?
//            if (v.getElements(new TypeFilter<>(CtInvocation.class)).size() > 0 && !isAbstractStub(v)) {
//                return;
//            }
            if (!res.map.containsKey(tmp)) {
                res.map.put(tmp, new HashSet<>());
            }
            res.map.get(tmp).add(af);
            if (!valueExprInfo.containsKey(tmp)) {
                valueExprInfo.put(tmp, v);
            }
        }
    }

    // todo: check the difference between putValueFeature(null, xx) and putValueFeature(v, xx)
    private void putStmtType(CtElement v, CtElement S) {
//        System.out.println("><><><><><><><><");
//        System.out.println("><" + v);
//        System.out.println("><" + S);
//        System.out.println("><" + isReplace);
        if (S instanceof CtLoop) {
//            assert !isReplace;
            putValueFeature(null, AtomicFeature.STMT_LOOP_AF);
            putValueFeature(v, AtomicFeature.STMT_LOOP_AF);
        }
        if (S instanceof CtAssignment) {
            putValueFeature(null, isReplace ? AtomicFeature.R_STMT_ASSIGN_AF : AtomicFeature.STMT_ASSIGN_AF);
            putValueFeature(v, isReplace ? AtomicFeature.R_STMT_ASSIGN_AF : AtomicFeature.STMT_ASSIGN_AF);
        }
        if (S instanceof CtInvocation) {
            putValueFeature(null, isReplace ? AtomicFeature.R_STMT_CALL_AF : AtomicFeature.STMT_CALL_AF);
            putValueFeature(v, isReplace ? AtomicFeature.R_STMT_CALL_AF : AtomicFeature.STMT_CALL_AF);
        }
        if (S instanceof CtIf) {
            putValueFeature(null, isReplace ? AtomicFeature.R_STMT_COND_AF : AtomicFeature.STMT_COND_AF);
            putValueFeature(v, isReplace ? AtomicFeature.R_STMT_COND_AF : AtomicFeature.STMT_COND_AF);
        }
        if (S instanceof CtCFlowBreak) {
            if (S instanceof CtLabelledFlowBreak) {
//                assert !isReplace;
                putValueFeature(null, AtomicFeature.STMT_LABEL_AF);
                putValueFeature(v, AtomicFeature.STMT_LABEL_AF);
            } else {
                putValueFeature(null, isReplace ? AtomicFeature.R_STMT_CONTROL_AF : AtomicFeature.STMT_CONTROL_AF);
                putValueFeature(v, isReplace ? AtomicFeature.R_STMT_CONTROL_AF : AtomicFeature.STMT_CONTROL_AF);
            }
        }
    }

    public void traverseRepair(Repair repair, CtElement atom) { // traverseRC
        if (atom != null) {
            // for the return value of getCandidateAtoms() when null is not its only element
            putValueFeature(atom, AtomicFeature.ABST_V_AF);
        }
        assert(repair.actions.size() > 0);
        isReplace = (repair.actions.get(0).kind == RepairActionKind.ReplaceMutationKind);
//        System.out.println("repair.actions.get(0)");
//        System.out.println(repair.actions.get(0).srcElem);
//        System.out.println(repair.actions.get(0).dstElem);
//        System.out.println(repair.actions.get(0).kind);
//        System.out.println(isReplace);
        // no meanings todo: check
//        if (repair.kind == RepairCandidateKind.TightenConditionKind ||
//                repair.kind == RepairCandidateKind.LoosenConditionKind ||
//                repair.kind == RepairCandidateKind.GuardKind ||
//                repair.kind == RepairCandidateKind.SpecialGuardKind) {
//            if (repair.actions.get(0).dstElem instanceof CtIf) {
//                CtIf IFS = (CtIf) repair.actions.get(0).dstElem;
//                putValueFeature(null, AtomicFeature.R_STMT_COND_AF);
//                CtExpression cond = IFS.getCondition();
//                traverseStmt(cond);
//            }
//        }
//        else {
//            System.out.println("travelRC~");
            traverseStmt(repair.actions.get(0).dstElem);
//        }
    }

    public void traverseStmt(CtElement S) {
//        System.out.println("V-0");
//        System.out.println(S);
//        System.out.println(S instanceof CtExpression);
//        System.out.println(S instanceof CtStatement);
//        System.out.println(S instanceof CtInvocation);
//        putStmtType(null, S);
        CtScanner scanner = new CtScanner() {
            @Override
            public void scan(CtElement element) { // VisitExpr
                super.scan(element);
//                System.out.println("!!!!!!" + element);
//                System.out.println(element instanceof CtCFlowBreak);
                if (element instanceof CtLoop || element instanceof CtAssignment || element instanceof CtInvocation || element instanceof CtIf || element instanceof CtCFlowBreak) {
                    putStmtType(element, element);
//                    for (CtElement e: element.getElements(new TypeFilter<>(CtElement.class))) {
//                        putStmtType(e, element);
//                    }
                }
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) { // VisitCallExpr
                super.visitCtInvocation(invocation);
                CtElement callee = invocation.getExecutable();
//                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
//                System.out.println(callee);
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
        };
        scanner.scan(S);
    }

    // i really do not know why we need to remove some atomic features
    public ValueToFeatureMapTy getFeatureResult() {
        // FIXME: Going to filter out some messy stuff in NULL stmtTypeFeature
        // We just want one type to dominate here
//        System.out.println(">>>>>>>>");
//        System.out.println(res.map);
//        System.out.println("<<<<<<<<");
//        if (res.map.containsKey("")) {
//            Set<AtomicFeature> tmp = res.map.get("");
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
        return res;
    }
}

