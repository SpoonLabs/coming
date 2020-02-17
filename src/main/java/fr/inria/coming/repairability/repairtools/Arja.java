package fr.inria.coming.repairability.repairtools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.repairability.models.ASTData;
import fr.inria.coming.utils.ASTInfoResolver;
import fr.inria.coming.utils.CtEntityType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.Launcher;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

/**
 * Arja tries to correct the code by inserting statements that are in the source
 * file either in a direct approach or a type matching approach.
 * <p>
 * Direct Approach: the extracted variable/Method with the same name and
 * compatible types exists in the variable/Method scope.
 * <p>
 * statement or the invocation exists in the source code with compatible types.
 * (Read more about this on
 * https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=8485732)
 */

public class Arja extends AbstractRepairTool {
	private static final int INS_DEL_COMMON_PAR_HEIGHT = 5;

	private static final String ARJA_DEEP_PATTERN = "arja_deep_change";
	private static final String ARJA_SHALLOW_PATTERN = "arja_shallow_change";
	private static final String ARJA_INS_DEL = "arja_ins_del";

	private static final String[] patternFileNames = { ARJA_DEEP_PATTERN + ".xml", ARJA_SHALLOW_PATTERN + ".xml",
			ARJA_INS_DEL + ".xml" };

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
		try { // line 156 throws exception.
			String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];
			Operation op = instance.getActions().get(0);

			if (patternType.contains(ARJA_SHALLOW_PATTERN)) {

				if (op instanceof MoveOperation) {
					// FIXME: shallow_mov operations should not be ignored
					return false;
				} else if (op instanceof DeleteOperation) {
					return true;
				} else if (op instanceof InsertOperation) {
					MappingStore mapping = diff.getMappingsComp();
					if (!mapping.hasSrc(((Insert) op.getAction()).getParent()))
						return false;

					CtElement parentOfInsertedNode = ((InsertOperation) op).getParent();

					if (parentOfInsertedNode == null) // smallcreep__cucumber-seeds
						return false;

					CtElement srcRoot = ASTInfoResolver.getRootNode(parentOfInsertedNode);
					return canBeReproducedFromSrc(srcRoot, (CtStatement) op.getSrcNode());
				} else if (op instanceof UpdateOperation) {
					return canBeReproducedFromSrc(ASTInfoResolver.getRootNode(op.getSrcNode()),
							(CtStatement) op.getDstNode());
				}

			} else if (patternType.contains(ARJA_DEEP_PATTERN)) {

				CtElement affectedNode, srcNode;
				if (op instanceof InsertOperation) {
					MappingStore mapping = diff.getMappingsComp();
					if (!mapping.hasSrc(((Insert) op.getAction()).getParent()))
						return false;

					affectedNode = op.getSrcNode().getParent();
					srcNode = ((InsertOperation) op).getParent();
				} else if (op instanceof DeleteOperation) {
					MappingStore mapping = diff.getMappingsComp();

					if (!mapping.hasSrc(op.getAction().getNode().getParent()))
						return false;

					ITree dstTree = mapping.getDst(op.getAction().getNode().getParent());
					affectedNode = (CtElement) dstTree.getMetadata("spoon_object");
					srcNode = op.getSrcNode();
				} else if (op instanceof UpdateOperation) {
					affectedNode = op.getDstNode();
					srcNode = op.getSrcNode();
				} else {
					// FIXME: deep_mov operations should not be ignored
					return false;
				}

				CtStatement newElement = (CtStatement) ASTInfoResolver.getFirstAncestorOfType(affectedNode,
						CtEntityType.STATEMENT);

				if (newElement == null || srcNode == null) {
					return false;
				}

				return canBeReproducedFromSrc(ASTInfoResolver.getRootNode(srcNode), newElement);
			} else if (patternType.contains(ARJA_INS_DEL)) {
				Operation insOp = getActionFromDelInsInstance(instance, "INS"),
						delOp = getActionFromDelInsInstance(instance, "DEL");

				MappingStore mapping = diff.getMappingsComp();
				if (!mapping.hasSrc(((Insert) op.getAction()).getParent()))
					// the inserted node is a part of a parent inserted node
					return false;

				CtElement insertedNodeParent = ((InsertOperation) insOp).getParent();

				if (insertedNodeParent == null)
					return false;

				List<CtElement> insPars = ASTInfoResolver.getNSubsequentParents(insertedNodeParent,
						INS_DEL_COMMON_PAR_HEIGHT),
						delPars = ASTInfoResolver.getNSubsequentParents(delOp.getSrcNode(), INS_DEL_COMMON_PAR_HEIGHT);

				Set<CtElement> insDistinctParsSet = new HashSet<CtElement>();
				insDistinctParsSet.addAll(insPars);
				insDistinctParsSet.removeAll(delPars);

				if (insDistinctParsSet.size() == insPars.size())
					return false;

				CtElement srcRoot = ASTInfoResolver.getRootNode(insertedNodeParent);
				return canBeReproducedFromSrc(srcRoot, (CtStatement) op.getSrcNode());
			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean canBeReproducedFromSrc(CtElement src, CtStatement target) {
		if (!checkSrcContainsTargetVarsAndMethods(src, target)) {
			return false;
		}

		if (!checkSrcIncludesDstTemplate(src, target))
			return false;

		return true;
	}

	private boolean checkSrcContainsTargetVarsAndMethods(CtElement src, CtElement target) {
		return new ASTData(ASTInfoResolver.getRootNode(src)).canArjaFindVarsAndMethods(target);
	}

	private boolean checkSrcIncludesDstTemplate(CtElement srcNode, CtElement dstNode) {
		CtElement srcRootNode = ASTInfoResolver.getPathToRootNode(srcNode).get(0);
		List<CtElement> allSrcElements = srcRootNode.getElements(null);

		List<CtElement> allDstElements = dstNode.getElements(null);
		String dstNodeAsString = dstNode.toString();
		// the following for-loop replaces variable/method names in dstNodeAsString with
		// their type name
		for (int i = 0; i < allDstElements.size(); i++) {
			CtElement dstElement = allDstElements.get(i);

			if ((dstElement instanceof CtVariableAccess || dstElement instanceof CtVariable)
					&& !(dstElement instanceof CtLocalVariable)) {
				String variableType = getType(dstElement);
				dstNodeAsString = replaceElement(dstNodeAsString, (dstElement instanceof CtField ? 
						((CtField)dstElement).getSimpleName() : dstElement.toString()), "#" + variableType + "#");
			}

			if (dstElement instanceof CtAbstractInvocation) {
				String methodStr = ((CtAbstractInvocation) dstElement).getExecutable().toString();
				methodStr = methodStr.substring(0, methodStr.indexOf('(')) + "(";
				dstNodeAsString = replaceElement(dstNodeAsString, methodStr, "#METHOD#(");
			}
		}

		for (int i = 0; i < allSrcElements.size() - allDstElements.size() + 1; i++) {
			CtElement currentSrcElement = allSrcElements.get(i);
			if (!(currentSrcElement instanceof CtStatement)) {
				continue;
			}

			List<CtElement> curSrcSubElements = currentSrcElement.getElements(null);
			if (curSrcSubElements.size() > allDstElements.size() * 5) {
				// to ignore the case where the srcElem is too bigger than the dstElem
				continue;
			}

			String srcAsString = currentSrcElement.toString();
			Set<CtElement> elementsInSubtree = new HashSet<>();
			elementsInSubtree.add(currentSrcElement);
			for (int j = 0; j == 0 || (i + j < allSrcElements.size()
					&& elementsInSubtree.contains(allSrcElements.get(i + j).getParent())); j++) {
				CtElement srcElement = allSrcElements.get(i + j);

				elementsInSubtree.add(srcElement);

				if ((srcElement instanceof CtVariable || srcElement instanceof CtVariableAccess)
						&& !(srcElement instanceof CtLocalVariable)) {
					String variableOrLiteralType = getType(srcElement);
					srcAsString = replaceElement(srcAsString, srcElement.toString(), "#" + variableOrLiteralType + "#");
				}

				if (srcElement instanceof CtAbstractInvocation) {
					String methodStr = ((CtAbstractInvocation) srcElement).getExecutable().toString();
					methodStr = methodStr.substring(0, methodStr.indexOf('(')) + "(";
					srcAsString = replaceElement(srcAsString, methodStr, "#METHOD#(");
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
			if (!((ind > 0 && isVariableNameChar(source.charAt(ind - 1))) || (ind + element.length() < source.length()
					&& isVariableNameChar(source.charAt(ind + element.length()))))) {
				// the chars before and after the element are not a variable-name-char
				source = source.substring(0, ind) + target
						+ (ind + element.length() >= source.length() ? "" : source.substring(ind + element.length()));
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
		return (c <= 'z' && c >= 'a') || (c <= 'Z' && c >= 'A') || (c <= '9' && c >= '0') || c == '_';
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

	@Override
	protected Set<CtElement> getInstanceCoveredNodes(ChangePatternInstance instance, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

		Set<CtElement> res = new HashSet<>();
		Operation op = instance.getActions().get(0);

		if (instance.getPattern().getName().contains(ARJA_SHALLOW_PATTERN)) {
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
		} else if (instance.getPattern().getName().contains(ARJA_DEEP_PATTERN)) {
			if (op instanceof InsertOperation) {
				res.add(ASTInfoResolver.getFirstAncestorOfType(op.getSrcNode(), CtEntityType.STATEMENT));
				res.add(ASTInfoResolver.getFirstAncestorOfType(((InsertOperation) op).getParent(),
						CtEntityType.STATEMENT));
			} else if (op instanceof UpdateOperation) {
				res.add(ASTInfoResolver.getFirstAncestorOfType(op.getSrcNode(), CtEntityType.STATEMENT));
				res.add(ASTInfoResolver.getFirstAncestorOfType(op.getDstNode(), CtEntityType.STATEMENT));
			} else if (op instanceof DeleteOperation) {
				res.add(ASTInfoResolver.getFirstAncestorOfType(op.getSrcNode(), CtEntityType.STATEMENT));
			}
		} else if (instance.getPattern().getName().contains(ARJA_INS_DEL)) {
			Operation delOp = getActionFromDelInsInstance(instance, "DEL"),
					insOp = getActionFromDelInsInstance(instance, "INS");

			res.add(insOp.getSrcNode());
			res.add(delOp.getSrcNode());
		}

		return res;
	}

	@Override
	public List<ChangePatternInstance> filterSelectedInstances(List<ChangePatternInstance> lst, Diff diff) {
		Map<ChangePatternInstance, Set> instanceToCoveredNodes = new HashMap<>();
		List<ChangePatternInstance> ret = new ArrayList<>();

		for (ChangePatternInstance instance : lst) {
			if (instance.getPattern().getName().contains(ARJA_SHALLOW_PATTERN)) {
				ret.add(instance);
				instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
			}
		}

		for (ChangePatternInstance instance : lst) {
			if (instance.getPattern().getName().contains(ARJA_INS_DEL)) {
				List<CtElement> changedNodes = new ArrayList<>();

				Operation delOp = getActionFromDelInsInstance(instance, "DEL"),
						insOp = getActionFromDelInsInstance(instance, "INS");

				changedNodes.add(insOp.getSrcNode());
				changedNodes.add(delOp.getSrcNode());
				updateSelectedInstances(instanceToCoveredNodes, ret, instance, changedNodes, diff);
			}
		}

		for (ChangePatternInstance instance : lst) {
			if (instance.getPattern().getName().contains(ARJA_DEEP_PATTERN)) {
				List<CtElement> changedNodes = new ArrayList<>();
				changedNodes.add(instance.getActions().get(0).getSrcNode());
				if (instance.getActions().get(0).getDstNode() != null)
					changedNodes.add(instance.getActions().get(0).getDstNode());
				updateSelectedInstances(instanceToCoveredNodes, ret, instance, changedNodes, diff);
			}
		}

		return ret;
	}

	private void updateSelectedInstances(Map<ChangePatternInstance, Set> instanceToCoveredNodes,
			List<ChangePatternInstance> ret, ChangePatternInstance instance, Collection<CtElement> changedNodes,
			Diff diff) {
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

	private Operation getActionFromDelInsInstance(ChangePatternInstance instance, String actionType) {
		if (instance.getActions().get(0).getAction().getName().equals(actionType)) {
			return instance.getActions().get(0);
		} else {
			return instance.getActions().get(1);
		}
	}
}
