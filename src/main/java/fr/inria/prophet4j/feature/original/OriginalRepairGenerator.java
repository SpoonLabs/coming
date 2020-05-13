package fr.inria.prophet4j.feature.original;

import java.lang.reflect.Type;
import java.util.*;

import fr.inria.prophet4j.utility.Structure.RepairKind;
import fr.inria.prophet4j.utility.Structure.Repair;
import fr.inria.prophet4j.utility.Structure.DiffEntry;
import fr.inria.prophet4j.feature.RepairGenerator;
import fr.inria.prophet4j.feature.original.util.OriginalRepairAnalyzer;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.code.*;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;

// based on RepairGenerator.cpp
public class OriginalRepairGenerator implements RepairGenerator {
    private Set<CtElement> area; // loc_map
    private DiffEntry diffEntry;
    private CoreFactory factory;
    private List<Repair> repairs = new ArrayList<>();
    private Map<CtStatementList, Integer> compound_counter = new HashMap<>();
    private OriginalRepairAnalyzer repairAnalyzer = new OriginalRepairAnalyzer();

    public OriginalRepairGenerator(DiffEntry diffEntry) {
        this.area = fuzzyLocator(diffEntry.srcNode);
        this.diffEntry = diffEntry;
        this.factory = new Launcher().getFactory().Core();
        this.repairs.clear();
        this.compound_counter.clear();
    }

    private boolean isTainted(CtStatement S) {
        if (S == null) return false;
        if (area.contains(S))
            return true;
        // why Prophet does not need the second condition ?
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
        S.setParent(n.getParent());
        S.setCondition(newCondition);

        Repair repair = new Repair();
        repair.kind = RepairKind.TightenConditionKind;
        repair.isReplace = true;
        repair.srcElem = n;
        repair.dstElem = S;
        repair.atoms.addAll(repairAnalyzer.getCondCandidateVars(n));
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
        S.setParent(n.getParent());
        S.setCondition(newCondition);

        Repair repair = new Repair();
        repair.kind = RepairKind.LoosenConditionKind;
        repair.isReplace = true;
        repair.srcElem = n;
        repair.dstElem = S;
        repair.atoms.addAll(repairAnalyzer.getCondCandidateVars(n));
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
        guardIf.setParent(n.getParent());
        guardIf.setCondition(guardCondition);
        guardIf.setThenStatement(n.clone());

        Repair repair = new Repair();
        repair.kind = RepairKind.GuardKind;
        repair.isReplace = true;
        repair.srcElem = n;
        repair.dstElem = guardIf;
        repair.atoms.addAll(repairAnalyzer.getCondCandidateVars(n));
        repairs.add(repair);
        // we do not consider the case of if statement as special at all
    }

    private void genAddIfExit(CtStatement n) {
        CtLiteral<Boolean> placeholder = factory.createLiteral();
        placeholder.setValue(true); // consider the placeholder, should this be more concrete?
        Type returnType = void.class;
        CtMethod curFD = repairAnalyzer.getCurrentFunction(n);
        if (curFD != null) {
            CtStatement lastStatement = curFD.getBody().getLastStatement();
//            CtReturn ctReturn = curFD.getBody().getLastStatement();
//            Type returnType = ctReturn.getClass().getGenericSuperclass();
            List<CtReturn> ctReturns = lastStatement.getElements(new TypeFilter<>(CtReturn.class));
            if (ctReturns.size() > 0) {
                returnType = ctReturns.get(0).getClass().getGenericSuperclass();
            }
        }
        if (returnType == void.class) {
            CtLiteral<Object> returnValue = factory.createLiteral();
            returnValue.setValue(null); // is it equivalent to void ?
            CtReturn<Object> RS = factory.createReturn();
            RS.setReturnedExpression(returnValue);
            CtIf IFS = factory.createIf();
            IFS.setParent(n.getParent());
            IFS.setCondition(placeholder);
            IFS.setThenStatement(RS);
            Repair repair = new Repair();
            repair.kind = RepairKind.IfExitKind;
            repair.isReplace = false;
            repair.srcElem = n;
            repair.dstElem = IFS;
            repair.atoms = repairAnalyzer.getCondCandidateVars(n);
            repairs.add(repair);
        }
        else {
            List<CtExpression> exprs = repairAnalyzer.getCandidateReturnExpr(n, returnType);
            for (CtExpression placeholder2 : exprs) {
                CtReturn<Object> RS = factory.createReturn();
                RS.setReturnedExpression(placeholder2);
                CtIf IFS = factory.createIf();
                IFS.setParent(n.getParent());
                IFS.setCondition(placeholder);
                IFS.setThenStatement(RS);
                Repair repair = new Repair();
                repair.kind = RepairKind.IfExitKind;
                repair.isReplace = false;
                repair.srcElem = n;
                repair.dstElem = IFS;
                repair.atoms = repairAnalyzer.getCondCandidateVars(n);
                repairs.add(repair);
            }
        }
        if (repairAnalyzer.isInsideLoop(n)) {
            CtBreak BS = factory.createBreak();
            CtIf IFS = factory.createIf();
            IFS.setParent(n.getParent());
            IFS.setCondition(placeholder);
            IFS.setThenStatement(BS);
            Repair repair = new Repair();
            repair.kind = RepairKind.IfExitKind;
            repair.isReplace = false;
            repair.srcElem = n;
            repair.dstElem = IFS;
            repair.atoms = repairAnalyzer.getCondCandidateVars(n);
            repairs.add(repair);
        }
    }

    private void genReplaceStmt(CtStatement n) {
        if (n instanceof CtExpression) {
            OriginalRepairAnalyzer.AtomReplaceVisitor V = repairAnalyzer.newAtomReplaceVisitor();
            V.TraverseStmt(n);
            for (CtElement it : V.getResult()) {
                Repair repair = new Repair();
                repair.kind = RepairKind.ReplaceKind;
                repair.isReplace = true;
                repair.srcElem = n;
                repair.dstElem = it;
                repair.atoms.addAll(new ArrayList<>());
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
                repair.kind = RepairKind.ReplaceStringKind;
                repair.isReplace = true;
                repair.srcElem = n;
                repair.dstElem = placeholder;
                repair.atoms.addAll(new ArrayList<>());
                repair.oldRExpr = n;
                repair.newRExpr = null;
                repairs.add(repair);
            }
        }

        if (n instanceof CtInvocation) {
            for (CtInvocation it : repairAnalyzer.getCandidateCalleeFunction((CtInvocation) n)) {
                Repair repair = new Repair();
                repair.kind = RepairKind.ReplaceKind;
                repair.isReplace = true;
                repair.srcElem = n;
                repair.dstElem = it;
                repair.atoms.addAll(new ArrayList<>());
                repair.oldRExpr = ((CtInvocation) n).getExecutable();
                repair.newRExpr = it;
                repairs.add(repair);
            }
        }
    }

    // isValidStmt() were commented as thought unnecessary
    // also I just doubt the validity of this kind of repair
    private void genAddStmt(CtStatement n) {
        Set<CtElement> exprs = repairAnalyzer.getGlobalCandidateExprs(n);
        for (CtElement it: exprs) {
            OriginalRepairAnalyzer.AtomReplaceVisitor V = repairAnalyzer.newAtomReplaceVisitor();
            V.TraverseStmt(it);
//            if (!repairAnalyzer.isValidStmt(it))
//                continue;

            for (CtElement it2 : V.getResult()) {
//                boolean valid_after_replace = repairAnalyzer.isValidStmt(it2);
//                if (!valid_after_replace) continue;
                Repair repair = new Repair();
                repair.kind = RepairKind.AddAndReplaceKind;
                repair.isReplace = false;
                repair.srcElem = n;
                repair.dstElem = it2;
                repair.atoms.addAll(new ArrayList<>());
                repairs.add(repair);
            }
            Repair repair = new Repair();
            repair.kind = RepairKind.AddAndReplaceKind;
            repair.isReplace = false;
            repair.srcElem = n;
            repair.dstElem = it;
            repair.atoms.addAll(new ArrayList<>());
            repairs.add(repair);
        }

        // insert if_stmt without atom replace if possible
        Set<CtElement> stmts = repairAnalyzer.getGlobalCandidateIfStmts(n);
        for (CtElement it : stmts) {
//            boolean valid = repairAnalyzer.isValidStmt(it);
//            if (!valid) continue;
            Repair repair = new Repair();
            repair.kind = RepairKind.AddAndReplaceKind;
            repair.isReplace = false;
            repair.srcElem = n;
            repair.dstElem = it;
            repair.atoms.addAll(new ArrayList<>());
            repairs.add(repair);
        }
    }

    public Repair obtainHumanRepair() {
        Repair repair = new Repair();
        repair.kind = null; // related to RepairFeature
        repair.isReplace = false;
        repair.srcElem = diffEntry.srcNode;
        repair.dstElem = diffEntry.dstNode;
        repair.atoms.addAll(new ArrayList<>());
        repair.oldRExpr = null; // related to SchemaFeature
        repair.newRExpr = null; // related to SchemaFeature

        // todo improve
        // based on matchCandidateWithHumanFix()
        switch (diffEntry.type) {
		case DeleteType: // only delete
			repair.kind = RepairKind.RemoveSTMTKind;
			if (diffEntry.srcNode instanceof CtIf) {
				repair.kind = RepairKind.RemoveWholeIFKind;
			} else if (diffEntry.srcNode instanceof CtBlock) {
				repair.kind = RepairKind.RemoveWholeBlockKind;
			}
			break;
		case PartialDeleteType: // delete + move kind
			repair.kind = RepairKind.RemoveSTMTKind;
			if (diffEntry.srcNode instanceof CtIf) {
				repair.kind = RepairKind.RemovePartialIFKind;
			}
			break;
            case InsertType: // kind
                // IfExitKind: // INSERT_CONTROL_RF
                // AddAndReplaceKind: // INSERT_STMT_RF
                if (diffEntry.dstNode instanceof CtIf) {
                    repair.kind = RepairKind.IfExitKind;
                } else {
                    repair.kind = RepairKind.AddAndReplaceKind;
                }
                // compare with others in obtainRepairCandidates()
                break;
            case UpdateType: // kind // oldRExpr // newRExpr
                // IfExitKind: // INSERT_CONTROL_RF
                // GuardKind: // INSERT_GUARD_RF
                // SpecialGuardKind: // INSERT_GUARD_RF
                // LoosenConditionKind: // REPLACE_COND_RF
                // TightenConditionKind: // REPLACE_COND_RF
                // ReplaceKind: // REPLACE_STMT_RF
                // ReplaceStringKind: // REPLACE_STMT_RF
                CtIf IF2;
                if (diffEntry.dstNode instanceof CtIf) {
                    IF2 = (CtIf) diffEntry.dstNode;
                } else {
                    IF2 = diffEntry.dstNode.getParent(new TypeFilter<>(CtIf.class));
                }
                if (IF2 != null) {
                    CtIf IF1;
                    if (diffEntry.srcNode instanceof CtIf) {
                        IF1 = (CtIf) diffEntry.srcNode;
                    } else {
                        IF1 = diffEntry.srcNode.getParent(new TypeFilter<>(CtIf.class));
                    }
                    if (IF1 != null) {
                        // make sure repair.kind would be assigned one value
                        repair.kind = RepairKind.SpecialGuardKind;
                        if (IF1.getThenStatement().equals(IF2.getThenStatement())) {
                            // LoosenConditionKind and TightenConditionKind are almost same as both are REPLACE_COND_RF
                            if (IF1.getElseStatement()!=null && IF2.getElseStatement()!=null) {
                                if (IF1.getElseStatement().equals(IF2.getElseStatement())) {
                                    repair.kind = RepairKind.LoosenConditionKind;
                                }
                            } else {
                                repair.kind = RepairKind.LoosenConditionKind;
                            }
                        }
                    } else {
                        CtStatement S = IF2.getThenStatement();
                        if (S instanceof CtCFlowBreak) {
                            repair.kind = RepairKind.IfExitKind;
                        } else {
                            repair.kind = RepairKind.GuardKind;
                        }
                    }
                } else {
                    if (diffEntry.srcNode instanceof CtLiteral) {
                        repair.kind = RepairKind.ReplaceStringKind;
                    } else {
                        repair.kind = RepairKind.ReplaceKind;
                    }
                }
                repair.oldRExpr = diffEntry.srcNode;
                repair.newRExpr = diffEntry.dstNode;
//                if (repair.oldRExpr instanceof CtExpression) {
//                    if (!(repair.oldRExpr instanceof CtAnnotation || repair.oldRExpr instanceof CtImport)) {
//                        while (!(repair.oldRExpr instanceof CtStatement)){
//                            repair.oldRExpr = repair.oldRExpr.getParent();
//                        }
//                    }
//                }
//                if (repair.newRExpr instanceof CtExpression) {
//                    if (!(repair.newRExpr instanceof CtAnnotation || repair.newRExpr instanceof CtImport)) {
//                        while (!(repair.newRExpr instanceof CtStatement)){
//                            repair.newRExpr = repair.newRExpr.getParent();
//                        }
//                    }
//                }
                // compare with others in obtainRepairCandidates()
                repair.isReplace = true;
                break;
        }
        try {
            List<CtElement> candidates = diffEntry.dstNode.getElements(new TypeFilter<>(CtElement.class));
            repair.atoms.addAll(candidates);
        } catch (Exception e) {
            // such as public, final, static
        }
        return repair;
    }

    // https://people.csail.mit.edu/fanl/papers/spr-fse15.pdf <3.2 Transformation Schemas> Figure 4
    public List<Repair> obtainRepairCandidates() {
        CtScanner scanner = new CtScanner() {

            // https://clang.llvm.org/doxygen/classclang_1_1LabelStmt.html
            private boolean isLabelStmt(CtStatement statement) {
                return false;
            }

            // todo check
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
                        // todo check
                        if (!isDeclStmt(n) && !isLabelStmt(n))
                            genAddIfGuard(n);
                        genAddStmt(n);
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
                            genAddStmt(n);
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
        if (statement instanceof CtMethod || statement instanceof CtClass || statement instanceof CtIf || statement instanceof CtStatementList) {
            locations.add(statement);
        } else {
            // "int a;" is not CtStatement?
            CtElement parent = statement.getParent();
            if (parent != null) {
                List<CtElement> statements = parent.getElements(new TypeFilter<>(CtElement.class));
                if (parent instanceof CtStatement) {
                    statements = statements.subList(1, statements.size());
                }
                int idx = statements.indexOf(statement);
                if (idx >= 0) {
                    if (idx > 0)
                        locations.add(statements.get(idx - 1));
                    locations.add(statements.get(idx));
                    if (idx < statements.size() - 1)
                        locations.add(statements.get(idx + 1));
                }
            }
        }
        return locations;
    }
}