package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

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
        try { // FIXME: should not throw an exception
            Operation anyOperation = instance.getActions().get(0);
            CtElement srcNode = anyOperation.getSrcNode(), dstNode = anyOperation.getDstNode();

            CtElement srcRootNode = getPathToRootNode(srcNode).get(0);
            List<CtElement> allSrcElements = srcRootNode.getElements(null);
            Set<String> srcVariablesAndLiterals = new HashSet<>();

            for (int i = 0; i < allSrcElements.size(); i++) {
                CtElement srcElement = allSrcElements.get(i);
                if (srcElement instanceof CtVariableRead || srcElement instanceof CtLiteral) {
                    srcVariablesAndLiterals.add(srcElement.toString());
                }
            }

            List<CtElement> allDstElements = dstNode.getElements(null);
            String dstNodeAsString = dstNode.toString();
            // the following for-loop replaces variable names/literals in dstNodeAsString with their type name
            for (int i = 0; i < allDstElements.size(); i++) {
                CtElement dstElement = allDstElements.get(i);
                if (dstElement instanceof CtVariableRead || dstElement instanceof CtLiteral) {
                    if (!srcVariablesAndLiterals.contains(dstElement.toString()))
                        // A variable/literal is used that does not exist in SRC is used in the patch
                        // FIXME: We should also make sure than the variable/literal is in the current scope
                        return false;
                    String variableOrLiteralType = getType(dstElement);
                    dstNodeAsString = dstNodeAsString.replace(dstElement.toString(), "#" + variableOrLiteralType + "#");
                }
            }

            for (int i = 0; i < allSrcElements.size() - allDstElements.size() + 1; i++) {
                String srcAsString = allSrcElements.get(i).toString();
                if (srcAsString.contains("Precision.equals"))
                    System.out.println();
                for (int j = 0; j < allDstElements.size(); j++) {
                    CtElement srcElement = allSrcElements.get(i + j);
                    if(srcElement instanceof CtLiteral || srcElement instanceof CtVariableRead) {
                        String variableOrLiteralType = getType(srcElement);
                        srcAsString = srcAsString.replace(srcElement.toString(), "#" + variableOrLiteralType + "#");
                    }
                }
                if (srcAsString.contains(dstNodeAsString))
                    // the template of the dst-node is found in the src
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String getType(CtElement element) {
        String variableOrLiteralType = null;
        if (element instanceof CtVariableRead) {
            variableOrLiteralType = ((CtVariableRead) element).getType().toString();
        } else if (element instanceof CtLiteral) {
            variableOrLiteralType = ((CtLiteral) element).getType().toString();
        }
        return variableOrLiteralType;
    }

    @Override
    public boolean coversTheWholeDiff(ChangePatternInstance instancePattern, Diff diff) {
        CtElement instanceSrcNode = instancePattern.getActions().get(0).getSrcNode();
        for (Operation diffOperation : diff.getRootOperations()) {
            boolean found = false;
            List<CtElement> pathToDiffRoot = getPathToRootNode(diffOperation.getSrcNode());
            for (CtElement item : pathToDiffRoot) {
                if (item == instanceSrcNode)
                    found = true;
            }
            if (!found)
                return false;
        }
        return true;
    }

    private List<CtElement> getPathToRootNode(CtElement element) {
        CtElement par = element.getParent();
        if (par == null || par instanceof CtPackage || element == par) {
            List<CtElement> res = new ArrayList<>();
            res.add(element);
            return res;
        }
        List<CtElement> pathToParent = getPathToRootNode(par);
        pathToParent.add(element);
        return pathToParent;
    }

}