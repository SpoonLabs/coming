package prophet4j.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prophet4j.meta.RepairStruct.DiffEntry;
import prophet4j.meta.RepairStruct.Repair;
import prophet4j.meta.RepairStruct.RepairAction;
import prophet4j.meta.RepairType.RepairActionKind;
import prophet4j.meta.RepairType.RepairCandidateKind;
import spoon.Launcher;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

// based on RepairGenerator.cpp
public class RepairGenerator {
    private Set<CtElement> area; // loc_map
    private DiffEntry diffEntry;
    private CoreFactory factory;
    private RepairAnalyzer repairAnalyzer = new RepairAnalyzer();
    private List<Repair> repairs = new ArrayList<>();
    private Map<CtStatementList, Integer> compound_counter = new HashMap<>();
    // todo: consider in_yacc_func
//     boolean in_yacc_func;

    public RepairGenerator(DiffEntry diffEntry) {
        this.area = fuzzyLocator(diffEntry.srcNode);
        this.diffEntry = diffEntry;
        this.factory = new Launcher().getFactory().Core();
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

    private void genTightCondition(CtIf n) {
        CtExpression<Boolean> oldCondition = n.getCondition();
        CtLiteral<Boolean> placeholder = factory.createLiteral();
        placeholder.setValue(true); // consider the placeholder, should this be more concrete?
        CtUnaryOperator<Boolean> tmpCondition = factory.createUnaryOperator();
        tmpCondition.setKind(UnaryOperatorKind.NOT);
        tmpCondition.setOperand(placeholder);
        CtBinaryOperator<Boolean> newCondition = factory.createBinaryOperator();
        newCondition.setKind(BinaryOperatorKind.AND);
        newCondition.setLeftHandOperand(oldCondition);
        newCondition.setRightHandOperand(placeholder);

        CtIf S = n.clone();
        S.setCondition(newCondition);
        // S is n.clone() so S is guessed owning same parent as n
//        S.setParent(n.getParent()); // for FUNC_ARGUMENT_VF

        Repair repair = new Repair();
        repair.actions.clear();
        repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, S));
        repair.actions.add(new RepairAction(S, placeholder, repairAnalyzer.getCondCandidateVars(n)));
        repair.kind = RepairCandidateKind.TightenConditionKind;
        repairs.add(repair);
        // we do not consider the case of short-circuit evaluation at all
    }

    private void genLooseCondition(CtIf n) {
        CtExpression<Boolean> oldCondition = n.getCondition();
        CtLiteral<Boolean> placeholder = factory.createLiteral();
        placeholder.setValue(true); // consider the placeholder, should this be more concrete?
        CtBinaryOperator<Boolean> newCondition = factory.createBinaryOperator();
        newCondition.setKind(BinaryOperatorKind.OR);
        newCondition.setLeftHandOperand(oldCondition);
        newCondition.setRightHandOperand(placeholder);

        CtIf S = n.clone();
        S.setCondition(newCondition);
        // S is n.clone() so S is guessed owning same parent as n
//        S.setParent(n.getParent()); // for FUNC_ARGUMENT_VF

        Repair repair = new Repair();
        repair.actions.clear();
        repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, S));
        repair.actions.add(new RepairAction(S, placeholder, repairAnalyzer.getCondCandidateVars(n)));
        repair.kind = RepairCandidateKind.LoosenConditionKind;
        repairs.add(repair);
        // we do not consider the case of short-circuit evaluation at all
    }

    private void genAddIfGuard(CtStatement n) {
        CtLiteral<Boolean> placeholder = factory.createLiteral();
        placeholder.setValue(true); // consider the placeholder, should this be more concrete?
        CtUnaryOperator<Boolean> guardCondition = factory.createUnaryOperator();
        guardCondition.setKind(UnaryOperatorKind.NOT);
        guardCondition.setOperand(placeholder);

        CtIf guardIf = factory.createIf();
        guardIf.setCondition(guardCondition);
        guardIf.setThenStatement(n.clone()); // i guess guardIf would be n.clone()'s parent automatically
        guardIf.setParent(n.getParent()); // for FUNC_ARGUMENT_VF

        Repair repair = new Repair();
        repair.actions.clear();
        repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, guardIf));
        repair.actions.add(new RepairAction(guardIf, placeholder, repairAnalyzer.getCondCandidateVars(n)));
        repair.kind = RepairCandidateKind.GuardKind;
        repairs.add(repair);
        // we do not consider the case of if statement as special at all
    }

    private void genAddIfExit(CtStatement n) {
        CtLiteral<Boolean> placeholder = factory.createLiteral();
        placeholder.setValue(true); // consider the placeholder, should this be more concrete?
        Repair repair = new Repair();
        CtMethod curFD = repairAnalyzer.getCurrentFunction(n);
        CtStatement lastStatement = curFD.getBody().getLastStatement();
//        CtReturn ctReturn = curFD.getBody().getLastStatement();
//        Type returnType = ctReturn.getClass().getGenericSuperclass();
        List<CtReturn> ctReturns = lastStatement.getElements(new TypeFilter<>(CtReturn.class));
        Type returnType = void.class;
        if (ctReturns.size() > 0) {
            returnType = ctReturns.get(0).getClass().getGenericSuperclass();
        }
        if (returnType == void.class) {
            CtLiteral<Object> returnValue = factory.createLiteral();
            returnValue.setValue(null); // is it equivalent to void ?
            CtReturn<Object> RS = factory.createReturn();
            RS.setReturnedExpression(returnValue);
            CtIf IFS = factory.createIf();
            IFS.setCondition(placeholder);
            IFS.setThenStatement(RS);
            IFS.setParent(n.getParent()); // for FUNC_ARGUMENT_VF
            repair.actions.clear();
            repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, n, IFS));
            repair.actions.add(new RepairAction(IFS, placeholder, repairAnalyzer.getCondCandidateVars(n)));
            repair.kind = RepairCandidateKind.IfExitKind;
            repairs.add(repair);
        }
        else {
            List<CtExpression> exprs = repairAnalyzer.getCandidateReturnExpr(n, returnType);
            for (CtExpression placeholder2 : exprs) {
                CtReturn<Object> RS = factory.createReturn();
                RS.setReturnedExpression(placeholder2);
                CtIf IFS = factory.createIf();
                IFS.setCondition(placeholder);
                IFS.setThenStatement(RS);
                IFS.setParent(n.getParent()); // for FUNC_ARGUMENT_VF
                repair.actions.clear();
                repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, n, IFS));
                repair.actions.add(new RepairAction(IFS, placeholder, repairAnalyzer.getCondCandidateVars(n)));
                repair.kind = RepairCandidateKind.IfExitKind;
                repairs.add(repair);
            }
        }
        if (repairAnalyzer.isInsideLoop(n)) {
            CtBreak BS = factory.createBreak();
            CtIf IFS = factory.createIf();
            IFS.setCondition(placeholder);
            IFS.setThenStatement(BS);
            IFS.setParent(n.getParent()); // for FUNC_ARGUMENT_VF
            repair.actions.clear();
            repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, n, IFS));
            repair.actions.add(new RepairAction(IFS, placeholder, repairAnalyzer.getCondCandidateVars(n)));
            repair.kind = RepairCandidateKind.IfExitKind;
            repairs.add(repair);
        }
    }

    private void genReplaceStmt(CtStatement n) {
        if (n instanceof CtExpression) {
            RepairAnalyzer.AtomReplaceVisitor V = repairAnalyzer.newAtomReplaceVisitor();
            V.TraverseStmt(n);
            for (CtElement it : V.getResult()) {
                Repair repair = new Repair();
                repair.actions.clear();
                repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, it));
                repair.kind = RepairCandidateKind.ReplaceKind;
                repair.oldRExpr = V.getOldRExpr(it);
                repair.newRExpr = V.getNewRExpr(it);
                repairs.add(repair);
            }
        }

        // I do not know its meaning as CtLiteral is not CtStatement
        if (n instanceof CtLiteral) {
            if (((CtLiteral) n).getValue() instanceof String) {
                CtLiteral<String> placeholder = factory.createLiteral();
                placeholder.setValue("");
                Repair repair = new Repair();
                repair.actions.clear();
                repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, placeholder));
                // this line seems meaningless
                repair.actions.add(new RepairAction(n, placeholder, new ArrayList<>()));
                repair.oldRExpr = n;
                repair.newRExpr = null;
                repair.kind = RepairCandidateKind.ReplaceStringKind;
                repairs.add(repair);
            }
        }

        if (n instanceof CtInvocation) {
            for (CtInvocation it : repairAnalyzer.getCandidateCalleeFunction((CtInvocation) n)) {
                Repair repair = new Repair();
                repair.actions.clear();
                repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, n, it));
                repair.kind = RepairCandidateKind.ReplaceKind;
                repair.oldRExpr = ((CtInvocation) n).getExecutable();
                repair.newRExpr = it;
                repairs.add(repair);
            }
        }
    }

    // isValidStmt() were commented as thought unnecessary
    // also I just doubt the validity of this kind of repair
    private void genAddStatement(CtStatement n) {
        Set<CtElement> exprs = repairAnalyzer.getGlobalCandidateExprs(n);
        for (CtElement it: exprs) {
            RepairAnalyzer.AtomReplaceVisitor V = repairAnalyzer.newAtomReplaceVisitor();
            V.TraverseStmt(it);
//            if (!repairAnalyzer.isValidStmt(it))
//                continue;

            for (CtElement it2 : V.getResult()) {
//                boolean valid_after_replace = repairAnalyzer.isValidStmt(it2);
//                if (!valid_after_replace) continue;
                Repair repair = new Repair();
                repair.actions.clear();
                repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, n, it2));
                repair.kind = RepairCandidateKind.AddAndReplaceKind;
            }
            Repair repair = new Repair();
            repair.actions.clear();
            repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, n, it));
            repair.kind = RepairCandidateKind.AddAndReplaceKind;
        }

        // insert if_stmt without atom replace if possible
        Set<CtElement> stmts = repairAnalyzer.getGlobalCandidateIfStmts(n);
        for (CtElement it : stmts) {
//            boolean valid = repairAnalyzer.isValidStmt(it);
//            if (!valid) continue;
            Repair repair = new Repair();
            repair.actions.clear();
            repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, n, it));
            repair.kind = RepairCandidateKind.AddAndReplaceKind;
        }
    }

    public Repair obtainHumanRepair() {
        Repair repair = new Repair();
        repair.kind = null; // related to RepairFeature
        repair.oldRExpr = null; // related to ValueFeature
        repair.newRExpr = null; // related to ValueFeature

        // todo: more checks
        // based on matchCandidateWithHumanFix()
        CtElement srcNode = diffEntry.srcNode;
        CtElement dstNode = diffEntry.dstNode;
        switch (diffEntry.type) {
            case DeleteAction: // kind
                // GuardKind: // INSERT_GUARD_RF
                repair.kind = RepairCandidateKind.GuardKind;
                break;
            case InsertAction: // kind
                // IfExitKind: // INSERT_CONTROL_RF
                // AddAndReplaceKind: // INSERT_STMT_RF
                if (dstNode instanceof CtIf) {
                    repair.kind = RepairCandidateKind.IfExitKind;
                } else {
                    repair.kind = RepairCandidateKind.AddAndReplaceKind;
                }
                // compare with others in obtainRepairCandidates()
                repair.actions.add(new RepairAction(RepairActionKind.InsertMutationKind, diffEntry.srcNode, diffEntry.dstNode));
                break;
            case ReplaceAction: // kind // oldRExpr // newRExpr
                // IfExitKind: // INSERT_CONTROL_RF
                // GuardKind: // INSERT_GUARD_RF
                // SpecialGuardKind: // INSERT_GUARD_RF
                // LoosenConditionKind: // REPLACE_COND_RF
                // TightenConditionKind: // REPLACE_COND_RF
                // ReplaceKind: // REPLACE_STMT_RF
                // ReplaceStringKind: // REPLACE_STMT_RF
                CtIf IF2;
                if (dstNode instanceof CtIf) {
                    IF2 = (CtIf) dstNode;
                } else {
                    IF2 = dstNode.getParent(new TypeFilter<>(CtIf.class));
                }
                if (IF2 != null) {
                    CtIf IF1;
                    if (srcNode instanceof CtIf) {
                        IF1 = (CtIf) srcNode;
                    } else {
                        IF1 = srcNode.getParent(new TypeFilter<>(CtIf.class));
                    }
                    if (IF1 != null) {
                        // make sure repair.kind would be assigned one value
                        repair.kind = RepairCandidateKind.SpecialGuardKind;
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
                    if (diffEntry.srcNode instanceof CtLiteral) {
                        repair.kind = RepairCandidateKind.ReplaceStringKind;
                    } else {
                        repair.kind = RepairCandidateKind.ReplaceKind;
                    }
                }
                repair.oldRExpr = diffEntry.srcNode;
                repair.newRExpr = diffEntry.dstNode;
                // compare with others in obtainRepairCandidates()
                repair.actions.add(new RepairAction(RepairActionKind.ReplaceMutationKind, diffEntry.srcNode, diffEntry.dstNode));
                break;
            case UnknownAction:
                break;
        }

        List<CtElement> candidates = diffEntry.dstNode.getElements(new TypeFilter<>(CtElement.class));
        repair.actions.add(new RepairAction(diffEntry.srcNode, diffEntry.dstNode, candidates));
        return repair;
    }

    // https://people.csail.mit.edu/fanl/papers/spr-fse15.pdf <3.2 Transformation Schemas> Figure 4
    public List<Repair> obtainRepairCandidates() {
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
        scanner.scan(diffEntry.srcNode);
        return repairs;
    }

    // based on LocationFuzzer class
    private Set<CtElement> fuzzyLocator(CtElement statement) {
        Set<CtElement> locations = new HashSet<>();
        // todo: check all conditions like this
        if (statement instanceof CtMethod || statement instanceof CtClass || statement instanceof CtIf || statement instanceof CtStatementList) {
            locations.add(statement);
        } else {
            // "int a;" is not CtStatement?
            List<CtElement> statements = statement.getParent().getElements(new TypeFilter<>(CtElement.class));
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
