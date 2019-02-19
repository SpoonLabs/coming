package prophet4j.feature;

import prophet4j.defined.FeatureStruct.*;
import prophet4j.defined.FeatureType.*;
import prophet4j.defined.RepairStruct.*;
import prophet4j.defined.RepairType.*;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// based on FeatureExtract.cpp todo: what is the difference between CtXXX and CtXXXReference?
public class FeatureVisitor {
//        typedef std::multimap<unsigned int, unsigned int> HelperMapTy;
//        HelperMapTy binOpHelper, uOpHelper, caOpHelper;
    ValueToFeatureMapTy res = new ValueToFeatureMapTy();
    CtElement abst_v = null;
    boolean isReplace = false;
    boolean is_replace_strconst = false;
    Map<String, CtElement> valueExprInfo;

    public FeatureVisitor(Map<String, CtElement> valueExprInfo) {
        this.valueExprInfo = valueExprInfo;
    }

    private void putValueFeature(CtElement v, AtomicFeature af) {
        if (v == null) {
            if (!res.map.containsKey("")) {
                res.map.put("", new HashSet<>());
            }
            res.map.get("").add(af);
        }
        else {
//                CtExpression e = stripParenAndCast(v);
//                std::string tmp = stmtToString(*ast, e);
            String tmp = v.toString();
            if (v instanceof CtAssignment) {
                return;
            }
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

    // todo: consider the importance, if this not exists, then we do not need 7 genXXX()s
    public void traverseRC(Repair repair, CtElement abst_v) {
        this.abst_v = abst_v;
        if (abst_v != null)
            putValueFeature(abst_v, AtomicFeature.ABST_V_AF);
        this.is_replace_strconst = (repair.kind == RepairCandidateKind.ReplaceStringKind);
        assert(repair.actions.size() > 0);
        isReplace = (repair.actions.get(0).kind == RepairActionKind.ReplaceMutationKind);
        if (repair.kind == RepairCandidateKind.TightenConditionKind ||
                repair.kind == RepairCandidateKind.LoosenConditionKind ||
                repair.kind == RepairCandidateKind.GuardKind ||
                repair.kind == RepairCandidateKind.SpecialGuardKind) {
            if (repair.actions.get(0).ast_node instanceof CtIf) {
                CtIf IFS = (CtIf) repair.actions.get(0).ast_node;
                putValueFeature(null, AtomicFeature.R_STMT_COND_AF);
                CtExpression cond = IFS.getCondition();
                traverseStmt(cond);
            }
        }
        else {
            System.out.println("travelRC~");
            System.out.println(abst_v);
            traverseStmt(repair.actions.get(0).ast_node);
        }
    }

    private void putStmtType(CtElement v, CtElement S) {
        if (S instanceof CtIf) {
            if (!isReplace)
                putValueFeature(v, AtomicFeature.STMT_COND_AF);
        }
        if (S instanceof CtAssignment) {
            putValueFeature(v, isReplace ? AtomicFeature.R_STMT_ASSIGN_AF : AtomicFeature.STMT_ASSIGN_AF);
        }
        if (S instanceof CtInvocation) {
            if (isAbstractStub(S))
                return;
            putValueFeature(v, isReplace ? AtomicFeature.R_STMT_CALL_AF : AtomicFeature.STMT_CALL_AF);
        }
        if (S instanceof CtLoop) {
            assert(!isReplace);
            putValueFeature(v, AtomicFeature.STMT_LOOP_AF);
        }
        if (S instanceof CtBreak || S instanceof CtReturn)
            putValueFeature(v, isReplace ? AtomicFeature.R_STMT_CONTROL_AF : AtomicFeature.STMT_CONTROL_AF);
//            if (llvm::isa<LabelStmt>(S)) {
//                assert(!isReplace);
//                putValueFeature(v, StmtLabelAF);
//            }
    }

    public void traverseStmt(CtElement S) {
        boolean propagate = true;
        System.out.println("V-0");
        System.out.println(S);
        System.out.println(S instanceof CtExpression);
        System.out.println(S instanceof CtStatement);
        System.out.println(S instanceof CtInvocation);
        putStmtType(null, S);
        if (S instanceof CtInvocation) {
            CtInvocation CE = (CtInvocation) S;
            System.out.println("V-1");
            if (isAbstractStub(CE)) {
                System.out.println("V-2");
                propagate = false;
                CtScanner scanner = new CtScanner() {
                    // VisitExpr
                    @Override
                    public void scan(CtElement element) {
                        super.scan(element);
                        if (element instanceof CtExpression) {
                            CtStatement TS = (CtStatement) S;
                            while(!(TS instanceof CtMethod || TS instanceof CtClass || TS instanceof CtIf || TS instanceof CtStatementList)) {
                                if (isAbstractStub(element) && abst_v!=null)
                                    putStmtType(abst_v, TS);
                                else {
                                    putStmtType((CtExpression) element, TS);
                                }
                                TS = (CtStatement) TS.getParent();
                            }
                        }
                    }

                    // VisitCallExpr
                    @Override
                    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                        super.visitCtInvocation(invocation);
                        CtElement callee = invocation.getExecutable();
                        // fixme: fix this condition
                        if (isAbstractStub(invocation) && false) {
                            if (abst_v!=null) {
                                if (isReplace)
                                    putValueFeature(abst_v, AtomicFeature.R_STMT_CALL_AF);
                                else
                                    putValueFeature(abst_v, AtomicFeature.STMT_CALL_AF);
                            }
                            else if (is_replace_strconst) {
                                // OK, we are in string const replacement part
                                putValueFeature(invocation, AtomicFeature.ABST_V_AF);
                            }
                        }
                        else {
                            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
                            putValueFeature(callee, AtomicFeature.CALLEE_AF);
                            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
                        }
                        for (Object it : invocation.getActualTypeArguments())
                            putValueFeature((CtTypeReference) it, AtomicFeature.CALL_ARGUMENT_AF);
                    }
                };
                scanner.scan(S);
            }
        }
        System.out.println("propagate: " + propagate);
        if (propagate) {
            CtScanner scanner = new CtScanner() {
                @Override
                public void scan(CtElement element) {
                    super.scan(element);
                    // VisitExpr
                    if (element instanceof CtExpression) {
                        CtElement TS = S;
                        while(!(TS instanceof CtMethod || TS instanceof CtClass || TS instanceof CtIf || TS instanceof CtStatementList)) {
                            if (isAbstractStub(element) && abst_v!=null)
                                putStmtType(abst_v, TS);
                            else {
                                putStmtType((CtExpression) element, TS);
                            }
                            TS = TS.getParent();
                        }
                    }
                }

                @Override
                public <T> void visitCtField(CtField<T> f) {
                    super.visitCtField(f);
//                    if (f instanceof CtArrayAccess)
//                        putValueFeature(f, AtomicFeature.DE_REF_AF);
                    putValueFeature(f, AtomicFeature.MEMBER_ACCESS_AF);
                }

                @Override
                public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
                    super.visitCtFieldReference(reference);
//                    if (reference instanceof CtArrayAccess)
//                        putValueFeature(reference, AtomicFeature.DE_REF_AF);
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
                        putValueFeature(LHS, AtomicFeature.DE_REF_AF);
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
    }

    public ValueToFeatureMapTy getFeatureResult() {
        // FIXME: Going to filter out some messy stuff in NULL stmtTypeFeature
        // We just want one type to dominate here
        if (res.map.containsKey("")) {
            Set<AtomicFeature> tmp = res.map.get("");
            if (tmp.contains(AtomicFeature.STMT_LOOP_AF)) {
                tmp.remove(AtomicFeature.STMT_COND_AF);
                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
                tmp.remove(AtomicFeature.STMT_CALL_AF);
                tmp.remove(AtomicFeature.STMT_CONTROL_AF);
                tmp.remove(AtomicFeature.STMT_LABEL_AF);
            }
            else if (tmp.contains(AtomicFeature.STMT_COND_AF)) {
                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
                tmp.remove(AtomicFeature.STMT_CALL_AF);
                tmp.remove(AtomicFeature.STMT_CONTROL_AF);
                tmp.remove(AtomicFeature.STMT_LABEL_AF);
            }
            else if (tmp.contains(AtomicFeature.STMT_LABEL_AF)) {
                tmp.remove(AtomicFeature.STMT_CONTROL_AF);
                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
                tmp.remove(AtomicFeature.STMT_CALL_AF);
            }
            else if (tmp.contains(AtomicFeature.STMT_CONTROL_AF)) {
                tmp.remove(AtomicFeature.STMT_CALL_AF);
                tmp.remove(AtomicFeature.STMT_ASSIGN_AF);
            }
            else if (tmp.contains(AtomicFeature.STMT_ASSIGN_AF)) {
                tmp.remove(AtomicFeature.STMT_CALL_AF);
            }
        }
        return res;
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
}

