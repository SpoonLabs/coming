package fr.inria.coming.repairability.repairtools;

import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.utils.ASTInfoResolver;
import fr.inria.coming.utils.CtEntityType;
import fr.inria.coming.utils.EntityTypesInfoResolver;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.*;
import spoon.reflect.declaration.*;

import java.util.*;

public class JGenProg extends AbstractRepairTool {
    private static final String SHALLOW_PATTERN = "any_statement_s";
    private static final String DEEP_PATTERN = "any_statement_d";

    private static final String[] patternFileNames = {
            SHALLOW_PATTERN + ".xml",
            DEEP_PATTERN + ".xml"
    };

    @Override
    protected List<ChangePatternSpecification> readPatterns() {
        List<ChangePatternSpecification> patterns = new ArrayList<>();
        for (String fileName : patternFileNames) {
            patterns.add(PatternXMLParser.parseFile(getPathFromResources(fileName)));
        }
        return patterns;
    }

    @Override
    public boolean filter(ChangePatternInstance instance, IRevision revision, Diff diff) {
        Operation operation = instance.getActions().get(0);

        CtElement newElement, srcNode;
        if (instance.getPattern().getName().contains(SHALLOW_PATTERN)) {

            if (operation instanceof InsertOperation) {
                MappingStore mapping = diff.getMappingsComp();
                if (!mapping.isSrcMapped(((Insert)operation.getAction()).getParent()))
                    return false;

                newElement = operation.getSrcNode(); // See why are using SrcNode: https://github.com/SpoonLabs/coming/issues/72#issuecomment-508123273
                srcNode = ((InsertOperation) operation).getParent();
            } else if (operation instanceof UpdateOperation) {
                newElement = operation.getDstNode(); // See why are using DstNode: https://github.com/SpoonLabs/coming/issues/72#issuecomment-508123273
                srcNode = operation.getSrcNode();
            } else if (operation instanceof DeleteOperation) {
                // ASSUMPTION: ONLY A STATEMENT CAN BE DELETED
                return EntityTypesInfoResolver.getInstance().isAChildOf(EntityTypesInfoResolver.getNodeLabelFromCtElement(operation.getSrcNode()),
                        CtEntityType.STATEMENT.toString());
            } else if (operation instanceof MoveOperation) {
                // FIXME: we should not ignore move operations.
                return false;
            } else {
                return false;
            }
        } else if (instance.getPattern().getName().contains(DEEP_PATTERN)) {
            CtElement affectedNode;
            if (operation instanceof InsertOperation) {
                MappingStore mapping = diff.getMappingsComp();
                if (!mapping.isSrcMapped(((Insert)operation.getAction()).getParent()))
                    return false;

                affectedNode = operation.getSrcNode();
                srcNode = ((InsertOperation) operation).getParent();
            } else if (operation instanceof DeleteOperation) {
                MappingStore mapping = diff.getMappingsComp();

                if (!mapping.isSrcMapped(operation.getAction().getNode().getParent()))
                    return false;

                Tree dstTree = mapping.getDstForSrc(operation.getAction().getNode().getParent());
                affectedNode = (CtElement) dstTree.getMetadata("spoon_object");
                srcNode = operation.getSrcNode();
            } else if (operation instanceof UpdateOperation) {
                affectedNode = operation.getDstNode();
                srcNode = operation.getSrcNode();
            } else {
                // FIXME: deep_mov operations should not be ignored
                return false;
            }

            newElement = ASTInfoResolver.getFirstAncestorOfType(affectedNode, CtEntityType.STATEMENT);
        } else {
            return false;
        }

        return doesElementOccursInSrcNode(srcNode, newElement);
    }

    private boolean doesElementOccursInSrcNode(CtElement srcNode, CtElement element) {
        CtElement srcRootNodes = ASTInfoResolver.getPathToRootNode(srcNode).get(0);
        List<CtElement> allSrcElements = srcRootNodes.getElements(null);

        String elementStr = element.toString();
        for (CtElement srcElement : allSrcElements) {
        	String srcElementStr;
        	try {
        		srcElementStr = srcElement.toString();
        	}catch(Exception e) {
        		continue; // ex.: on "org-tigris-jsapar__jsapar" repo
        	}
            if (srcElementStr.equals(elementStr))
                return true;
            else if (srcElementStr.replace(elementStr, "").trim().equals(elementStr.trim()))
                // for when a block is inserted inside itself: see patch1-Math-56-JGenProg2017
                return true;
        }
        return false;
    }

    @Override
    protected Set<CtElement> getInstanceCoveredNodes(ChangePatternInstance instance, Diff diff) {
        Set<CtElement> res = new HashSet<>();
        Operation op = instance.getActions().get(0);

        if (instance.getPattern().getName().contains(SHALLOW_PATTERN)) {
            if (op instanceof InsertOperation) {
                res.add(op.getSrcNode());
            } else if (op instanceof UpdateOperation) {
                res.add(op.getSrcNode());
                res.add(op.getDstNode());
            } else if (op instanceof DeleteOperation) {
                res.add(op.getSrcNode());
            } else if (op instanceof MoveOperation) {
                res.add(op.getDstNode());
            }
        } else if (instance.getPattern().getName().contains(DEEP_PATTERN)) {
            if (op instanceof InsertOperation) {
                res.add(ASTInfoResolver.getFirstAncestorOfType(op.getSrcNode(), CtEntityType.STATEMENT));
                res.add(ASTInfoResolver.getFirstAncestorOfType(((InsertOperation) op).getParent(), CtEntityType.STATEMENT));
            } else if (op instanceof UpdateOperation) {
                res.add(ASTInfoResolver.getFirstAncestorOfType(op.getSrcNode(), CtEntityType.STATEMENT));
                res.add(ASTInfoResolver.getFirstAncestorOfType(op.getDstNode(), CtEntityType.STATEMENT));
            } else if (op instanceof DeleteOperation) {
                res.add(ASTInfoResolver.getFirstAncestorOfType(op.getSrcNode(), CtEntityType.STATEMENT));
            }
        }

        return res;
    }

    @Override
    public List<ChangePatternInstance> filterSelectedInstances(List<ChangePatternInstance> lst, Diff diff) {
        Map<ChangePatternInstance, Set> instanceToCoveredNodes = new HashMap<>();
        List<ChangePatternInstance> ret = new ArrayList<>();

        for (ChangePatternInstance instance : lst) {
            if (instance.getPattern().getName().contains(SHALLOW_PATTERN)) {
                ret.add(instance);
                instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
            }
        }

        for (ChangePatternInstance instance : lst) {
            if (instance.getPattern().getName().contains(DEEP_PATTERN)) {
                List<CtElement> changedNodes = new ArrayList<>();
                changedNodes.add(instance.getActions().get(0).getSrcNode());
                if (instance.getActions().get(0).getDstNode() != null)
                    changedNodes.add(instance.getActions().get(0).getDstNode());
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
                    Collection<CtElement> changedNodes,
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
            if (addedBefore)
                break;
        }
        if (!addedBefore) {
            ret.add(instance);
            instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
        }
    }
}

