package prophet4j.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prophet4j.defined.RepairStruct.DiffEntry;
import prophet4j.defined.RepairStruct.Repair;
import prophet4j.defined.RepairStruct.RepairAction;
import prophet4j.defined.RepairType.RepairActionKind;
import prophet4j.defined.RepairType.RepairCandidateKind;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLiteralImpl;

/*
fixme:
    to correct:
        genAddIfGuard() genTightCondition() genLooseCondition()
    to implement:
        genReplaceStmt() genAddIfExit() genAddStatement()
 */
// based on RepairGenerator.cpp
public class RepairGenerator {
    private DiffEntry diffEntry;
    private Set<CtElement> area; // loc_map
    private List<Repair> repairs = new ArrayList<>();
    private Map<CtStatementList, Integer> compound_counter = new HashMap<>();
    // todo: consider naive and in_yacc_func
    private boolean naive;
//     boolean in_yacc_func;

    public RepairGenerator(DiffEntry diffEntry, boolean naive) {
        this.diffEntry = diffEntry;
        this.area = fuzzyLocator(diffEntry.srcCommonAncestor);
        this.naive = naive;
        repairs.clear();
        compound_counter.clear();
    }

    // todo: check this function
    private boolean isTainted(CtStatement S) {
        if (S == null) return false;
        if (area.contains(S))
            return true;
        // todo: the second condition is added by myself, to find out why Prophet does not need this
        if (S instanceof CtStatementList && compound_counter.containsKey(S)) {
            CtStatementList CS = (CtStatementList) S;
            return compound_counter.get(CS) >= 2 || (compound_counter.get(CS) == 1 && CS.getStatements().size() == 1);
        } else {
            return false;
        }
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

        Repair repair = new Repair();
        repair.actions.clear();
        repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, S));
        if (!naive) {
            // fixme: check and complete this to make demo run
            //  List<CtExpression> atoms = L.getCondCandidateVars(ori_cond.getLocEnd());
            List<CtElement> atoms = new ArrayList<>();
            repair.actions.add(new RepairAction(S, placeholder, atoms));
        }
        repair.kind = RepairCandidateKind.TightenConditionKind;
        repairs.add(repair);
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
        Repair repair = new Repair();
        repair.actions.clear();
        repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, S));
        if (!naive) {
            // fixme: check and complete this to make demo run
            //  List<CtExpression> atoms = L.getCondCandidateVars(ori_cond.getLocEnd());
            List<CtElement> atoms = new ArrayList<>();
            repair.actions.add(new RepairAction(S, placeholder, atoms));
        }
        repair.kind = RepairCandidateKind.LoosenConditionKind;
        repairs.add(repair);

        if (naive) return;

        // FIXME: I think the best way is probably to decompose the condition,
        // but I am to lazy to do this.
        // This is to fix the case where they && to guard side-effect calls in
        // conditions.
        if (ori_cond instanceof CtBinaryOperator) {
            CtBinaryOperator ori_BO = (CtBinaryOperator) ori_cond;
            if (ori_BO.getKind() == BinaryOperatorKind.AND) {
                S = n.clone();
                repair = new Repair();
                repair.actions.clear();
                repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, S));
                if (!naive) {
                    // fixme: check and complete this to make demo run
                    //  List<CtExpression> atoms = L.getCondCandidateVars(ori_cond.getLocEnd());
                    List<CtElement> atoms = new ArrayList<>();
                    repair.actions.add(new RepairAction(S, placeholder, atoms));
                }
                repair.kind = RepairCandidateKind.LoosenConditionKind;
                repairs.add(repair);
            }
        }
    }

    private void genReplaceStmt(CtStatement n) {
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
//                Repair repair = new Repair();
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.ReplaceMutationKind, it));
//                repair.kind = RepairCandidateKind.ReplaceKind;
//                repair.oldRExpr = V.getOldRExpr(it);
//                repair.newRExpr = V.getNewRExpr(it);
//                repairs.add(rc);
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
//                Repair repair = new Repair();
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.ReplaceMutationKind, it.getKey()));
//                repair.actions.add(new RepairAction(new ASTLocTy(file, it.getKey()), it.getValue(), new ArrayList<CtExpression>(), ExprTagTy.StringConstantTag));
//                repair.oldRExpr = V.getOldRExpr(it.getKey());
//                repair.newRExpr =  null;
//                repair.kind = RepairCandidateKind.ReplaceStringKind;
//                repairs.add(rc);
//            }
//        }
//
//        if (n instanceof CtExpression) {
//            CallExprReplaceVisitor V2(ctxt, L, n);
//            V2.TraverseStmt(n);
//            List<CtStatement> res2 = V2.getResult();
//            for (CtStatement it : res2) {
//                Repair repair = new Repair();
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.ReplaceMutationKind, it));
//                repair.kind = RepairCandidateKind.ReplaceKind;
//                repair.oldRExpr = V2.getOldRExpr(it);
//                repair.newRExpr = V2.getNewRExpr(it);
//                repairs.add(rc);
//            }
//        }
    }

    private void genAddStatement(CtStatement n) {
//        if (naive) return;
//        ASTLocTy loc = new ASTLocTy(file, n);
//        LocalAnalyzer L = M.getLocalAnalyzer(loc);
//        Set<CtExpression> exprs = L.getGlobalCandidateExprs();
//        Map<String, Repair> tmp_map;
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
//                Repair repair = new Repair();
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, it2));
//                repair.kind = RepairCandidateKind.AddAndReplaceKind;
//                tmp_map.get(stmtToString(ctxt, it2)) = rc;
//            }
//            if (valid) {
//                Repair repair = new Repair();
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, it));
//                repair.kind = RepairCandidateKind.AddAndReplaceKind;
//                tmp_map.get(stmtToString(ctxt, it)) = rc;
//            }
//        }
//
//        // insert if_stmt without atom replace if possible
//        Set<CtStatement> stmts = L.getGlobalCandidateIfStmts();
//        for (CtStatement it : stmts) {
//            boolean valid = L.isValidStmt(it,  null);
//            if (valid) {
//                Repair repair = new Repair();
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, it));
//                repair.kind = RepairCandidateKind.AddAndReplaceKind;
//                tmp_map.get(stmtToString(ctxt, it)) = rc;
//            }
//        }
    }

    private void genAddIfGuard(CtStatement n) {
        CtExpression placeholder;
        if (naive)
            placeholder = new CtLiteralImpl();
        else
            placeholder = new CtInvocationImpl();
        CtIf new_IF = new CtIfImpl();
        Repair repair = new Repair();
        repair.actions.clear();
        repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, new_IF));
        if (!naive) {
            // fixme: check and complete this to make demo run
            //  List<CtExpression> atoms = L.getCondCandidateVars(n.getLocStart());
            List<CtElement> atoms = new ArrayList<>();
            repair.actions.add(new RepairAction(new_IF, placeholder, atoms));
        }
        repair.kind = RepairCandidateKind.GuardKind;
        repairs.add(repair);
        if (naive) return;

        // for if statement, we also try guard the execution of the condition by adding is_neg in before
        if (n instanceof CtIf) {
//            if (hasCallExpr(n)) {
            CtIf IfS = (CtIf) n;
            new_IF = IfS.clone();
            repair = new Repair();
            repair.actions.clear();
            repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, new_IF));
            // fixme: check and complete this to make demo run
            //  List<CtExpression> atoms = L.getCondCandidateVars(n.getLocStart());
            List<CtElement> atoms = new ArrayList<>();
            repair.actions.add(new RepairAction(new_IF, placeholder, atoms));
            repair.kind = RepairCandidateKind.SpecialGuardKind;
            repairs.add(repair);
//            }
        }
    }

    private void genAddIfExit(CtStatement n) {
//        ASTLocTy loc = new ASTLocTy(file, n);
//        LocalAnalyzer L = M.getLocalAnalyzer(loc);
//        CtExpression placeholder;
//        if (naive)
//            placeholder = new CtLiteralImpl();
//        else
//            placeholder = new CtInvocationImpl();
//        FunctionDecl curFD = L.getCurrentFunction();
//        Repair repair = new Repair();
//        if (curFD.getCallResultType() == ctxt.VoidTy) {
//            CtReturn RS = new(ctxt) ReturnStmt(SourceLocation(),  null, 0);
//            CtIf IFS = new(ctxt) IfStmt(ctxt, SourceLocation(), 0, placeholder, RS);
//            repair.actions.clear();
//            repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//            if (!naive) {
//                // fixme: check and complete this to make demo run
//                //  List<CtExpression> atoms = L.getCondCandidateVars(n.getLocStart());
//                List<CtExpression> atoms = new ArrayList<>();
//                repair.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, atoms));
//            }
//            repair.kind = RepairCandidateKind.IfExitKind;
//            repairs.add(rc);
//        }
//        else {
//            List<CtExpression> exprs = L.getCandidateReturnExpr();
//            for (int i = 0; i < exprs.size(); i++) {
//                CtExpression placeholder2 = exprs.get(i);
//                CtReturn RS = new(ctxt) CtReturn(SourceLocation(), placeholder2, 0);
//                CtIf IFS = new(ctxt) CtIf(ctxt, SourceLocation(), 0, placeholder, RS);
//                repair.actions.clear();
//                repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//                if (!naive) {
//                    // fixme: check and complete this to make demo run
//                    //  List<CtExpression> atoms = L.getCondCandidateVars(n.getLocStart());
//                    List<CtExpression> atoms = new ArrayList<>();
//                    repair.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, atoms));
//                }
//                repair.kind = RepairCandidateKind.IfExitKind;
//                repairs.add(rc);
//            }
//        }
//        if (naive) return;
//        if (L.isInsideLoop()) {
//            CtBreak BS = new(ctxt) CtBreak(SourceLocation());
//            CtIf IFS = new(ctxt) IfStmt(ctxt, SourceLocation(), 0, placeholder, BS);
//            repair.actions.clear();
//            repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//            // fixme: check and complete this to make demo run
//            //  List<CtExpression> atoms = L.getCondCandidateVars(n.getLocStart());
//            List<CtExpression> atoms = new ArrayList<>();
//            repair.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, atoms));
//            repair.kind = RepairCandidateKind.IfExitKind;
//            repairs.add(rc);
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
//            repair.actions.clear();
//            repair.actions.add(new RepairAction(loc, RepairActionKind.InsertMutationKind, IFS));
//            // fixme: check and complete this to make demo run
//            //  List<CtExpression> atoms = L.getCondCandidateVars(n.getLocStart());
//            List<CtExpression> atoms = new ArrayList<>();
//            repair.actions.add(new RepairAction(new ASTLocTy(file, IFS), placeholder, atoms));
//            repair.kind = RepairCandidateKind.IfExitKind;
//            repairs.add(rc);
//        }
    }

    public Repair genHumanRepair(DiffEntry diffEntry) {
        Repair repair = new Repair();

        repair.kind = null; // related to RepairFeature
        repair.oldRExpr = null; // related to ValueFeature
        repair.newRExpr = null; // related to ValueFeature

        // todo: more checks
        // based on matchCandidateWithHumanFix(), repair.kind should have one RepairFeature
//        System.out.println("------------");
//        System.out.println(diffEntry.type);
        CtElement srcCommonAncestor = diffEntry.srcCommonAncestor;
        if (srcCommonAncestor instanceof CtStatementList) {
            srcCommonAncestor = diffEntry.srcCommonAncestor.getParent();
        }
        CtElement dstCommonAncestor = diffEntry.dstCommonAncestor;
        if (dstCommonAncestor instanceof CtStatementList) {
            dstCommonAncestor = diffEntry.dstCommonAncestor.getParent();
        }
        switch (diffEntry.type) {
            case DeleteAction: // kind
                // GuardKind: // INSERT_GUARD_RF
                repair.kind = RepairCandidateKind.GuardKind;
                break;
            case InsertAction: // kind
                // IfExitKind: // INSERT_CONTROL_RF
                // AddAndReplaceKind: // INSERT_STMT_RF
                if (dstCommonAncestor instanceof CtIf) {
                    repair.kind = RepairCandidateKind.IfExitKind;
                } else {
                    repair.kind = RepairCandidateKind.AddAndReplaceKind;
                }
                // compare with others in genRepairCandidates()
                repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, diffEntry.srcCommonAncestor, diffEntry.dstCommonAncestor));
                break;
            case ReplaceAction: // kind // oldRExpr // newRExpr
                // IfExitKind: // INSERT_CONTROL_RF
                // GuardKind: // INSERT_GUARD_RF
                // SpecialGuardKind: // INSERT_GUARD_RF
                // LoosenConditionKind: // REPLACE_COND_RF
                // TightenConditionKind: // REPLACE_COND_RF
                // ReplaceKind: // REPLACE_STMT_RF
                // ReplaceStringKind: // REPLACE_STMT_RF
                if (dstCommonAncestor instanceof CtIf) {
                    CtIf IF2 = (CtIf) dstCommonAncestor;
                    if (srcCommonAncestor instanceof CtIf) {
                        // make sure repair.kind would be assigned one value
                        repair.kind = RepairCandidateKind.SpecialGuardKind;
                        CtIf IF1 = (CtIf) srcCommonAncestor;
                        if (IF1.getThenStatement().equals(IF2.getThenStatement())) {
                            // LoosenConditionKind and TightenConditionKind are almost same as both are REPLACE_COND_RF
                            if (IF1.getElseStatement()!=null && IF2.getElseStatement()!=null) {
                                if (IF1.getElseStatement().equals(IF2.getElseStatement())) {
                                    repair.kind = RepairCandidateKind.LoosenConditionKind;
                                }
                            } else {
                                repair.kind = RepairCandidateKind.LoosenConditionKind;
                            }
                        }
                    } else {
                        CtStatement S = IF2.getThenStatement();
                        if (S instanceof CtCFlowBreak) {
                            repair.kind = RepairCandidateKind.IfExitKind;
                        } else {
                            repair.kind = RepairCandidateKind.GuardKind;
                        }
                    }
                } else {
                    // in both cases, oldRExpr is not null
                    repair.oldRExpr = diffEntry.srcCommonAncestor;
                    if (diffEntry.srcCommonAncestor instanceof CtLiteral) {
                        repair.kind = RepairCandidateKind.ReplaceStringKind;
                    } else {
                        // in this case, newRExpr is not null either
                        repair.newRExpr = diffEntry.dstCommonAncestor;
                        repair.kind = RepairCandidateKind.ReplaceKind;
                    }
                }
                // compare with others in genRepairCandidates()
                repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, diffEntry.srcCommonAncestor, diffEntry.dstCommonAncestor));
                break;
            case UnknownAction:
                break;
        }

        List<CtElement> candidates = diffEntry.dstCommonAncestor.getElements(new TypeFilter<>(CtElement.class));
        repair.actions.add(new RepairAction(diffEntry.srcCommonAncestor, diffEntry.dstCommonAncestor, candidates));

//        System.out.println("::::::::::::::");
//        System.out.println(repair.kind);
        return repair;
    }

    public List<Repair> genRepairCandidates() {
        CtScanner scanner = new CtScanner() {

            // https://clang.llvm.org/doxygen/classclang_1_1LabelStmt.html
            private boolean isLabelStmt(CtStatement statement) {
                return false;
            }

            // todo: check all "Decl" in Prophet
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
                if (isTainted(n) || isTainted(ThenCS))
                    genTightCondition(n);
                if (isTainted(n) || isTainted(ElseCS))
                    genLooseCondition(n);
            }

            @Override
            public void visitCtStatementList(CtStatementList n) {
                super.visitCtStatementList(n);
                compound_counter.put(n, 0);
                for (CtStatement it : n) {
                    if (isTainted(it)) {
                        compound_counter.put(n, compound_counter.get(n) + 1);
                    }
                }
            }

            @Override
            public void scan(CtRole role, CtElement element) {
                super.scan(role, element);
                if (element instanceof CtStatement && !(element instanceof CtStatementList)) {
                    CtStatement n = (CtStatement) element;

                    if (isTainted(n)) {
                        // This is to compute whether Stmt n is the first
                        // non-decl statement in a CompoundStmt
                        genReplaceStmt(n);
                        // todo: exact condition for DeclStmt and LabelStmt
                        if (!isDeclStmt(n) && !isLabelStmt(n))
                            genAddIfGuard(n);
                        genAddStatement(n);
                        genAddIfExit(n);
                    }
                    else if (n instanceof CtIf) {
                        CtIf IFS = (CtIf) n;
                        CtStatement thenBlock = IFS.getThenStatement();
                        CtStatement firstS = thenBlock;
                        if (thenBlock instanceof CtStatementList) {
                            CtStatementList CS = (CtStatementList) thenBlock;
                            if (CS.getStatements().size() > 1)
                                firstS = CS.getStatements().get(0);
                        }
                        if (isTainted(thenBlock) || isTainted(firstS)) {
                            genAddStatement(n);
                        }
                    }
                }
            }
        };
        // traverse (i.e. go to each node) the AST of clang::ASTContext (the top declaration context)
        scanner.scan(diffEntry.srcCommonAncestor);
        return repairs;
    }

    // based on LocationFuzzer class
    private Set<CtElement> fuzzyLocator(CtElement statement) {
        Set<CtElement> locations = new HashSet<>();
        // todo: check all conditions like this
        if (statement instanceof CtMethod || statement instanceof CtClass || statement instanceof CtIf || statement instanceof CtStatementList) {
            locations.add(statement);
        } else {
            List<CtStatement> statements = statement.getParent().getElements(new TypeFilter<>(CtStatement.class));
//            System.out.println(statement);
//            System.out.println(statements);
            if (statement.getParent() instanceof CtStatement) {
                statements = statements.subList(1, statements.size());
            }
            assert statements.contains(statement);
            int idx = statements.indexOf(statement);
            if (idx > 0)
                locations.add(statements.get(idx - 1));
            locations.add(statements.get(idx));
            if (idx < statements.size() - 1)
                locations.add(statements.get(idx + 1));
        }
        return locations;
    }
}
