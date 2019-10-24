package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.utils.GumtreeHelper;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;

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
        Operation anyOperation = instance.getActions().get(0);
        CtElement srcNode = anyOperation.getSrcNode(), dstNode = anyOperation.getDstNode();

        CtElement srcRootNode = GumtreeHelper.getPathToRootNode(srcNode).get(0);
        List<CtElement> allSrcElements = srcRootNode.getElements(null);
        Set<String> srcVariablesAndLiterals = new HashSet<>();

        for (int i = 0; i < allSrcElements.size(); i++) {
            CtElement srcElement = allSrcElements.get(i);
            if (srcElement instanceof CtVariableAccess || srcElement instanceof CtLiteral) {
                srcVariablesAndLiterals.add(cleanedName(srcElement));
            }
        }

        List<CtElement> allDstElements = dstNode.getElements(null);
        String dstNodeAsString = dstNode.toString();
        // the following for-loop replaces variable names/literals in dstNodeAsString with their type name
        for (int i = 0; i < allDstElements.size(); i++) {
            CtElement dstElement = allDstElements.get(i);
            if (dstElement instanceof CtVariableAccess || dstElement instanceof CtLiteral) {
                if (!srcVariablesAndLiterals.contains(cleanedName(dstElement)))
                    // A variable/literal is used that does not exist in SRC
                    // FIXME: We should also make sure than the variable/literal is in the current scope
                    return false;
                String variableOrLiteralType = getType(dstElement);
                dstNodeAsString = replaceElement(dstNodeAsString, dstElement.toString(),
                        "#" + variableOrLiteralType + "#");
            }
        }

        for (int i = 0; i < allSrcElements.size() - allDstElements.size() + 1; i++) {
            CtElement currentSrcElement = allSrcElements.get(i);
            String typeOfCurrentSrcElement = GumtreeHelper.getNodeLabelFromCtElement(currentSrcElement);
            if (!GumtreeHelper.getInstance().isAChildOf(typeOfCurrentSrcElement, "Expression")) {
                continue;
            }
            String srcAsString = currentSrcElement.toString();
            Set<CtElement> elementsInSubtree = new HashSet<>();
            elementsInSubtree.add(currentSrcElement);
            for (int j = 0; j == 0 ||
                    (i + j < allSrcElements.size() && elementsInSubtree.contains(allSrcElements.get(i + j).getParent()))
                    ; j++) {
                CtElement srcElement = allSrcElements.get(i + j);
                elementsInSubtree.add(srcElement);
                if (srcElement instanceof CtLiteral || srcElement instanceof CtVariableAccess) {
                    String variableOrLiteralType = getType(srcElement);
                    srcAsString = replaceElement(srcAsString, srcElement.toString(),
                            "#" + variableOrLiteralType + "#");
                }
            }
            if (areTheSameTemplates(srcAsString, dstNodeAsString))
                // the template of the dst-node is found in the src
                return true;
        }
        return false;
    }

    private String replaceElement(String source, String element, String target) {
        int fromInd = 0;
        while (source.indexOf(element, fromInd) > -1) {
            int ind = source.indexOf(element, fromInd);
            if (!((ind > 0 && isVariableNameChar(source.charAt(ind - 1)))
                    || (ind + element.length() < source.length() &&
                    isVariableNameChar(source.charAt(ind + element.length()))))) {
                // the chars before and after the element are not a variable-name-char
                source = source.substring(0, ind) + target + (ind + element.length() >= source.length() ? "" :
                        source.substring(ind + element.length()));
                fromInd = ind + target.length();
                if (fromInd >= source.length())
                    break;
                continue;
            }
            fromInd = ind + element.length();
            if (fromInd >= source.length())
                break;
        }
        return source;
    }

    private boolean isVariableNameChar(char c) {
        return (c <= 'z' && c >= 'a') || (c <= 'Z' && c >= 'A') || (c <= '9' && c >= '0');
    }

    private boolean areTheSameTemplates(String temp1, String temp2) {
        temp1 = cleanedName(temp1);
        temp2 = cleanedName(temp2);
        String[] parts1 = temp1.split("#");
        String[] parts2 = temp2.split("#");
        if (parts1.length != parts2.length)
            return false;
        for (int i = 0; i < parts1.length; i++) {
            if (!parts1[i].equals(parts2[i]) && !parts1[i].equals("<nulltype>") && !parts2[i].equals("<nulltype>")
                    && !parts1[i].equals("null") && !parts2[i].equals("null"))
                return false;
        }
        return true;
    }

    private String cleanedName(CtElement element) {
        String elementName = element.toString();
        return cleanedName(elementName);
    }

    private String cleanedName(String elementName) {
        while (elementName.startsWith("(") && elementName.endsWith(")")) {
            elementName = elementName.substring(1, elementName.length() - 1);
        }
        if (elementName.startsWith("this.")) {
            elementName = elementName.substring("this.".length());
        }
        return elementName;
    }

    private String getType(CtElement element) {
        CtTypeReference type = ((CtTypedElement) element).getType();
        if (type == null)
            return "<nulltype>";
        return type.toString();
    }

    @Override
    public boolean coversTheWholeDiff(ChangePatternInstance instancePattern, Diff diff) {
        CtElement instanceSrcNode = instancePattern.getActions().get(0).getSrcNode();
        for (Operation diffOperation : diff.getRootOperations()) {
            boolean found = false;
            List<CtElement> pathToDiffRoot = GumtreeHelper.getPathToRootNode(diffOperation.getSrcNode());
            for (CtElement item : pathToDiffRoot) {
                if (item == instanceSrcNode)
                    found = true;
            }
            if (!found)
                return false;
        }
        return true;
    }

}