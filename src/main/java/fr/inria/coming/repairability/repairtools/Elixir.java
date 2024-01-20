package fr.inria.coming.repairability.repairtools;

import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;
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
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

import javax.naming.ldap.SortResponseControl;
import java.io.File;
import java.util.*;

/**
 * Elixir fixes programs by doing 8 things:
 * <p>
 * 1.loosening or tightening variable types => patter file ep_1 2.changing the
 * return statement => patter file ep_2 3.adding if (variable != null) => patter
 * file ep_3 4.array index in bound check => patter file ep_4 5.chnage of a
 * boolean operator => patter file ep_5 6.loosening or tightening boolean
 * expressions => patter file ep_6,7 7.changing method invocations => patter
 * file ep_8 8.inserting the method invocation => patter file ep_9
 */

public class Elixir extends AbstractRepairTool {
	private static final String RETURN_UPDATE_DEEP_PATTERN = "update_return_deep";
	private static final String INVOCATION_UPDATE_DEEP_PATTERN = "update_invocation_deep";
	private static final String INSERT_INVOCATION_PATTERN = "Insert_Invocation";
	private static final String UPDATE_INVOCATION_PATTERN = "update_invocation";
	private static final String BO_CHANGE_PATTERN = "BO_change";
	private static final String LOOSE_TYPE_1_PATTERN = "loose_type1";
	private static final String LOOSE_TYPE_2_PATTERN = "loose_type2";
	private static final String CHECK_NULL_PATTERN = "if_null_check";
	private static final String CHECK_LENGTH_PATTERN = "check_length";

	private static final String[] patternFileNames = { "ep_1.xml", "ep_2.xml", "ep_3.xml", "ep_4.xml", "ep_5.xml",
			"ep_6.xml", "ep_7.xml", "ep_8.xml", "ep_9.xml", "ep_10.xml", "ep_11.xml", "ep_12.xml" };

	/**
	 * Encodes the search space of Elixir
	 *
	 * @return a List of ChangePatternSpecifications that are supposed to be mined
	 *         by PatternInstanceAnalyzer
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
	 * Certain patterns/characteristics of search-space of a repair tool can't be
	 * represented by ChangePatternSpecification This filter is supposed to
	 * delete/remove such instances from the results given by
	 * PatternInstanceAnalyser.
	 *
	 * @param patternInstance
	 * @param diff
	 * @return
	 */
	@Override
	public boolean filter(ChangePatternInstance patternInstance, IRevision revision, Diff diff) {
		String patternType = patternInstance.getPattern().getName().split(File.pathSeparator)[1];

		if (patternType.startsWith(RETURN_UPDATE_DEEP_PATTERN)) {
			return doesSrcContainUpdatedParentOfType(patternInstance, diff, CtEntityType.RETURN);
		}

		if (patternType.startsWith(INVOCATION_UPDATE_DEEP_PATTERN)) {
			return doesSrcContainUpdatedParentOfType(patternInstance, diff, CtEntityType.ABSTRACT_INVOCATION);
		}

		if (patternType.startsWith(INSERT_INVOCATION_PATTERN)) {
			Operation op = patternInstance.getActions().get(0);
			MappingStore mapping = diff.getMappingsComp();
			if (!mapping.isSrcMapped(((Insert) op.getAction()).getParent()))
				// this inserted element is a part of another inserted element
				return false;

			CtElement srcNode = ((InsertOperation) op).getParent();
			CtElement srcRootNode = ASTInfoResolver.getRootNode(srcNode);

			return new ASTData(srcRootNode).canElixirGenerateNode(null, op.getSrcNode());
		}
		
		if(patternType.startsWith(UPDATE_INVOCATION_PATTERN)) {
			Operation op = patternInstance.getActions().get(0);
			
			CtElement srcRootNode = ASTInfoResolver.getRootNode(op.getSrcNode());
			return new ASTData(srcRootNode).canElixirGenerateNode(op.getSrcNode(), op.getDstNode());
		}

		if (patternType.startsWith(BO_CHANGE_PATTERN)) {
			Operation upd = patternInstance.getActions().get(0);
			CtBinaryOperator src = (CtBinaryOperator) upd.getSrcNode();
			CtBinaryOperator dst = (CtBinaryOperator) upd.getDstNode();

			return src.getLeftHandOperand().equals(dst.getLeftHandOperand())
					&& src.getRightHandOperand().equals(dst.getRightHandOperand());
		}

		if (patternType.startsWith(LOOSE_TYPE_2_PATTERN)) {

			Operation upd = patternInstance.getActions().get(0);
			CtElement src = upd.getSrcNode();
			CtElement dst = upd.getDstNode();

			return ((CtBinaryOperator) dst.getParent()).getRightHandOperand().equals(true)
					|| ((CtBinaryOperator) dst.getParent()).getRightHandOperand().equals(false);
		}

		if (patternType.startsWith(LOOSE_TYPE_1_PATTERN)) {

			Operation upd = patternInstance.getActions().get(0);
			CtElement src = upd.getSrcNode();
			CtElement dst = upd.getDstNode();

			return ((CtBinaryOperator) dst.getParent()).getLeftHandOperand().equals(true)
					|| ((CtBinaryOperator) dst.getParent()).getLeftHandOperand().equals(false);
		}

		if (patternType.startsWith(CHECK_LENGTH_PATTERN) || patternType.startsWith(CHECK_NULL_PATTERN)) {
			Operation insIfOp = null, movOp = null;
			for (Operation op : patternInstance.getActions()) {
				if (op.getAction().getName().equals("INS") && op.getSrcNode() instanceof CtIf) {
					insIfOp = op;
				}
				if (op.getAction().getName().equals("MOV")) {
					movOp = op;
				}
			}
			if (insIfOp == null) {
				return false;
			}
			CtIf insertedNode = (CtIf) insIfOp.getSrcNode();
			List<CtStatement> thenStatements = insertedNode.getThenStatement()
					.getElements(new TypeFilter<CtStatementList>(CtStatementList.class)).get(0).getStatements();
			if (thenStatements.size() != 1) {
				return false;
			}

			if (insertedNode.getElseStatement() != null) {
				List<CtStatement> elseStatements = insertedNode.getElseStatement()
						.getElements(new TypeFilter<CtStatementList>(CtStatementList.class)).get(0).getStatements();
				if (elseStatements.size() != 1) {
					return false;
				}
			}

			CtElement dstMov = movOp.getDstNode();
			if (!ASTInfoResolver.getPathToRootNode(dstMov).contains(insertedNode)) {
				return false;
			}

			return true;
		}

		return true;
	}

	private boolean doesSrcContainUpdatedParentOfType(ChangePatternInstance patternInstance, Diff diff,
			CtEntityType entityType) {
		Operation op = patternInstance.getActions().get(0);

		CtElement srcNode = null, dstMappedExpression = null, dstMappedElement = null, dstNode = null;

		if (op instanceof UpdateOperation) {
			srcNode = op.getSrcNode();
			dstNode = op.getDstNode();
		} else if (op instanceof InsertOperation) {
			dstNode = op.getSrcNode();

			MappingStore mapping = diff.getMappingsComp();
			if (!mapping.isSrcMapped(((Insert) op.getAction()).getParent()))
				// this inserted element is a part of another inserted element
				return false;

			srcNode = ((InsertOperation) op).getParent();
		} else {
			// FIXME: delete should be handled as well
			srcNode = op.getSrcNode();

			MappingStore mapping = diff.getMappingsComp();
			Tree srcParentITree = op.getAction().getNode().getParent();
			if (!mapping.isSrcMapped(srcParentITree))
				// this inserted element is a part of another inserted element
				return false;

			CtElement dstParentNode = (CtElement) mapping.getDstForSrc(op.getAction().getNode().getParent())
					.getMetadata("spoon_object");

			CtElement srcRootNode = ASTInfoResolver.getRootNode(srcNode);
			return new ASTData(srcRootNode).canElixirGenerateNode(null, dstParentNode);
		}

		dstMappedElement = ASTInfoResolver.getFirstAncestorOfType(dstNode, entityType);
		if (entityType.equals(CtEntityType.RETURN)) {
			dstMappedExpression = ((CtReturn) dstMappedElement).getReturnedExpression();
		} else if (entityType.equals(CtEntityType.ABSTRACT_INVOCATION)) {
			dstMappedExpression = dstMappedElement;
		}

		CtElement srcMappedElement = ASTInfoResolver.getFirstAncestorOfType(srcNode, entityType),
				srcMappedExpression = null;
		if (srcMappedElement == null)
			return false;
		if (entityType.equals(CtEntityType.RETURN)) {
			srcMappedExpression = ((CtReturn) srcMappedElement).getReturnedExpression();
		} else if (entityType.equals(CtEntityType.ABSTRACT_INVOCATION)) {
			srcMappedExpression = srcMappedElement;
		}

		CtElement srcRootNode = ASTInfoResolver.getRootNode(srcNode);
		return new ASTData(srcRootNode).canElixirGenerateNode(srcMappedExpression, dstMappedExpression);
	}

	@Override
	protected Set<CtElement> getInstanceCoveredNodes(ChangePatternInstance instance, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

		if (patternType.startsWith(RETURN_UPDATE_DEEP_PATTERN)) {
			return getCoveredElementsOfParentWithType(instance, diff, CtEntityType.RETURN);
		} else if (patternType.contains(INVOCATION_UPDATE_DEEP_PATTERN)) {
			return getCoveredElementsOfParentWithType(instance, diff, CtEntityType.ABSTRACT_INVOCATION);
		}

		return super.getInstanceCoveredNodes(instance, diff);
	}

	private Set<CtElement> getCoveredElementsOfParentWithType(ChangePatternInstance instance, Diff diff,
			CtEntityType entityType) {
		Set<CtElement> res = new HashSet<>();
		Operation op = instance.getActions().get(0);

		if (op instanceof UpdateOperation || op instanceof InsertOperation) {
			CtElement dstNode = null;

			if (op instanceof UpdateOperation) {
				dstNode = op.getDstNode();
			} else if (op instanceof InsertOperation) {
				dstNode = op.getSrcNode();
			}

			CtElement dstParentNode = ASTInfoResolver.getFirstAncestorOfType(dstNode, entityType);
			res.add(dstParentNode);

			Tree dstParentTree = (Tree) dstParentNode.getMetadata("gtnode");

			MappingStore mapping = diff.getMappingsComp();
			if (!mapping.isSrcMapped(dstParentTree))
				return res;

			Tree srcParentTree = mapping.getSrcForDst(dstParentTree);
			CtElement srcParentNode = (CtElement) srcParentTree.getMetadata("spoon_object");

			res.add(srcParentNode);

			return res;
		} else if (op instanceof DeleteOperation) {
			CtElement srcParentNode = op.getSrcNode().getParent();
			res.add(srcParentNode);

			Tree srcParentTree = (Tree) srcParentNode.getMetadata("gtnode");

			MappingStore mapping = diff.getMappingsComp();
			if (!mapping.isSrcMapped(srcParentTree))
				return res;

			CtElement dstParentNode = (CtElement) mapping.getDstForSrc(srcParentTree).getMetadata("spoon_object");
			res.add(dstParentNode);

			return res;
		}

		return res;
	}

	@Override
	public List<ChangePatternInstance> filterSelectedInstances(List<ChangePatternInstance> lst, Diff diff) {
		Map<ChangePatternInstance, Set> instanceToCoveredNodes = new HashMap<>();
		List<ChangePatternInstance> ret = new ArrayList<>();

		for (ChangePatternInstance instance : lst) {
			String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];
			if (!patternType.contains(RETURN_UPDATE_DEEP_PATTERN)
					&& !patternType.contains(INVOCATION_UPDATE_DEEP_PATTERN)) {
				ret.add(instance);
				instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
			}
		}

		filterAndAddSelectedInstancesOfPattern(lst, diff, instanceToCoveredNodes, ret, INVOCATION_UPDATE_DEEP_PATTERN);

		filterAndAddSelectedInstancesOfPattern(lst, diff, instanceToCoveredNodes, ret, RETURN_UPDATE_DEEP_PATTERN);

		return ret;
	}

	private void filterAndAddSelectedInstancesOfPattern(List<ChangePatternInstance> lst, Diff diff,
			Map<ChangePatternInstance, Set> instanceToCoveredNodes, List<ChangePatternInstance> ret, String pattern) {
		for (ChangePatternInstance instance : lst) {
			String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];
			if (patternType.contains(pattern)) {
				List<CtElement> changedNodes = new ArrayList<>();
				changedNodes.add(instance.getActions().get(0).getSrcNode());
				if (instance.getActions().get(0).getDstNode() != null)
					changedNodes.add(instance.getActions().get(0).getDstNode());
				updateSelectedInstances(instanceToCoveredNodes, ret, instance, changedNodes, diff);
			}
		}
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
}
