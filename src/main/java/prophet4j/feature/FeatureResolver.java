package prophet4j.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import picocli.CommandLine.Option;
import prophet4j.defined.FeatureStruct.Feature;
import prophet4j.defined.FeatureStruct.FeatureManager;
import prophet4j.defined.FeatureType;
import prophet4j.defined.FeatureType.AtomicFeature;
import prophet4j.defined.FeatureType.JointType;
import prophet4j.defined.FeatureType.Position;
import prophet4j.defined.FeatureType.RepairFeature;
import prophet4j.defined.FeatureType.ValueFeature;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.CtWhile;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.CtPathStringBuilder;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

// this is the old implementation which will be removed someday
// based on FeatureExtract.cpp, RepairGenerator.cpp
public class FeatureResolver {

    // check these variables
    @Option(names = {"--disable-mod"}, description = "Disable Modification Features.")
    private boolean DisableModificationFeatures = false;
    @Option(names = {"--disable-sema-cross"}, description = "Disable Semantic Features, setting them all zero!")
    private boolean DisableSemanticCrossFeatures = false;
    @Option(names = {"--disable-sema-value"}, description = "Disable Semantic-Value Features, setting them all zero!")
    private boolean DisableSemanticValueFeatures = false;

    private List<CtElement> getAtoms(CtElement element) {
        List<CtElement> atoms = new ArrayList<>();

        CtScanner scanner = new CtScanner() {
            @Override
            public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
                super.visitCtVariableRead(variableRead);
//                System.out.println("R:" + variableRead.toString());
                atoms.add(variableRead);
            }

            @Override
            public <T> void visitCtVariableWrite(CtVariableWrite<T> variableWrite) {
                super.visitCtVariableWrite(variableWrite);
//                System.out.println("W:" + variableWrite.toString());
                atoms.add(variableWrite);
            }

            @Override
            public <T> void visitCtLiteral(CtLiteral<T> ctLiteral) {
                super.visitCtLiteral(ctLiteral);
                atoms.add(ctLiteral);
            }
        };
        scanner.scan(element);

//        System.out.println("----getAtoms----");
//        System.out.println(atoms);
        return atoms;
    }

    private EnumSet<AtomicFeature> getAtomicKinds(CtElement atom, CtElement element, boolean isReplace) {
        EnumSet<AtomicFeature> atomicFeatures = EnumSet.noneOf(AtomicFeature.class);
        if (element == null) {
            return atomicFeatures;
        }

        if (atom != null) {
            if (atom instanceof CtLiteral) {
                Object value = ((CtLiteral) atom).getValue();
                if (value != null) {
                    if (value.equals(0)) {
                        atomicFeatures.add(AtomicFeature.ASSIGN_ZERO_AF);
                    } else {
                        atomicFeatures.add(AtomicFeature.ASSIGN_CONST_AF);
                    }
                }
            } else if (atom instanceof CtVariable || atom instanceof CtVariableAccess) {
//                atomicFeatures.add(AtomicFeature.VARIABLE_AF);
            }

            if (!getAtoms(element).contains(atom)) {
//                atomicFeatures.add(AtomicFeature.EXCLUDE_ATOM_AF);
            } else {
                List<String> operatorList = new ArrayList<>();
                // todo: one special case of string const replacement (ABST_V_AF) (one idea is to use contains function)
                CtScanner scanner = new CtScanner() {
                    @Override
                    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
                        super.visitCtBinaryOperator(operator);
                        operatorList.add(operator.getKind().toString());

                        if (atom instanceof CtVariable || atom instanceof CtVariableAccess) {
                            if (operator.getLeftHandOperand().equals(atom)) {
//                                atomicFeatures.add(AtomicFeature.OPERATE_LHS_AF);
                            }
                            if (operator.getRightHandOperand().equals(atom)) {
//                                atomicFeatures.add(AtomicFeature.OPERATE_RHS_AF);
                            }
                        }
                        // I guess it should be redundant todo: maybe check again someday
//                        if (operator.getKind().equals(BinaryOperatorKind.AND)
//                                || operator.getKind().equals(BinaryOperatorKind.OR)) {
//                            atomicFeatures.addAll(getAtomicKinds(atom, operator.getLeftHandOperand(), isReplace));
//                            atomicFeatures.addAll(getAtomicKinds(atom, operator.getRightHandOperand(), isReplace));
//                        }
                    }

                    @Override
                    public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
                        super.visitCtUnaryOperator(operator);
                        operatorList.add(operator.getKind().toString());
                    }

                    @Override
                    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> ctAssignment) {
                        super.visitCtAssignment(ctAssignment);
                        if (atom instanceof CtLiteral) {
                            if (ctAssignment.getAssignment().equals(atom)) {
//                                atomicFeatures.add(AtomicFeature.ASSIGN_RHS_AF);
                            }
                        } else if (atom instanceof CtVariable || atom instanceof CtVariableAccess) {
                            if (ctAssignment.getAssigned().equals(atom)) {
                                atomicFeatures.add(AtomicFeature.ASSIGN_LHS_AF);
                            } else {
//                                atomicFeatures.add(AtomicFeature.ASSIGN_RHS_AF);
                            }
                        }
                    }

                    @Override
                    public <T, A extends T> void visitCtOperatorAssignment(CtOperatorAssignment<T, A> assignment) {
                        super.visitCtOperatorAssignment(assignment);
                        if (atom instanceof CtLiteral) {
                            if (assignment.getAssignment().equals(atom)) {
//                                atomicFeatures.add(AtomicFeature.ASSIGN_RHS_AF);
                            }
                        } else if (atom instanceof CtVariable || atom instanceof CtVariableAccess) {
                            if (assignment.getAssigned().equals(atom)) {
                                atomicFeatures.add(AtomicFeature.ASSIGN_LHS_AF);
                            } else {
//                                atomicFeatures.add(AtomicFeature.ASSIGN_RHS_AF);
                            }
                        }
                    }

                    // I guess it should be redundant todo: maybe check again someday
//                    @Override
//                    public void visitCtTry(CtTry tryBlock) {
//                        super.visitCtTry(tryBlock);
//                        for (CtStatement ctStmt : tryBlock.getBody().getStatements()) {
//                            atomicFeatures.addAll(getAtomicKinds(atom, ctStmt, isReplace));
//                        }
//                    }
//
//                    @Override
//                    public void visitCtCatch(CtCatch catchBlock) {
//                        super.visitCtCatch(catchBlock);
//                        for (CtStatement ctStmt : catchBlock.getBody().getStatements()) {
//                            atomicFeatures.addAll(getAtomicKinds(atom, ctStmt, isReplace));
//                        }
//                    }
//
//                    @Override
//                    public <R> void visitCtBlock(CtBlock<R> block) {
//                        super.visitCtBlock(block);
//                        for (CtStatement ctStmt : block.getStatements()) {
//                            atomicFeatures.addAll(getAtomicKinds(atom, ctStmt, isReplace));
//                        }
//                    }
//
//                    @Override
//                    public void visitCtIf(CtIf ifElement) {
//                        super.visitCtIf(ifElement);
//                        atomicFeatures.addAll(getAtomicKinds(atom, ifElement.getCondition(), isReplace));
//                        atomicFeatures.addAll(getAtomicKinds(atom, ifElement.getElseStatement(), isReplace));
//                        atomicFeatures.addAll(getAtomicKinds(atom, ifElement.getThenStatement(), isReplace));
//                    }
//
//                    @Override
//                    public void visitCtDo(CtDo doLoop) {
//                        super.visitCtDo(doLoop);
//                        atomicFeatures.addAll(getAtomicKinds(atom, doLoop.getLoopingExpression(), isReplace));
//                        atomicFeatures.addAll(getAtomicKinds(atom, doLoop.getBody(), isReplace));
//                    }
//
//                    @Override
//                    public void visitCtFor(CtFor forLoop) {
//                        super.visitCtFor(forLoop);
//                        for (CtStatement ctStmt : forLoop.getForInit()) {
//                            atomicFeatures.addAll(getAtomicKinds(atom, ctStmt, isReplace));
//                        }
//                        atomicFeatures.addAll(getAtomicKinds(atom, forLoop.getExpression(), isReplace));
//                        for (CtStatement ctStmt : forLoop.getForUpdate()) {
//                            atomicFeatures.addAll(getAtomicKinds(atom, ctStmt, isReplace));
//                        }
//                        atomicFeatures.addAll(getAtomicKinds(atom, forLoop.getBody(), isReplace));
//                    }
//
//                    @Override
//                    public void visitCtWhile(CtWhile whileLoop) {
//                        super.visitCtWhile(whileLoop);
//                        atomicFeatures.addAll(getAtomicKinds(atom, whileLoop.getLoopingExpression(), isReplace));
//                        atomicFeatures.addAll(getAtomicKinds(atom, whileLoop.getBody(), isReplace));
//                    }

                    @Override
                    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                        super.visitCtInvocation(invocation);
                        // some extra checks expected
                        atomicFeatures.add(AtomicFeature.CALLEE_AF);
                        atomicFeatures.add(AtomicFeature.CALL_ARGUMENT_AF);
                    }

                    @Override
                    public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
                        super.visitCtFieldRead(fieldRead);
                        atomicFeatures.add(AtomicFeature.MEMBER_ACCESS_AF);
                    }

                    @Override
                    public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
                        super.visitCtFieldWrite(fieldWrite);
                        atomicFeatures.add(AtomicFeature.MEMBER_ACCESS_AF);
                    }

                    @Override
                    public <T> void visitCtArrayRead(CtArrayRead<T> arrayRead) {
                        super.visitCtArrayRead(arrayRead);
                        atomicFeatures.add(AtomicFeature.INDEX_AF);
                    }

                    @Override
                    public <T> void visitCtArrayWrite(CtArrayWrite<T> arrayWrite) {
                        super.visitCtArrayWrite(arrayWrite);
                        atomicFeatures.add(AtomicFeature.INDEX_AF);
                    }
                };
                scanner.scan(element);

                if (operatorList.contains(UnaryOperatorKind.POS.toString())
                        || operatorList.contains(BinaryOperatorKind.PLUS.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_ADD_AF);
                }
                if (operatorList.contains(UnaryOperatorKind.NEG.toString())
                        || operatorList.contains(BinaryOperatorKind.MINUS.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_SUB_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.MUL.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_MUL_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.DIV.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_DIV_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.MOD.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_MOD_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.LE.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_LE_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.LT.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_LT_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.GE.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_GE_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.GT.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_GT_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.EQ.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_EQ_AF);
                }
                if (operatorList.contains(BinaryOperatorKind.NE.toString())) {
                    atomicFeatures.add(AtomicFeature.OP_NE_AF);
                }
                if (operatorList.contains(UnaryOperatorKind.PREINC.toString())
                        || operatorList.contains(UnaryOperatorKind.POSTINC.toString())) {
                    atomicFeatures.add(AtomicFeature.UOP_INC_AF);
                }
                if (operatorList.contains(UnaryOperatorKind.PREDEC.toString())
                        || operatorList.contains(UnaryOperatorKind.POSTDEC.toString())) {
                    atomicFeatures.add(AtomicFeature.UOP_DEC_AF);
                }
                if (operatorList.contains(UnaryOperatorKind.PREINC.toString())
                        || operatorList.contains(UnaryOperatorKind.POSTINC.toString())
                        || operatorList.contains(UnaryOperatorKind.PREDEC.toString())
                        || operatorList.contains(UnaryOperatorKind.POSTDEC.toString())) {
                    atomicFeatures.add(AtomicFeature.CHANGED_AF);
                }
            }
        }
        // only for StmtKind
        CtScanner scanner = new CtScanner() {
            @Override
            public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> ctAssignment) {
                super.visitCtAssignment(ctAssignment);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_ASSIGN_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_ASSIGN_AF);
                }
            }

            @Override
            public <T, A extends T> void visitCtOperatorAssignment(CtOperatorAssignment<T, A> assignment) {
                super.visitCtOperatorAssignment(assignment);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_ASSIGN_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_ASSIGN_AF);
                }
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                super.visitCtInvocation(invocation);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_CALL_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_CALL_AF);
                }
            }

            @Override
            public void visitCtIf(CtIf ifElement) {
                super.visitCtIf(ifElement);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_COND_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_COND_AF);
                }
            }

            @Override
            public void visitCtDo(CtDo doLoop) {
                super.visitCtDo(doLoop);
                atomicFeatures.add(AtomicFeature.STMT_LOOP_AF);
            }

            @Override
            public void visitCtFor(CtFor forLoop) {
                super.visitCtFor(forLoop);
                atomicFeatures.add(AtomicFeature.STMT_LOOP_AF);
            }

            @Override
            public void visitCtWhile(CtWhile whileLoop) {
                super.visitCtWhile(whileLoop);
                atomicFeatures.add(AtomicFeature.STMT_LOOP_AF);
            }

            @Override
            public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
                super.visitCtSwitch(switchStatement);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_CONTROL_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_CONTROL_AF);
                }
            }

            @Override
            public <S> void visitCtCase(CtCase<S> caseStatement) {
                super.visitCtCase(caseStatement);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_CONTROL_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_CONTROL_AF);
                }
            }

            @Override
            public void visitCtBreak(CtBreak breakStatement) {
                super.visitCtBreak(breakStatement);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_CONTROL_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_CONTROL_AF);
                }
            }

            @Override
            public void visitCtContinue(CtContinue continueStatement) {
                super.visitCtContinue(continueStatement);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_CONTROL_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_CONTROL_AF);
                }
            }

            @Override
            public <R> void visitCtReturn(CtReturn<R> returnStatement) {
                super.visitCtReturn(returnStatement);
                if (isReplace) {
                    atomicFeatures.add(AtomicFeature.R_STMT_CONTROL_AF);
                } else {
                    atomicFeatures.add(AtomicFeature.STMT_CONTROL_AF);
                }
            }
        };
        scanner.scan(element);

        // the priority is based on getFeatureResult() in prophet
//        if (atomicFeatures.contains(AtomicFeature.STMT_LOOP_AF)) {
//            atomicFeatures.remove(AtomicFeature.STMT_COND_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_LABEL_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_CONTROL_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_ASSIGN_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_CALL_AF);
//        } else if (atomicFeatures.contains(AtomicFeature.STMT_COND_AF)) {
//            atomicFeatures.remove(AtomicFeature.STMT_LABEL_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_CONTROL_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_ASSIGN_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_CALL_AF);
//        } else if (atomicFeatures.contains(AtomicFeature.STMT_LABEL_AF)) {
//            atomicFeatures.remove(AtomicFeature.STMT_CONTROL_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_ASSIGN_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_CALL_AF);
//        } else if (atomicFeatures.contains(AtomicFeature.STMT_CONTROL_AF)) {
//            atomicFeatures.remove(AtomicFeature.STMT_ASSIGN_AF);
//            atomicFeatures.remove(AtomicFeature.STMT_CALL_AF);
//        } else if (atomicFeatures.contains(AtomicFeature.STMT_ASSIGN_AF)) {
//            atomicFeatures.remove(AtomicFeature.STMT_CALL_AF);
//        }

//        System.out.println("----getAtomicKinds----");
//        System.out.println(atomicFeatures);
        return atomicFeatures;
    }

    private EnumSet<RepairFeature> getRepairTypes(Operation operation) {
        List<String> controlList = new ArrayList<>();
        List<String> blockList = new ArrayList<>();
        List<String> conditionList = new ArrayList<>();
        CtScanner scanner = new CtScanner() {
            @Override
            public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
                super.visitCtSwitch(switchStatement);
                controlList.add(switchStatement.toString());
            }

            @Override
            public <S> void visitCtCase(CtCase<S> caseStatement) {
                super.visitCtCase(caseStatement);
                controlList.add(caseStatement.toString());
            }

            @Override
            public void visitCtBreak(CtBreak breakStatement) {
                super.visitCtBreak(breakStatement);
                controlList.add(breakStatement.toString());
            }

            @Override
            public void visitCtContinue(CtContinue continueStatement) {
                super.visitCtContinue(continueStatement);
                controlList.add(continueStatement.toString());
            }

            @Override
            public <R> void visitCtReturn(CtReturn<R> returnStatement) {
                super.visitCtReturn(returnStatement);
                controlList.add(returnStatement.toString());
            }

            @Override
            public void visitCtTry(CtTry tryBlock) {
                super.visitCtTry(tryBlock);
                blockList.add(tryBlock.toString());
            }

            @Override
            public void visitCtCatch(CtCatch catchBlock) {
                super.visitCtCatch(catchBlock);
                blockList.add(catchBlock.toString());
            }

            @Override
            public <R> void visitCtBlock(CtBlock<R> block) {
                super.visitCtBlock(block);
                blockList.add(block.toString());
            }

            @Override
            public void visitCtIf(CtIf ifElement) {
                super.visitCtIf(ifElement);
                conditionList.add(ifElement.toString());
            }

            @Override
            public <T> void visitCtConditional(CtConditional<T> conditional) {
                super.visitCtConditional(conditional);
                conditionList.add(conditional.toString());
            }
        };
        // todo: consider the difference between getSrcNode() or getDstNode(), or based on other methods
        scanner.scan(operation.getSrcNode());

        EnumSet<RepairFeature> repairFeatures = EnumSet.noneOf(RepairFeature.class);
        if (operation instanceof InsertOperation) {
            if (controlList.size() > 0) {
                repairFeatures.add(RepairFeature.INSERT_CONTROL_RF);
            } else if (blockList.size() > 0) {
                repairFeatures.add(RepairFeature.INSERT_GUARD_RF);
            } else {
                repairFeatures.add(RepairFeature.INSERT_STMT_RF);
            }
        } else if (operation instanceof UpdateOperation) {
            if (conditionList.size() > 0) {
                repairFeatures.add(RepairFeature.REPLACE_COND_RF);
            } else {
                repairFeatures.add(RepairFeature.REPLACE_STMT_RF);
            }
        } else if (operation instanceof DeleteOperation) {
//            repairFeatures.add(RepairFeature.DELETE_STMT_RF);
        } else {
            // move or some unknown cases
//            repairFeatures.add(RepairFeature.UNKNOWN_STMT_RF);
        }

//        System.out.println("----getRepairTypes----");
//        System.out.println(repairFeatures);
        return repairFeatures;
    }

    private EnumSet<ValueFeature> getValueKinds(AtomicFeature atomicFeature, CtElement srcStmt, CtElement dstStmt) {
        EnumSet<ValueFeature> valueFeatures = EnumSet.noneOf(ValueFeature.class);

        if (srcStmt != null && dstStmt != null) {
            if (getAtomicKinds(null, dstStmt, false).contains(atomicFeature)) {
                valueFeatures.add(ValueFeature.MODIFIED_VF);
            }
            String srcStr = srcStmt.toString();
            String dstStr = dstStmt.toString();
            double srcStrLen = srcStr.length();
            double dstStrLen = dstStr.length();
            if (srcStr.length() > 3 && dstStr.length() > 3) {
                if (2 * srcStrLen > dstStrLen && 2 * dstStrLen > srcStrLen) {
                    if (srcStr.contains(dstStr) || dstStr.contains(srcStr)) {
                        valueFeatures.add(ValueFeature.MODIFIED_SIMILAR_VF);
                    }
                }
            }
        }

        List<String> variableList = new ArrayList<>();
        CtScanner scanner = new CtScanner() {
            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                super.visitCtInvocation(invocation);
                valueFeatures.add(ValueFeature.FUNC_ARGUMENT_VF);
            }

            @Override
            public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
                super.visitCtFieldRead(fieldRead);
                valueFeatures.add(ValueFeature.MEMBER_VF);
            }

            @Override
            public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
                super.visitCtFieldWrite(fieldWrite);
                valueFeatures.add(ValueFeature.MEMBER_VF);
            }

            @Override
            public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
                super.visitCtVariableRead(variableRead);
                variableList.add(variableRead.toString());
            }

            @Override
            public <T> void visitCtVariableWrite(CtVariableWrite<T> variableWrite) {
                super.visitCtVariableWrite(variableWrite);
                variableList.add(variableWrite.toString());
            }

            @Override
            public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
                super.visitCtLocalVariable(localVariable);
                variableList.remove(localVariable.toString());
                valueFeatures.add(ValueFeature.LOCAL_VARIABLE_VF);
            }

            @Override
            public <T> void visitCtLiteral(CtLiteral<T> ctLiteral) {
                super.visitCtLiteral(ctLiteral);
                if (ctLiteral.getValue() instanceof Integer) {
                    if ((Integer) ctLiteral.getValue() == 0) {
                        valueFeatures.add(ValueFeature.ZERO_CONST_VF);
                    } else {
                        valueFeatures.add(ValueFeature.NONZERO_CONST_VF);
                    }
                } else if (ctLiteral.getValue() instanceof String) {
                    valueFeatures.add(ValueFeature.STRING_LITERAL_VF);
                }
            }
        };
        scanner.scan(dstStmt);
        if (variableList.size() > 0) {
            valueFeatures.add(ValueFeature.GLOBAL_VARIABLE_VF);
        }

        if (dstStmt != null) {
            if (dstStmt.toString().contains("size")) {
                valueFeatures.add(ValueFeature.SIZE_LITERAL_VF);
            }
        }
        // todo: one special case of string const replacement (one idea is to use contains function)
//        if (isAbstractStub(E)) {
//            assert(rc.kind == Repair::ReplaceStringKind);
//            ret.insert(ModifiedVF);
//            ret.insert(StringLiteralVF);
//        }

//        System.out.println("----getValueKinds----");
//        System.out.println(valueFeatures);
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

    // intended for FeatureExtractorTest.java
    public FeatureManager easyExtractor(File file0, File file1) throws Exception {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(file0, file1);
        CtElement srcRoot = comparator.getCtType(file0).getParent();
        CtElement dstRoot = comparator.getCtType(file1).getParent();
        return extractFeature(diff, srcRoot, dstRoot);
    }

    // this is for FeatureExtractorTest.java
    public FeatureManager easyExtractor(String str0, String str1) {
        AstComparator comparator = new AstComparator();
        Diff diff = comparator.compare(str0, str1);
        CtElement srcRoot = comparator.getCtType(str0).getParent();
        CtElement dstRoot = comparator.getCtType(str1).getParent();
        return extractFeature(diff, srcRoot, dstRoot);
    }

    // this is for FeatureExtractorTest.java
    private FeatureManager extractFeature(Diff diff, CtElement srcRoot, CtElement dstRoot) {
        CtElement ancestor = diff.commonAncestor();
        if (ancestor instanceof CtExpression) {
            while (!(ancestor instanceof CtStatement)){
                ancestor = ancestor.getParent();
            }
        }
        // p & p' in Feature Extraction Algorithm
        // we have to handle the CtPath here because evaluateOn() would be invalid when it meets #subPackage
        CtPath ancestorPath = ancestor.getPath();
        String ancestorPathString = ancestorPath.toString();
        ancestorPathString = ancestorPathString.substring(ancestorPathString.indexOf("#containedType"));
        ancestorPath = new CtPathStringBuilder().fromString(ancestorPathString);
        List<CtElement> srcStmtList = new ArrayList<>(ancestorPath.evaluateOn(srcRoot));
        assert srcStmtList.size() == 1;
        CtElement srcAncestor = srcStmtList.get(0);
        srcStmtList = getStmtList(srcAncestor);
        List<CtElement> dstStmtList = new ArrayList<>(ancestorPath.evaluateOn(dstRoot));
        assert dstStmtList.size() == 1;
        CtElement dstAncestor = dstStmtList.get(0);
        dstStmtList = getStmtList(dstAncestor);
        int pivot = getPivot(srcStmtList, dstStmtList);
//        System.out.println(pivot);
//        System.out.println(srcStmtList);
//        System.out.println(dstStmtList);
//        System.out.println(diff.getRootOperations());

        FeatureManager featureManager = new FeatureManager();
        for (Operation operation : diff.getRootOperations()) {
            // RepairFeatureNum     = RepairFeatureNum                      = 5
            EnumSet<RepairFeature> repairKinds = getRepairTypes(operation);
            // ModKind should be synonyms of RepairType
            for (FeatureType repairType : repairKinds) {
                // RF_JT
                List<FeatureType> repairFeatures = new ArrayList<>();
                repairFeatures.add(repairType);
                featureManager.addFeature(new Feature(JointType.RF_JT, repairFeatures));
            }
            // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 450
//            System.out.println("GlobalFeature");
            for (int index = Math.max(0, pivot - 3); index < Math.min(pivot + 4, srcStmtList.size()); index++) {
                // s in Feature Extraction Algorithm
                CtElement focusedStmt = srcStmtList.get(index);
                FeatureType position = index < pivot ? Position.POS_P : (index > pivot ? Position.POS_N : Position.POS_C);
                // StmtKind should be one subset of AtomicFeature
                for (FeatureType atomicKind : getAtomicKinds(null, focusedStmt, operation instanceof UpdateOperation)) {
                    for (FeatureType repairType : repairKinds) {
                        // POS_AF_RF_JT
                        List<FeatureType> globalFeatures = new ArrayList<>();
                        globalFeatures.add(position);
                        globalFeatures.add(atomicKind);
                        globalFeatures.add(repairType);
                        featureManager.addFeature(new Feature(JointType.POS_AF_RF_JT, globalFeatures));
                    }
                }
            }
            if (pivot < dstStmtList.size()) { // handle DELETE action
                // n in Feature Extraction Algorithm
                CtElement patchedStmt = dstStmtList.get(pivot);
                // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
//                System.out.println("VarCrossFeature + ValueCrossFeature");
                for (CtElement atom : getAtoms(patchedStmt)) {
                    EnumSet<AtomicFeature> dstAtomicFeatures = getAtomicKinds(atom, patchedStmt, operation instanceof UpdateOperation);
                    for (int index = Math.max(0, pivot - 3); index < Math.min(pivot + 4, srcStmtList.size()); index++) {
                        CtElement focusedStmt = srcStmtList.get(index);
                        EnumSet<AtomicFeature> srcAtomicFeatures = getAtomicKinds(atom, focusedStmt, operation instanceof UpdateOperation);
                        FeatureType position = index < pivot ? Position.POS_P : (index > pivot ? Position.POS_N : Position.POS_C);
                        for (FeatureType dstAtomicKind : dstAtomicFeatures) {
                            for (FeatureType srcAtomicKind : srcAtomicFeatures) {
                                // POS_AF_AF_JT
                                List<FeatureType> varCrossFeatures = new ArrayList<>();
                                varCrossFeatures.add(position);
                                varCrossFeatures.add(srcAtomicKind);
                                varCrossFeatures.add(dstAtomicKind);
                                featureManager.addFeature(new Feature(JointType.POS_AF_AF_JT, varCrossFeatures));
                            }
                        }
                    }
                    // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360
                    // this is not mentioned at prophet paper but implemented in prophet code
                    if (pivot < srcStmtList.size()) { // handle DELETE action
                        for (AtomicFeature atomicFeature : dstAtomicFeatures) {
                            for (FeatureType valueKind : getValueKinds(atomicFeature, srcStmtList.get(pivot), patchedStmt)) {
                                // AF_VF_JT
                                List<FeatureType> valueCrossFeature = new ArrayList<>();
                                valueCrossFeature.add(atomicFeature);
                                valueCrossFeature.add(valueKind);
                                featureManager.addFeature(new Feature(JointType.AF_VF_JT, valueCrossFeature));
                            }
                        }
                    }
                }
            }
        }
        return featureManager;
    }

//    // TODO: ~
//    // for features related to ProgramVariant & ModificationPoint
//    public Context<?> retrieveContext(ModificationPoint modificationPoint) {
//        return retrieveContext(modificationPoint.getCodeElement());
//    }
//
//    public Context<?> retrievePatchContext(CtElement element) {
//        Context<Object> patchcontext = new Context<>(determineKey(element));
//
//        patchcontext.put(FeatureType.PATCH_CODE_ELEMENT, element.toString());
//
//        CtElement stmt = element.getParent(CtStatement.class);
//        if (stmt == null)
//            stmt = element.getParent(CtMethod.class);
//        patchcontext.put(FeatureType.PATCH_CODE_STATEMENT, (stmt != null) ? element.toString() : null);
//
//        retrieveType(element, patchcontext);
//        retrievePath(element, patchcontext);
//
//        return patchcontext;
//    }
//
//    private void retrieveType(CtElement element, Context<Object> context) {
//        context.put(FeatureType.TYPE, element.getClass().getSimpleName());
//    }
//
//    private void retrievePath(CtElement element, Context<Object> context) {
//        try {
//            CtPath path = element.getPath();
//
//            context.put(FeatureType.SPOON_PATH, path.toString());
//            if (path instanceof CtPathImpl) {
//                CtPathImpl pi = (CtPathImpl) path;
//                List<CtPathElement> elements = pi.getElements();
//                List<String> paths = elements.stream().map(e -> e.toString()).collect(Collectors.toList());
//                context.put(FeatureType.PATH_ELEMENTS, paths);
//            }
//        } catch (Throwable e) {
//        }
//
//    }
//
//    private boolean isElementBeforeVariable(CtVariableAccess variableAffected, CtElement element) {
//
//        try {
//            CtStatement stst = (element instanceof CtStatement) ? (CtStatement) element
//                    : element.getParent(CtStatement.class);
//
//            CtStatement target = (variableAffected instanceof CtStatement) ? (CtStatement) variableAffected
//                    : variableAffected.getParent(CtStatement.class);
//
//            return target.getPosition() != null && stst.getParent() != null
//                    && target.getPosition().getSourceStart() > stst.getPosition().getSourceStart();
//        } catch (Exception e) {
//            // e.printStackTrace();
//        }
//        return false;
//
//    }
}