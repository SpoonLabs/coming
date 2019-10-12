package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Cardumen fixes bugs by replacing expressions with template-based generated expressions.
 * The templates of the generated expressions are extracted from the same file/package/project by replacing variables with
 * placeholders.
 * <p>
 * expression_replacement.xml pattern only collects the changes that update an expression.
 * <p>
 * The filter functions should determine whether the new expression is an instance of a template from the same file.
 */
public class Cardumen extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "expression_replacement.xml"
    };

    /**
     * Encodes the search space of JMutRepair
     *
     * @return a List of ChangePatternSpecifications that are supposed to be mined by PatternInstanceAnalyzer
     */
    @Override
    protected List<ChangePatternSpecification> readPatterns() {
        List<ChangePatternSpecification> patterns = new ArrayList<>();
        for (String fileName : patternFileNames) {
            patterns.add(PatternXMLParser.parseFile(getPathFromResources(fileName)));
        }
        return patterns;
    }

    /**
     * Certain patterns/characteristics of search-space of a repair tool can't be represented by ChangePatternSpecification
     * This filter is supposed to delete/remove such instances from the results given by PatternInstanceAnalyser.
     * <p>
     * The filter functions should determine whether the new expression is an instance of a template from the same file.
     *
     * @param instance
     * @return
     */
    @Override
    public boolean filter(ChangePatternInstance instance, IRevision revision) {
        try {
            Operation anyOperation = instance.getActions().get(0);
            CtElement srcNode = anyOperation.getSrcNode(), dstNode = anyOperation.getDstNode();
            List<CtElement> allDstElements = dstNode.getElements(null);

            CtElement srcRootNode = getRootNode(srcNode);
            List<CtElement> allSrcElements = srcRootNode.getElements(null);

            for (int i = 0; i < allSrcElements.size() - allDstElements.size() + 1; i++) {
                boolean haveSameTemplate = true;
                for (int j = 0; j < allDstElements.size(); j++) {
                    CtElement srcElement = allSrcElements.get(i + j);
                    CtElement dstElement = allDstElements.get(j);
                    if (!srcElement.getClass().getSimpleName().equals(dstElement.getClass().getSimpleName())) {
                        haveSameTemplate = false;
                        break;
                    }
                    if (srcElement instanceof CtVariable) {
                        if (!((CtVariable) srcElement).getSimpleName().equals(((CtVariable) dstElement).getSimpleName())) {
                            haveSameTemplate = false;
                            break;
                        }
                    }
                }
                if (haveSameTemplate)
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private CtElement getRootNode(CtElement element) {
        CtElement par = element.getParent();
        if (par == null || element == par)
            return element;
        return getRootNode(element);
    }

}