package fr.inria.coming.codefeatures;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import fr.inria.coming.codefeatures.codeanalyze.AbstractCodeAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.BinaryOperatorAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.ConstantAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.ConstructorAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.LogicalExpressionAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.MethodAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.TypeaccessAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.VariableAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.WholeStatementAnalyzer;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

// appended the default method
public class CodeFeatureDetector {

    public Cntx<?> analyzeFeatures(CtElement element, List<CtExpression> allExpressions,
                                   List<CtExpression> allrootlogicalexpers, List<CtBinaryOperator> allBinOperators) {

        CodeElementInfo infoElementStudy = new CodeElementInfo(element, allExpressions, allrootlogicalexpers, allBinOperators);

        List<AbstractCodeAnalyzer> analyzers = new ArrayList<>();

        analyzers.add(new VariableAnalyzer(infoElementStudy));

        analyzers.add(new BinaryOperatorAnalyzer(infoElementStudy));

        analyzers.add(new ConstantAnalyzer(infoElementStudy));

        analyzers.add(new ConstructorAnalyzer(infoElementStudy));

        //	analyzers.add(new ExpressionAnalyzer(infoElementStudy));

        analyzers.add(new LogicalExpressionAnalyzer(infoElementStudy));

        analyzers.add(new TypeaccessAnalyzer(infoElementStudy));

        analyzers.add(new MethodAnalyzer(infoElementStudy));

        analyzers.add(new WholeStatementAnalyzer(infoElementStudy));

        for (int index = 0; index < analyzers.size(); index++) {
            analyzers.get(index).analyze();
        }

        return infoElementStudy.context;
    }

    // default method
    public Cntx<?> analyzeFeatures(CtElement element) {

        CtClass parentClass = element instanceof CtClass ? (CtClass) element : element.getParent(CtClass.class);

        CtElement elementToStudy = retrieveElementToStudy(element);

        List<CtExpression> expressionssFromFaultyLine = elementToStudy.getElements(e -> (e instanceof CtExpression)).stream()
            .map(CtExpression.class::cast).collect(Collectors.toList());

        LinkedHashSet<CtExpression> hashSetExpressions = new LinkedHashSet<>(expressionssFromFaultyLine);
        ArrayList<CtExpression> listExpressionWithoutDuplicates = new ArrayList<>(hashSetExpressions);

        ArrayList<CtExpression> removeUndesirable = new ArrayList<>();

        for (int index = 0; index < listExpressionWithoutDuplicates.size(); index++) {

            CtExpression certainExpression = listExpressionWithoutDuplicates.get(index);

            if (certainExpression instanceof CtVariableAccess || certainExpression instanceof CtLiteral ||
                certainExpression instanceof CtInvocation || certainExpression instanceof CtConstructorCall ||
                certainExpression instanceof CtArrayRead || analyzeWhetherAE(certainExpression))
                removeUndesirable.add(certainExpression);
        }

        List<CtExpression> logicalExpressions = new ArrayList();
        for (int index = 0; index < expressionssFromFaultyLine.size(); index++) {

            if (isBooleanExpressionNew(expressionssFromFaultyLine.get(index)) &&
                !whetherparentboolean(expressionssFromFaultyLine.get(index)) &&
                !logicalExpressions.contains(expressionssFromFaultyLine.get(index))) {
                logicalExpressions.add(expressionssFromFaultyLine.get(index));
            }
        }

        List<CtBinaryOperator> allBinOperators = parentClass.getElements(e ->
            (e instanceof CtBinaryOperator)).stream()
            .map(CtBinaryOperator.class::cast).collect(Collectors.toList());

        // element, desirableExpressions, logicalExpressions, binoperators
        return analyzeFeatures(element, removeUndesirable, logicalExpressions, allBinOperators);
    }

    // supporting methods
    private boolean analyzeWhetherAE(CtExpression expression) {

        try {

            List<BinaryOperatorKind> opKinds = new ArrayList<>();
            opKinds.add(BinaryOperatorKind.DIV);
            opKinds.add(BinaryOperatorKind.PLUS);
            opKinds.add(BinaryOperatorKind.MINUS);
            opKinds.add(BinaryOperatorKind.MUL);
            opKinds.add(BinaryOperatorKind.MOD);

            if (expression instanceof CtBinaryOperator && opKinds.contains(((CtBinaryOperator) expression).getKind()))
                return true;

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isLogicalExpressionNew(CtElement currentElement) {
        if (currentElement == null)
            return false;

        if ((currentElement instanceof CtBinaryOperator)) {

            CtBinaryOperator binOp = (CtBinaryOperator) currentElement;

            if (binOp.getKind().equals(BinaryOperatorKind.AND) || binOp.getKind().equals(BinaryOperatorKind.OR)
                || binOp.getKind().equals(BinaryOperatorKind.EQ)
                || binOp.getKind().equals(BinaryOperatorKind.GE)
                || binOp.getKind().equals(BinaryOperatorKind.GT)
                || binOp.getKind().equals(BinaryOperatorKind.INSTANCEOF)
                || binOp.getKind().equals(BinaryOperatorKind.LE)
                || binOp.getKind().equals(BinaryOperatorKind.LT)
                || binOp.getKind().equals(BinaryOperatorKind.NE)
                || (binOp.getType() != null &&
                binOp.getType().unbox().getSimpleName().equals("boolean")))

                return true;
        }

        if (currentElement.getParent() instanceof CtConditional) {
            CtConditional cond = (CtConditional) currentElement.getParent();
            if (currentElement.equals(cond.getCondition()))
                return true;
        }

        if (currentElement.getParent() instanceof CtIf) {
            CtIf ifcond = (CtIf) currentElement.getParent();
            if (currentElement.equals(ifcond.getCondition()))
                return true;
        }

        if (currentElement.getParent() instanceof CtWhile) {
            CtWhile whilecond = (CtWhile) currentElement.getParent();
            if (currentElement.equals(whilecond.getLoopingExpression()))
                return true;
        }

        if (currentElement.getParent() instanceof CtDo) {
            CtDo docond = (CtDo) currentElement.getParent();
            if (currentElement.equals(docond.getLoopingExpression()))
                return true;
        }

        if (currentElement.getParent() instanceof CtFor) {
            CtFor forcond = (CtFor) currentElement.getParent();
            if (currentElement.equals(forcond.getExpression()))
                return true;
        }

        return false;
    }

    public boolean isBooleanExpressionNew(CtElement currentexpression) {

        if (currentexpression == null)
            return false;

        if (isLogicalExpressionNew(currentexpression)) {
            return true;
        }

        if (currentexpression instanceof CtExpression) {
            CtExpression exper = (CtExpression) currentexpression;
            try {
                if (exper.getType() != null
                    && exper.getType().unbox().toString().equals("boolean")) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public boolean whetherparentboolean(CtExpression tostudy) {

        CtElement parent = tostudy;
        while (parent != null) {
            parent = parent.getParent();

            if (isBooleanExpressionNew(parent))
                return true;
        }

        return false;
    }

    public CtElement retrieveElementToStudy(CtElement element) {

        if (element instanceof CtIf) {
            return (((CtIf) element).getCondition());
        } else if (element instanceof CtWhile) {
            return (((CtWhile) element).getLoopingExpression());
        } else if (element instanceof CtFor) {
            return (((CtFor) element).getExpression());
        } else if (element instanceof CtDo) {
            return (((CtDo) element).getLoopingExpression());
//		} else if (element instanceof CtConditional) {
//			return (((CtConditional) element).getCondition());
        } else if (element instanceof CtForEach) {
            return (((CtForEach) element).getExpression());
        } else if (element instanceof CtSwitch) {
            return (((CtSwitch) element).getSelector());
        } else
            return (element);

    }

}
