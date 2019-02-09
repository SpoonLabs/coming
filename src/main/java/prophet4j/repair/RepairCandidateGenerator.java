package prophet4j.repair;

import java.util.*;

import prophet4j.defined.RepairType.*;
import prophet4j.defined.RepairStruct.*;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLiteralImpl;

/*
fixme:
    approximated:
        genAddIfGuard() genTightCondition() genLooseCondition()
    to implement:
        genReplaceStmt() genAddIfExit() genAddStatement()
    if some of them are hard to implement, we could leave these aside now and try to solve them later
 */
// based on RepairCandidateGenerator.cpp
public class RepairCandidateGenerator {
    final int PRIORITY_ALPHA = 5000;

    CtElement root;
    List<RepairCandidate> q = new ArrayList<>();
    Map<CtStatement, Integer> loc_map;
    Map<CtStatement, Integer> loc_map1;
    Map<CtStatementList, Integer> compound_counter = new HashMap<>();
    // This is a hacky tmp list for fix is_first + is_func_block
    List<Integer> tmp_memo = new ArrayList<>();
    boolean naive;
    boolean learning;
    double GeoP;
//     boolean in_yacc_func; // seem meaningless

    // todo: consider SourceContextManager and maybe keep it as one input parameter
    public RepairCandidateGenerator(CtElement root, Map<CtStatement, Integer> locs, boolean naive, boolean learning) {
        this.root = root;
        this.loc_map = locs;
        this.naive = naive;
        this.learning = learning;
        compound_counter.clear();
        q.clear();
        GeoP = 0.01;
        this.loc_map1 = loc_map;
    }

    private boolean isTainted(CtStatement S) {
        if (S == null) return false;
        if (loc_map.containsKey(S))
            return true;
        // todo: the second condition is added by myself, to find out why Prophet does not need this
        if (S instanceof CtStatementList && compound_counter.containsKey(S)) {
            CtStatementList CS = (CtStatementList) S;
            return compound_counter.get(CS) >= 2 || (compound_counter.get(CS) == 1 && CS.getStatements().size() == 1);
        } else {
            return false;
        }
    }

    private double getPriority(CtStatement n) {
        // temporary
        assert (loc_map1.containsKey(n));
        return -((double) loc_map1.get(n));
    }

    private double getLocScore(CtStatement n) {
        assert (loc_map1.containsKey(n));
        double score = Math.log(1 - GeoP) * loc_map1.get(n) + Math.log(GeoP);
        // fixme: consider the case of CtLabel as spoon does not support CtLabel right now
//        if (llvm::isa<LabelStmt>(n))
//            score += 4.5;
        return score;
    }

    // The set of mutation generation subroutines
    private void genTightCondition(CtIf n) {
        CtExpression ori_cond = n.getCondition();
        //assert(ori_cond.getType().isIntegerType());
        CtExpression placeholder;
        if (naive)
            placeholder = new CtLiteralImpl();
        else
            placeholder = new CtInvocationImpl();
        CtIf S = n.clone();
        RepairCandidate rc = new RepairCandidate();
        rc.actions.clear();
        rc.actions.add(new RepairAction(n, RepairActionKind.ReplaceMutationKind, S));
        if (!naive) {
            // fixme: it might only be important to patch-generation
            //  List<CtExpression> candidateVars = L.getCondCandidateVars(ori_cond.getLocEnd());
            List<CtElement> candidateVars = new ArrayList<>();
            rc.actions.add(new RepairAction(S, placeholder, candidateVars));
        }
        // FIXME: priority!
        if (learning)
            rc.score = getLocScore(n);
        else
            rc.score = 4 * PRIORITY_ALPHA;
        rc.kind = CandidateKind.TightenConditionKind;
        q.add(rc);
    }

    private void genLooseCondition(CtIf n) {
        CtExpression ori_cond = n.getCondition();
        //assert(ori_cond.getType().isIntegerType());
        CtExpression placeholder;
        if (naive)
            placeholder = new CtLiteralImpl();
        else
            placeholder = new CtInvocationImpl();
        CtIf S = n.clone();
        RepairCandidate rc = new RepairCandidate();
        rc.actions.clear();
        rc.actions.add(new RepairAction(n, RepairActionKind.ReplaceMutationKind, S));
        if (!naive) {
            // fixme: it might only be important to patch-generation
            //  List<CtExpression> candidateVars = L.getCondCandidateVars(ori_cond.getLocEnd());
            List<CtElement> candidateVars = new ArrayList<>();
            rc.actions.add(new RepairAction(S, placeholder, candidateVars));
        }
        // FIXME: priority!
        if (learning)
            rc.score = getLocScore(n);
        else
            rc.score = 4 * PRIORITY_ALPHA;
        rc.kind = CandidateKind.LoosenConditionKind;
        q.add(rc);

        if (naive) return;

        // FIXME: I think the best way is probably to decompose the condition,
        // but I am to lazy to do this.
        // This is to fix the case where they && to guard side-effect calls in
        // conditions.
        if (ori_cond instanceof CtBinaryOperator) {
            CtBinaryOperator ori_BO = (CtBinaryOperator) ori_cond;
            if (ori_BO.getKind() == BinaryOperatorKind.AND) {
                S = n.clone();
                rc = new RepairCandidate();
                rc.actions.clear();
                rc.actions.add(new RepairAction(n, RepairActionKind.ReplaceMutationKind, S));
                if (!naive) {
                    // fixme: it might only be important to patch-generation
                    //  List<CtExpression> candidateVars = L.getCondCandidateVars(ori_cond.getLocEnd());
                    List<CtElement> candidateVars = new ArrayList<>();
                    rc.actions.add(new RepairAction(S, placeholder, candidateVars));
                }
                // FIXME: priority!
                if (learning)
                    rc.score = getLocScore(n);
                else
                    rc.score = 4 * PRIORITY_ALPHA;
                rc.kind = CandidateKind.LoosenConditionKind;
                q.add(rc);
            }
        }

    }

    private void genReplaceStmt(CtStatement n, boolean is_first) {
//        if (naive) return;
//        ASTLocTy loc = new ASTLocTy(file, n);
//        LocalAnalyzer L = M.getLocalAnalyzer(loc);
//        // OK, we limit replacement to expr only statement to avoid stupid redundent
//        // changes to an compound statement/if statement
//        if (n instanceof CtExpression) {
//            AtomReplaceVisitor V(ctxt, L, n, ReplaceExt.getValue());
//            V.TraverseStmt(n);
//            Set<CtStatement> res = V.getResult();
//            for (CtStatement it : res) {
//                RepairCandidate rc = new RepairCandidate();
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.ReplaceMutationKind, it));
//                // FIXME:
//                if (learning)
//                    rc.score = getLocScore(n);
//                else
//                    rc.score = getPriority(n) + PRIORITY_ALPHA / 2.0;
//                rc.kind = CandidateKind.ReplaceKind;
//                rc.is_first = is_first;
//                rc.oldRExpr = V.getOldRExpr(it);
//                rc.newRExpr = V.getNewRExpr(it);
//                q.add(rc);
//            }
//        }
//
//        // We also attempt to replace string constant
//        if (n instanceof CtExpression) {
//            StringConstReplaceVisitor V(M, ctxt, n);
//            V.TraverseStmt(n);
//            // here set<map.entry> is actually the same as map, but map.entry is equivalent to std::pair
//            Set<Map.Entry<CtStatement, CtExpression>> res = V.getResult();
//            for (Map.Entry<CtStatement, CtExpression> it: res) {
//                RepairCandidate rc = new RepairCandidate();
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.ReplaceMutationKind, it.getKey()));
//                rc.actions.add(new RepairAction(new ASTLocTy(file, it.getKey()), it.getValue(), new ArrayList<CtExpression>(), ExprTagTy.StringConstantTag));
//                rc.is_first = is_first;
//                rc.oldRExpr = V.getOldRExpr(it.getKey());
//                rc.newRExpr =  null;
//                if (learning)
//                    rc.score = getLocScore(n);
//                else
//                    rc.score = getPriority(n) + PRIORITY_ALPHA + PRIORITY_ALPHA / 2.0;
//                rc.kind = CandidateKind.ReplaceStringKind;
//                q.add(rc);
//            }
//        }
//
//        if (n instanceof CtExpression) {
//            CallExprReplaceVisitor V2(ctxt, L, n);
//            V2.TraverseStmt(n);
//            List<CtStatement> res2 = V2.getResult();
//            for (CtStatement it : res2) {
//                RepairCandidate rc = new RepairCandidate();
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.ReplaceMutationKind, it));
//                if (learning)
//                    rc.score = getLocScore(n);
//                else
//                    rc.score = getPriority(n) + PRIORITY_ALPHA;
//                rc.kind = CandidateKind.ReplaceKind;
//                rc.is_first = is_first;
//                rc.oldRExpr = V2.getOldRExpr(it);
//                rc.newRExpr = V2.getNewRExpr(it);
//                q.add(rc);
//            }
//        }
    }

    private void genAddStatement(CtStatement n, boolean is_first, boolean is_func_block) {
//        if (naive) return;
//        ASTLocTy loc = new ASTLocTy(file, n);
//        LocalAnalyzer L = M.getLocalAnalyzer(loc);
//        Set<CtExpression> exprs = L.getGlobalCandidateExprs();
//        Map<String, RepairCandidate> tmp_map;
//        tmp_map.clear();
//        for (CtExpression it: exprs) {
//            AtomReplaceVisitor V(ctxt, L, it, false);
//            V.TraverseStmt(it);
//            Set<CtStatement> stmts = V.getResult();
//            CtExpression subExpr =  null;
//            boolean valid = L.isValidStmt(it, subExpr);
//            // If it is not valid, and it contains more than
//            // one invalid decl vars
//            if (!valid && (subExpr == null))
//                continue;
//
//            for (CtStatement it2 : stmts) {
//                boolean valid_after_replace = L.isValidStmt(it2,  null);
//                if (!valid_after_replace) continue;
//                RepairCandidate rc = new RepairCandidate();
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, it2));
//                if (learning) {
//                    rc.score = getLocScore(n);
//                }
//                else {
//                    rc.score = getPriority(n);
//                    if (is_first) {
//                        rc.score += PRIORITY_ALPHA;
//                        if (is_func_block)
//                            rc.score += PRIORITY_ALPHA / 2.0;
//                    }
//                }
//                rc.kind = CandidateKind.AddAndReplaceKind;
//                rc.is_first = is_first;
//                tmp_map.get(stmtToString(ctxt, it2)) = rc;
//            }
//            if (valid) {
//                RepairCandidate rc = new RepairCandidate();
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, it));
//                if (learning) {
//                    rc.score = getLocScore(n);
//                }
//                else {
//                    rc.score = getPriority(n);
//                    if (is_first) {
//                        rc.score += PRIORITY_ALPHA;
//                        if (is_func_block)
//                            rc.score += PRIORITY_ALPHA / 2.0;
//                    }
//                }
//                rc.kind = CandidateKind.AddAndReplaceKind;
//                rc.is_first = is_first;
//                tmp_map.get(stmtToString(ctxt, it)) = rc;
//            }
//        }
//
//        // insert if_stmt without atom replace if possible
//        Set<CtStatement> stmts = L.getGlobalCandidateIfStmts();
//        for (CtStatement it : stmts) {
//            boolean valid = L.isValidStmt(it,  null);
//            if (valid) {
//                RepairCandidate rc = new RepairCandidate();
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, it));
//                if (learning) {
//                    rc.score = getLocScore(n);
//                }
//                else {
//                    rc.score = getPriority(n);
//                    if (is_first) {
//                        rc.score += PRIORITY_ALPHA;
//                        if (is_func_block)
//                            rc.score += PRIORITY_ALPHA / 2.0;
//                    }
//                }
//                rc.kind = CandidateKind.AddAndReplaceKind;
//                rc.is_first = is_first;
//                tmp_map.get(stmtToString(ctxt, it)) = rc;
//            }
//        }
//
//        // This tmp_map is used to eliminate identical candidate generated
//        for (RepairCandidate rc: tmp_map.values()) {
//            // see TraverseFuncDecl, some hacky way to fix loc score for is_first && is_func_block
//            if (is_first && is_func_block)
//                tmp_memo.add(q.size());
//            q.add(rc);
//        }
    }

    private void genAddIfGuard(CtStatement n, boolean is_first) {
        CtExpression placeholder;
        if (naive)
            placeholder = new CtLiteralImpl();
        else
            placeholder = new CtInvocationImpl();
        CtIf new_IF = new CtIfImpl();
        RepairCandidate rc = new RepairCandidate();
        rc.actions.clear();
        rc.actions.add(new RepairAction(n, RepairActionKind.ReplaceMutationKind, new_IF));
        if (!naive) {
            // fixme: it might only be important to patch-generation
            //  List<CtExpression> candidateVars = L.getCondCandidateVars(n.getLocStart());
            List<CtElement> candidateVars = new ArrayList<>();
            rc.actions.add(new RepairAction(new_IF, placeholder, candidateVars));
        }
        // FIXME: priority!
        if (learning)
            rc.score = getLocScore(n);
        else
            rc.score = getPriority(n) + PRIORITY_ALPHA;
        rc.kind = CandidateKind.GuardKind;
        rc.is_first = is_first;
        q.add(rc);
        if (naive) return;

        // for if statement, we also try guard the execution of the condition by adding is_neg in before
        if (n instanceof CtIf) {
//            if (hasCallExpr(n)) {
            CtIf IfS = (CtIf) n;
            new_IF = IfS.clone();
            rc = new RepairCandidate();
            rc.actions.clear();
            rc.actions.add(new RepairAction(n, RepairActionKind.ReplaceMutationKind, new_IF));
            // fixme: it might only be important to patch-generation
            //  List<CtExpression> candidateVars = L.getCondCandidateVars(n.getLocStart());
            List<CtElement> candidateVars = new ArrayList<>();
            rc.actions.add(new RepairAction(new_IF, placeholder, candidateVars));
            // FIXME: priority!
            if (learning)
                rc.score = getLocScore(n);
            else
                rc.score = getPriority(n) + PRIORITY_ALPHA;
            rc.kind = CandidateKind.SpecialGuardKind;
            rc.is_first = is_first;
            q.add(rc);
//            }
        }
    }

    private void genAddIfExit(CtStatement n, boolean is_first, boolean is_func_block) {
//        ASTLocTy loc = new ASTLocTy(file, n);
//        LocalAnalyzer L = M.getLocalAnalyzer(loc);
//        CtExpression placeholder;
//        if (naive)
//            placeholder = new CtLiteralImpl();
//        else
//            placeholder = new CtInvocationImpl();
//        FunctionDecl curFD = L.getCurrentFunction();
//        RepairCandidate rc = new RepairCandidate();
//        if (curFD.getCallResultType() == ctxt.VoidTy) {
//            CtReturn RS = new(ctxt) ReturnStmt(SourceLocation(),  null, 0);
//            CtIf IFS = new(ctxt) IfStmt(ctxt, SourceLocation(), 0, placeholder, RS);
//            rc.actions.clear();
//            rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//            if (!naive) {
//                // fixme: it might only be important to patch-generation
//                //  List<CtExpression> candidateVars = L.getCondCandidateVars(n.getLocStart());
//                List<CtExpression> candidateVars = new ArrayList<>();
//                rc.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, candidateVars));
//            }
//            if (learning)
//                rc.score = getLocScore(n);
//            else {
//                rc.score = getPriority(n) + PRIORITY_ALPHA;
////                if (llvm::isa<LabelStmt>(n))
////                    rc.score += PRIORITY_ALPHA / 2.0;
//               /*if (llvm::isa<IfStmt>(n))
//                    rc.score -= PRIORITY_ALPHA / 4.0;*/
//                if (is_first) {
//                    rc.score += PRIORITY_ALPHA;
//                    if (is_func_block)
//                        rc.score += PRIORITY_ALPHA / 2.0;
//                }
//            }
//            rc.kind = CandidateKind.IfExitKind;
//            rc.is_first = is_first;
//            q.add(rc);
//        }
//        else {
//            List<CtExpression> exprs = L.getCandidateReturnExpr();
//            for (int i = 0; i < exprs.size(); i++) {
//                CtExpression placeholder2 = exprs.get(i);
//                CtReturn RS = new(ctxt) CtReturn(SourceLocation(), placeholder2, 0);
//                CtIf IFS = new(ctxt) CtIf(ctxt, SourceLocation(), 0, placeholder, RS);
//                rc.actions.clear();
//                rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//                if (!naive) {
//                    // fixme: it might only be important to patch-generation
//                    //  List<CtExpression> candidateVars = L.getCondCandidateVars(n.getLocStart());
//                    List<CtExpression> candidateVars = new ArrayList<>();
//                    rc.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, candidateVars));
//                }
//                //FIXME: candidate
//                if (learning)
//                    rc.score = getLocScore(n);
//                else {
//                    rc.score = getPriority(n) + PRIORITY_ALPHA;
////                    if (llvm::isa<LabelStmt>(n))
////                        rc.score += PRIORITY_ALPHA / 2.0;
//                    /*if (llvm::isa<IfStmt>(n))
//                        rc.score -= PRIORITY_ALPHA / 4.0;*/
//                    if (is_first) {
//                        rc.score += PRIORITY_ALPHA;
//                        if (is_func_block)
//                            rc.score += PRIORITY_ALPHA / 2.0;
//                    }
//                }
//                rc.kind = CandidateKind.IfExitKind;
//                rc.is_first = is_first;
//                q.add(rc);
//            }
//        }
//        if (naive) return;
//        if (L.isInsideLoop()) {
//            CtBreak BS = new(ctxt) CtBreak(SourceLocation());
//            CtIf IFS = new(ctxt) IfStmt(ctxt, SourceLocation(), 0, placeholder, BS);
//            rc.actions.clear();
//            rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//            // fixme: it might only be important to patch-generation
//            //  List<CtExpression> candidateVars = L.getCondCandidateVars(n.getLocStart());
//            List<CtExpression> candidateVars = new ArrayList<>();
//            rc.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, candidateVars));
//            //FIXME: score
//            if (learning)
//                rc.score = getLocScore(n);
//            else {
//                rc.score = getPriority(n) + PRIORITY_ALPHA;
////                if (llvm::isa<LabelStmt>(n))
////                    rc.score += PRIORITY_ALPHA / 2.0;
//                /*if (llvm::isa<IfStmt>(n))
//                    rc.score -= PRIORITY_ALPHA / 4.0;*/
//                if (is_first)
//                    rc.score += PRIORITY_ALPHA;
//            }
//            rc.kind = CandidateKind.IfExitKind;
//            rc.is_first = is_first;
//            q.add(rc);
//        }
//        // insert if goto, this is beyond the reach of expr synthesizer,
//        // so we need a loop
//        List<LabelDecl> labels = L.getCandidateGotoLabels();
//        for (int i = 0; i < labels.size(); i++) {
//            // We are going to ignore yy generated labels, this is hacky
//             String label_name = labels.get(i).getName();
//            if (label_name.startsWith("yy"))
//                continue;
//            GotoStmt GS = new(ctxt) GotoStmt(labels.get(i), SourceLocation(), SourceLocation());
//            CtIf IFS = new(ctxt) IfStmt(ctxt, SourceLocation(), 0, placeholder, GS);
//            rc.actions.clear();
//            rc.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//            // fixme: it might only be important to patch-generation
//            //  List<CtExpression> candidateVars = L.getCondCandidateVars(n.getLocStart());
//            List<CtExpression> candidateVars = new ArrayList<>();
//            rc.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, candidateVars));
//            if (learning) {
//                rc.score = getLocScore(n) + 1e-3;
//            }
//            else {
//                rc.score = getPriority(n) + PRIORITY_ALPHA + 200;
////                if (llvm::isa<LabelStmt>(n))
////                    rc.score += PRIORITY_ALPHA / 2.0;
//                /*if (llvm::isa<IfStmt>(n))
//                    rc.score -= PRIORITY_ALPHA / 4.0;*/
//                if (is_first)
//                    rc.score += PRIORITY_ALPHA;
//            }
//            rc.kind = CandidateKind.IfExitKind;
//            rc.is_first = is_first;
//            q.add(rc);
//        }
    }

    // only used by RepairSearchEngine
    public void setFlipP(double GeoP) {
        this.GeoP = GeoP;
    }

    public List<RepairCandidate> run() {
        CtScanner scanner = new CtScanner() {

            // https://clang.llvm.org/doxygen/classclang_1_1LabelStmt.html
            private boolean isLabelStmt(CtStatement statement) {
                return false;
            }

            // https://clang.llvm.org/doxygen/classclang_1_1DeclStmt.html
            private boolean isDeclStmt(CtStatement statement) {
                return statement instanceof CtIf || statement instanceof CtLoop || statement instanceof CtSwitch || statement instanceof CtAssignment;
            }

            @Override
            public void visitCtIf(CtIf n) {
                // genTightCondition genLooseCondition
                super.visitCtIf(n);
                CtStatement ThenCS = n.getThenStatement();
                CtStatement ElseCS = n.getElseStatement();
                if (isTainted(n) || isTainted(ThenCS) || isTainted(ElseCS)) {
                    if (!loc_map1.containsKey(n))
                        loc_map1.put(n, 10000000);
                    if (loc_map1.get(n) > loc_map1.get(ThenCS))
                        loc_map1.put(n, loc_map1.get(ThenCS));
                    if (loc_map1.get(n) > loc_map1.get(ElseCS))
                        loc_map1.put(n, loc_map1.get(ElseCS));
                }
                if (isTainted(n) || isTainted(ThenCS))
                    genTightCondition(n);
                if (isTainted(n) || isTainted(ElseCS))
                    genLooseCondition(n);
            }

            @Override
            public void visitCtStatementList(CtStatementList n) {
                super.visitCtStatementList(n);
                assert n instanceof CtStatement; // that is to say, n instanceof CtBlock
                compound_counter.put(n, 0);
                int best = 1000000;
                for (CtStatement it : n) {
                    if (isTainted(it)) {
                        compound_counter.put(n, compound_counter.get(n) + 1);
                        assert loc_map1.containsKey(it);
                        if (best > loc_map1.get(n))
                            best = loc_map1.get(n);
                    }
                }
                loc_map1.put((CtStatement) n, best);
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> m) {
                super.visitCtMethod(m);
                // seems not apply to Java as no goto in Java
//                in_yacc_func = isYaccFunc(FD);
                // This is to fix the first statement in the function block,
                // a little bit hacky though, because we use a temporary memo<`2`> list
                // todo: this seems weird
                tmp_memo.clear();
                for (int iv : tmp_memo) {
                    RepairCandidate rc = q.get(iv);
                    assert (rc.is_first);
                    assert (rc.actions.size() > 0);
                    int old_loc = loc_map1.get(rc.actions.get(0).loc_stmt);
                    loc_map1.put(rc.actions.get(0).loc_stmt, loc_map1.get(m.getBody()));
                    if (learning)
                        rc.score = getLocScore(rc.actions.get(0).loc_stmt) + 0.2;
                    else {
                        rc.score += old_loc;
                        rc.score -= loc_map1.get(rc.actions.get(0).loc_stmt);
                    }
                }
            }

            @Override
            public void scan(CtRole role, CtElement element) {
                super.scan(role, element);
                if (element instanceof CtStatement && !(element instanceof CtStatementList)) {
                    CtStatement n = (CtStatement) element;
                    boolean is_first = false;
                    if (n.getParent() instanceof CtStatementList) {
                        CtStatementList CS = (CtStatementList) n.getParent();
                        is_first = true;
                        for (CtStatement it : CS.getStatements()) {
                            if (it == n) break;
                            if (!isDeclStmt(it))
                                is_first = false;
                        }
                    }
                    is_first = is_first && !isDeclStmt(n);

                    if (isTainted(n)) {
                        // This is to compute whether Stmt n is the first
                        // non-decl statement in a CompoundStmt
                        genReplaceStmt(n, is_first);
                        // todo: exact condition for DeclStmt and LabelStmt
                        if (!isDeclStmt(n) && !isLabelStmt(n))
                            genAddIfGuard(n, is_first);
//                            genAddMemset(n, is_first); // seems not apply to Java
                        genAddStatement(n, is_first, n instanceof CtClass); // or "n instanceof CtMethod"
                        genAddIfExit(n, is_first, n instanceof CtClass); // or "n instanceof CtMethod"
                    } else if (n instanceof CtIf) {
                        CtIf IFS = (CtIf) n;
                        CtStatement thenBlock = IFS.getThenStatement();
                        CtStatement firstS = thenBlock;
                        if (thenBlock instanceof CtStatementList) {
                            CtStatementList CS = (CtStatementList) thenBlock;
                            if (CS.getStatements().size() > 1)
                                firstS = CS.getStatements().get(0);
                        }
                        if (isTainted(thenBlock) || isTainted(firstS)) {
                            if (isTainted(thenBlock))
                                loc_map1.put(n, loc_map1.get(thenBlock));
                            else
                                loc_map1.put(n, loc_map1.get(firstS));
                            genAddStatement(n, is_first, n instanceof CtClass); // or "n instanceof CtMethod"
                        }
                    }
                }
            }
        };
        // traverse (i.e. go to each node) the AST of clang::ASTContext (the top declaration context)
        scanner.scan(root);
        return q;
    }
}
