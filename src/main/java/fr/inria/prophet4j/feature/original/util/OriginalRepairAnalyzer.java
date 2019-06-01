package fr.inria.prophet4j.feature.original.util;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Type;
import java.util.*;

// based on LocalAnalyzer.cpp GlobalAnalyzer.cpp
public class OriginalRepairAnalyzer {
    public List<CtElement> getCondCandidateVars(CtElement element) {
        List<CtElement> ret = new ArrayList<>();
        // Global variables
        CtClass ownedClass = element.getParent(new TypeFilter<>(CtClass.class));
        if (ownedClass != null) {
            ret.addAll(ownedClass.getElements(new TypeFilter<>(CtVariableAccess.class)));
            ret.addAll(ownedClass.getElements(new TypeFilter<>(CtArrayAccess.class)));
        }
        // Local variables
        CtMethod ownedMethod = element.getParent(new TypeFilter<>(CtMethod.class));
        if (ownedMethod != null) {
            ret.addAll(ownedMethod.getElements(new TypeFilter<>(CtLocalVariable.class)));
        }
        return ret;
    }

    public boolean isInsideLoop(CtElement element) {
        return element.getParent(new TypeFilter<>(CtLoop.class)) != null;
    }

    public CtMethod getCurrentFunction(CtElement element) {
        return element.getParent(new TypeFilter<>(CtMethod.class));
    }

    public List<CtElement> getCandidateConstantInType(CtElement element, Type type) {
        List<CtElement> ret = new ArrayList<>();
        CtClass ownedClass = element.getParent(new TypeFilter<>(CtClass.class));
        if (ownedClass != null) {
            for (CtLiteral tmp : ownedClass.getElements(new TypeFilter<>(CtLiteral.class))) {
                if (tmp.getClass().getGenericSuperclass() == type) {
                    ret.add(tmp);
                }
            }
            for (CtVariableAccess tmp : ownedClass.getElements(new TypeFilter<>(CtVariableAccess.class))) {
                if (tmp.getClass().getGenericSuperclass() == type) {
                    ret.add(tmp);
                }
            }
        }
        return ret;
    }

    public List<CtExpression> getCandidateReturnExpr(CtElement element, Type type) {
        List<CtExpression> ret = new ArrayList<>();
        CtClass ownedClass = element.getParent(new TypeFilter<>(CtClass.class));
        if (ownedClass != null) {
            for (CtLiteral tmp : ownedClass.getElements(new TypeFilter<>(CtLiteral.class))) {
                if (tmp.getClass().getGenericSuperclass() == type) {
                    ret.add(tmp);
                }
            }
            for (CtVariableAccess tmp : ownedClass.getElements(new TypeFilter<>(CtVariableAccess.class))) {
                if (tmp.getClass().getGenericSuperclass() == type) {
                    ret.add(tmp);
                }
            }
        }
        return ret;
    }

    public List<CtElement> getCandidateLocalVars(CtElement element, Type type) {
        List<CtElement> ret = new ArrayList<>();
        CtMethod ownedMethod = element.getParent(new TypeFilter<>(CtMethod.class));
        if (ownedMethod != null) {
            for (CtLocalVariable tmp : ownedMethod.getElements(new TypeFilter<>(CtLocalVariable.class))) {
                if (tmp.getClass().getGenericSuperclass() == type) {
                    ret.add(tmp);
                }
            }
        }
        return ret;
    }

    public Set<CtElement> getGlobalCandidateExprs(CtElement element) {
        Set<CtElement> ret = new HashSet<>();
        CtClass ownedClass = element.getParent(new TypeFilter<>(CtClass.class));
        if (ownedClass != null) {
            ret.addAll(ownedClass.getElements(new TypeFilter<>(CtExpression.class)));
        }
        return ret;
    }

    public Set<CtElement> getGlobalCandidateIfStmts(CtElement element) {
        Set<CtElement> ret = new HashSet<>();
        CtClass ownedClass = element.getParent(new TypeFilter<>(CtClass.class));
        if (ownedClass != null) {
            ret.addAll(ownedClass.getElements(new TypeFilter<>(CtIf.class)));
        }
        return ret;
    }

    public AtomReplaceVisitor newAtomReplaceVisitor() {
        return new AtomReplaceVisitor();
    }

    public class AtomReplaceVisitor { // this class could be reduced as one method
        Set<CtElement> res = new HashSet<>();
        Map<CtElement, HashMap.SimpleEntry<CtElement, CtElement>> resRExpr = new HashMap<>();

        // we implement one equivalent method instead of CtScanner
        public void TraverseStmt(CtElement element) {
            // PR spoon to support getting belonged CtEnum with one CtEnumValue
//            List<CtEnumValue> enumValues = element.getElements(new TypeFilter<>(CtEnumValue.class));
//            for (CtEnumValue enumValue : enumValues) {
//                List<CtElement> exprs = L->getCandidateEnumConstant(enumValue);
//                for (CtElement expr : exprs) {
//                    res.add(expr);
//                    resRExpr.put(expr, new HashMap.SimpleEntry<>(enumValue, expr));
//                }
//            }
            List<CtBinaryOperator> binaryOperators = element.getElements(new TypeFilter<>(CtBinaryOperator.class));
            for (CtBinaryOperator binaryOperator : binaryOperators) {
                CtExpression RHS = binaryOperator.getRightHandOperand();
                if (RHS instanceof CtLiteral || RHS instanceof CtVariableAccess) {
                    if (RHS.getClass().getGenericSuperclass().equals(Integer.class)) {
                        List<CtElement> exprs = getCandidateConstantInType(element, Integer.class);
                        for (CtElement expr : exprs) {
                            res.add(expr);
                            resRExpr.put(expr, new HashMap.SimpleEntry<>(RHS, expr));
                        }
                    }
                } else if (RHS instanceof CtLocalVariable) {
                    List<CtElement> exprs = getCandidateLocalVars(element, RHS.getClass().getGenericSuperclass());
                    for (CtElement expr : exprs) {
                        res.add(expr);
                        resRExpr.put(expr, new HashMap.SimpleEntry<>(RHS, expr));
                    }
                }
            }
        }

        public Set<CtElement> getResult() {
            return res;
        }

        public CtElement getOldRExpr(CtElement S) {
            return resRExpr.get(S).getKey();
        }

        public CtElement getNewRExpr(CtElement S) {
            return resRExpr.get(S).getValue();
        }
    }

    public List<CtInvocation> getCandidateCalleeFunction(CtInvocation CE) {
        List<CtInvocation> ret = new ArrayList<>();

        CtMethod ownedMethod = CE.getParent(new TypeFilter<>(CtMethod.class));
        if (ownedMethod != null) {
            List<CtInvocation> invocations = ownedMethod.getElements(new TypeFilter<>(CtInvocation.class));
            for (CtInvocation invocation : invocations) {
                if (CE == invocation) {
                    continue;
                }
                if (CE.getExecutable() != invocation.getExecutable()) {
                    continue;
                }
                if (CE.getArguments().size() != invocation.getArguments().size()) {
                    continue;
                }
                ret.add(invocation);
            }
        }
        return ret;
    }
}
