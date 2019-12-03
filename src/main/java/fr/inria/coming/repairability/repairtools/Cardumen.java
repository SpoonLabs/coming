package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.ASTInfoResolver;
import fr.inria.coming.utils.CtEntityType;
import fr.inria.coming.utils.EntityTypesInfoResolver;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cardumen fixes bugs by replacing expressions with template-based generated expressions.
 * The templates of the generated expressions are extracted from the same file/package/project by replacing variables with
 * placeholders.
 * <p>
 * expression_replacement_by_upd.xml pattern collects the changes that update an expression.
 * expression_replacement_by_del_ins.xml pattern collects the changes that delete an expression and then insert a new expression.
 * expression_replacement_by_del_mov.xml pattern collects the changes that replace an expression with a part of it.
 * expression_insertion_deep.xml pattern collects the changes that update an expression by inserting an expression to it.
 * <p>
 * The filter functions should determine whether the new expression is an instance of a template from the same file.
 */
public class Cardumen extends AbstractRepairTool {
    private static final String UPD_PATTERN_NAME = "expression_replacement_by_upd";
    private static final String DEL_INS_PATTERN_NAME = "expression_replacement_by_del_ins";
    private static final String DEL_MOV_PATTERN_NAME = "expression_replacement_by_del_mov";

    private static final String[] patternFileNames = {
            UPD_PATTERN_NAME + ".xml",
            DEL_INS_PATTERN_NAME + ".xml",
            DEL_MOV_PATTERN_NAME + ".xml"
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
     * @param diff
     * @return
     */
    @Override
    public boolean filter(ChangePatternInstance instance, IRevision revision, Diff diff) {
        CtElement srcNode = null, dstNode = null;
        if (instance.getPattern().getName().contains(UPD_PATTERN_NAME)) {
            Operation anyOperation = instance.getActions().get(0);

            srcNode = anyOperation.getSrcNode();
            dstNode = anyOperation.getDstNode();

        } else if (instance.getPattern().getName().contains(DEL_INS_PATTERN_NAME)) {
            Operation delAction, insAction;
            delAction = getActionFromDelInstance(instance, "DEL");
            insAction = getActionFromDelInstance(instance, "INS");

            if (delAction.getSrcNode().getParent() != ((InsertOperation) insAction).getParent())
                return false;

            srcNode = delAction.getSrcNode();
            dstNode = insAction.getSrcNode();
        } else if (instance.getPattern().getName().contains(DEL_MOV_PATTERN_NAME)) {

            if(!ComingProperties.getPropertyBoolean("exclude_repair_patterns_not_covering_the_whole_diff"))
                // this pattern produces too many false positives when not-covering instances are not excluded.
                return false;

            srcNode = getActionFromDelInstance(instance, "DEL").getSrcNode();
            dstNode = getActionFromDelInstance(instance, "MOV").getSrcNode();

            return ASTInfoResolver.getPathToRootNode(dstNode).contains(srcNode);
        } else {
            return false;
        }

        return checkSrcIncludesDstTemplate(srcNode, dstNode);
    }

    private boolean checkSrcIncludesDstTemplate(CtElement srcNode, CtElement dstNode) {
        CtElement srcRootNode = ASTInfoResolver.getPathToRootNode(srcNode).get(0);
        List<CtElement> allSrcElements = srcRootNode.getElements(null);
        Set<String> srcVariablesAndLiterals = new HashSet<>();

        for (int i = 0; i < allSrcElements.size(); i++) {
            CtElement srcElement = allSrcElements.get(i);
            if (srcElement instanceof CtVariableAccess || srcElement instanceof CtLiteral) {
                srcVariablesAndLiterals.add(ASTInfoResolver.getCleanedName(srcElement));
            }
            if(srcElement instanceof CtField) {
            	srcVariablesAndLiterals.add(ASTInfoResolver.getCleanedName(((CtField)srcElement)
						.getSimpleName()));
            }
        }

        List<CtElement> allDstElements = dstNode.getElements(null);
        String dstNodeAsString = dstNode.toString();
        // the following for-loop replaces variable names/literals in dstNodeAsString with their type name
        for (int i = 0; i < allDstElements.size(); i++) {
            CtElement dstElement = allDstElements.get(i);
            if (dstElement instanceof CtVariableAccess || dstElement instanceof CtLiteral) {
                if (!srcVariablesAndLiterals.contains(ASTInfoResolver.getCleanedName(dstElement)))
                    // A variable/literal is used that does not exist in SRC
                    // FIXME: We should also make sure that the variable/literal is in the current scope
                    return false;
                String variableOrLiteralType = getType(dstElement);
                dstNodeAsString = replaceElement(dstNodeAsString, dstElement.toString(),
                        "#" + variableOrLiteralType + "#");
            }
        }

        for (int i = 0; i < allSrcElements.size() - allDstElements.size() + 1; i++) {
            CtElement currentSrcElement = allSrcElements.get(i);
            String typeOfCurrentSrcElement = EntityTypesInfoResolver.getNodeLabelFromCtElement(currentSrcElement);
            if (!EntityTypesInfoResolver.getInstance().isAChildOf(typeOfCurrentSrcElement, CtEntityType.EXPRESSION.toString())) {
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
        temp1 = ASTInfoResolver.getCleanedName(temp1);
        temp2 = ASTInfoResolver.getCleanedName(temp2);
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

    private String getType(CtElement element) {
        CtTypeReference type = ((CtTypedElement) element).getType();
        if (type == null)
            return "<nulltype>";
        return type.toString();
    }

    // DEL_MOV/INS might add instances that are already added by other patterns. They should be filtered.
    @Override
    public List<ChangePatternInstance> filterSelectedInstances(List<ChangePatternInstance> lst, Diff diff) {
        Map<ChangePatternInstance, Set> instanceToCoveredNodes = new HashMap<>();
        List<ChangePatternInstance> ret = new ArrayList<>();

        for (ChangePatternInstance instance : lst) {
            if (instance.getPattern().getName().contains(UPD_PATTERN_NAME)) {
                ret.add(instance);
                instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
            }
        }

        for (ChangePatternInstance instance : lst) {
            if (instance.getPattern().getName().contains(DEL_MOV_PATTERN_NAME)
                    || instance.getPattern().getName().contains(DEL_INS_PATTERN_NAME)) {
                List<CtElement> changedNodes = new ArrayList<>();
                changedNodes.add(getActionFromDelInstance(instance, "DEL").getSrcNode());
                updateSelectedInstances(instanceToCoveredNodes, ret, instance, changedNodes, diff);
            }
        }

        return ret;
    }

    private void updateSelectedInstances
            (
                    Map<ChangePatternInstance, Set> instanceToCoveredNodes,
                    List<ChangePatternInstance> ret,
                    ChangePatternInstance instance,
                    List<CtElement> changedNodes,
                    Diff diff
            ) {
        boolean addedBefore = false;
        for (ChangePatternInstance existingInstance : ret) {
            Set<CtElement> instanceCoveredNodes = instanceToCoveredNodes.get(existingInstance);
            for (CtElement changedNode : changedNodes) {
                if (coveredByInstanceNodes(instanceCoveredNodes, changedNode)) {
                    addedBefore = true;
                    break;
                }
            }
            if(addedBefore)
                break;
        }
        if (!addedBefore) {
            ret.add(instance);
            instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
        }
    }

    @Override
    protected Set<CtElement> getInstanceCoveredNodes(ChangePatternInstance instance, Diff diff) {
        Set<CtElement> dstNodes = new HashSet<>();

        if (instance.getPattern().getName().contains(DEL_INS_PATTERN_NAME)) {
            for (Operation op : instance.getActions()) {
                if (op.getAction().getName().contains("INS")) {
                    dstNodes.add(op.getSrcNode());
                }
            }
        } else if (instance.getPattern().getName().contains(UPD_PATTERN_NAME)
                || instance.getPattern().getName().contains(DEL_MOV_PATTERN_NAME)) {
            dstNodes = instance.getActions().stream()
                    .map(action -> (action.getDstNode() != null ? action.getDstNode() : action.getSrcNode()))
                    .collect(Collectors.toSet());
        }

        Set<CtElement> srcNodes = new HashSet<>();
            if (instance.getPattern().getName().contains(DEL_INS_PATTERN_NAME)
                || instance.getPattern().getName().contains(DEL_MOV_PATTERN_NAME)) {
            for (Operation op : instance.getActions()) {
                if (op.getAction().getName().contains("DEL")) {
                    srcNodes.add(op.getSrcNode());
                }
            }
        } else if (instance.getPattern().getName().contains(UPD_PATTERN_NAME)) {
            srcNodes = instance.getActions().stream()
                    .map(action -> (action.getSrcNode())).collect(Collectors.toSet());
        }

        Set<CtElement> all = dstNodes;
        all.addAll(srcNodes);
        return all;
    }

    private Operation getActionFromDelInstance(ChangePatternInstance instance, String actionType) {
        if (instance.getActions().get(0).getAction().getName().equals(actionType)) {
            return instance.getActions().get(0);
        } else {
            return instance.getActions().get(1);
        }
    }
}