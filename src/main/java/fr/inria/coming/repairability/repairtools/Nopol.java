package fr.inria.coming.repairability.repairtools;

import com.github.gumtreediff.tree.Tree;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.repairability.models.ASTData;
import fr.inria.coming.utils.ASTInfoResolver;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.matchers.MappingStore;

public class Nopol extends AbstractRepairTool {
	private static final String IF_UPD_SHALLOW_PATTERN = "if_condition_upd_shallow";
	private static final String IF_UPD_DEEP_PATTERN = "if_condition_upd_deep";
	private static final String IF_INS_SHALLOW_PATTERN = "if_condition_ins_shallow";
	private static final String IF_INS_DEEP_PATTERN = "if_condition_ins_deep";

	private static final String[] patternFileNames = { "if_upd_d.xml", "if_upd_s.xml", "if_ins_s.xml", "if_ins_d.xml" };

	/**
	 * Encodes the search space of Nopol
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
	 * @param instance
	 * @param revision
	 * @param diff
	 * @return
	 */
	@Override
	public boolean filter(ChangePatternInstance instance, IRevision revision, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

		if (patternType.startsWith(IF_UPD_DEEP_PATTERN)) {
			CtElement srcRootNode = null, dstCondition = null;
			Operation op = instance.getActions().get(0);

			if (op instanceof UpdateOperation) {
				CtElement srcNode = op.getSrcNode(), dstNode = op.getDstNode();

				dstCondition = getWrapperIfConditoin(dstNode);
				if (dstCondition == null) {
					return false;
				}

				srcRootNode = ASTInfoResolver.getRootNode(srcNode);
			} else if (op instanceof MoveOperation) {
				// FIXME: move operations should be handled as well.
				return false;
			} else if (op instanceof InsertOperation) {
				dstCondition = getWrapperIfConditoin(op.getSrcNode());

				Tree dstConditionParentTree = (Tree) dstCondition.getParent().getMetadata("gtnode");

				MappingStore mapping = diff.getMappingsComp();
				if (!mapping.isDstMapped(dstConditionParentTree))
					return false;

				CtElement srcNode = (CtElement) mapping.getSrcForDst(dstConditionParentTree).getMetadata("spoon_object");
				srcRootNode = ASTInfoResolver.getRootNode(srcNode);
			} else if (op instanceof DeleteOperation) {
				CtElement srcCondition = getWrapperIfConditoin(op.getSrcNode());
				Tree srcConditionTree = (Tree) srcCondition.getMetadata("gtnode");

				MappingStore mapping = diff.getMappingsComp();
				if (mapping.isSrcMapped(srcConditionTree)) {
					dstCondition = (CtElement) mapping.getDstForSrc(srcConditionTree).getMetadata("spoon_object");
				} else if (mapping.isSrcMapped(srcConditionTree.getParent())) {
					CtElement dstConditionParent = (CtElement) mapping.getDstForSrc(srcConditionTree.getParent())
							.getMetadata("spoon_object");

					if (dstConditionParent instanceof CtIf) {
						dstCondition = ((CtIf) dstConditionParent).getCondition();
					} else {
						return false;
					}
				} else {
					return false;
				}

				srcRootNode = ASTInfoResolver.getRootNode(op.getSrcNode());
			} else {
				return false;
			}

			return new ASTData(srcRootNode).canNopolGenerateCondition(dstCondition);
		} else if (patternType.startsWith(IF_INS_SHALLOW_PATTERN) || patternType.startsWith(IF_INS_DEEP_PATTERN)) {
			Operation insertOp = getInsertAction(instance);
			CtIf insertedIf = (CtIf) insertOp.getSrcNode();
			
			if (insertedIf.getElseStatement() != null)
				return false;

			CtElement srcRoot = null, dstRoot = ASTInfoResolver.getRootNode(insertedIf);
			Tree dstRootTree = (Tree) dstRoot.getMetadata("gtnode");
			
			MappingStore mapping = diff.getMappingsComp();
            if (mapping.isDstMapped(dstRootTree)) {
            	srcRoot = (CtElement)mapping.getSrcForDst(dstRootTree)
            			.getMetadata("spoon_object");
            } else {
            	return false;
            }

            CtElement dstCondition = insertedIf.getCondition();
            
			return new ASTData(srcRoot).canNopolGenerateCondition(dstCondition);
		} else if (patternType.startsWith(IF_UPD_SHALLOW_PATTERN)) {
			Operation op = instance.getActions().get(0);
			CtElement srcRootNode = ASTInfoResolver.getRootNode(op.getSrcNode()), dstNode = op.getDstNode();

			return new ASTData(srcRootNode).canNopolGenerateCondition(dstNode);
		}

		return false;
	}

	@Override
	protected Set<CtElement> getInstanceCoveredNodes(ChangePatternInstance instance, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

		if (patternType.startsWith(IF_UPD_DEEP_PATTERN)) {
			Set<CtElement> res = new HashSet<CtElement>();
			Operation op = instance.getActions().get(0);
			CtElement dstCondition = null, srcCondition = null;

			if (op instanceof InsertOperation || op instanceof UpdateOperation) {
				dstCondition = op instanceof InsertOperation ? getWrapperIfConditoin(op.getSrcNode())
						: getWrapperIfConditoin(op.getDstNode());
				res.add(dstCondition);
				Tree dstConditionTree = (Tree) dstCondition.getMetadata("gtnode");

				MappingStore mapping = diff.getMappingsComp();
				if (mapping.isDstMapped(dstConditionTree)) {

					res.add((CtElement) mapping.getSrcForDst(dstConditionTree).getMetadata("spoon_object"));

				} else if (mapping.isDstMapped(dstConditionTree.getParent())) {

					CtElement srcConditionParent = (CtElement) mapping.getSrcForDst(dstConditionTree.getParent())
							.getMetadata("spoon_object");

					if (srcConditionParent instanceof CtIf) {
						res.add(((CtIf) srcConditionParent).getCondition());
					}
				}
			} else if (op instanceof DeleteOperation) {
				srcCondition = getWrapperIfConditoin(op.getSrcNode());
				res.add(srcCondition);
				Tree srcConditionTree = (Tree) srcCondition.getMetadata("gtnode");

				MappingStore mapping = diff.getMappingsComp();
				if (mapping.isSrcMapped(srcConditionTree)) {

					res.add((CtElement) mapping.getDstForSrc(srcConditionTree).getMetadata("spoon_object"));

				} else if (mapping.isSrcMapped(srcConditionTree.getParent())) {

					CtElement dstConditionParent = (CtElement) mapping.getDstForSrc(srcConditionTree.getParent())
							.getMetadata("spoon_object");

					if (dstConditionParent instanceof CtIf) {
						res.add(((CtIf) dstConditionParent).getCondition());
					}
				}
			}
			return res;
		} else if (patternType.startsWith(IF_UPD_SHALLOW_PATTERN)) {
			Operation op = instance.getActions().get(0);

			Set<CtElement> res = new HashSet<CtElement>();
			res.add(op.getSrcNode());
			res.add(op.getDstNode());

			return res;
		}

		return super.getInstanceCoveredNodes(instance, diff);
	}

	@Override
	public List<ChangePatternInstance> filterSelectedInstances(List<ChangePatternInstance> lst, Diff diff) {
		Map<ChangePatternInstance, Set> instanceToCoveredNodes = new HashMap<>();
		List<ChangePatternInstance> ret = new ArrayList<>();

		for (ChangePatternInstance instance : lst) {
			if (instance.getPattern().getName().contains(IF_UPD_SHALLOW_PATTERN)
					|| instance.getPattern().getName().contains(IF_INS_SHALLOW_PATTERN)) {
				ret.add(instance);
				instanceToCoveredNodes.put(instance, getInstanceCoveredNodes(instance, diff));
			}
		}

		for (ChangePatternInstance instance : lst) {
			if (instance.getPattern().getName().contains(IF_INS_DEEP_PATTERN)) {
				List<CtElement> changedNodes = new ArrayList<>();

				changedNodes.add(instance.getActions().get(0).getSrcNode());
				if (instance.getActions().get(0).getDstNode() != null)
					changedNodes.add(instance.getActions().get(0).getDstNode());

				changedNodes.add(instance.getActions().get(1).getSrcNode());
				if (instance.getActions().get(1).getDstNode() != null)
					changedNodes.add(instance.getActions().get(1).getDstNode());

				updateSelectedInstances(instanceToCoveredNodes, ret, instance, changedNodes, diff);
			}
		}

		for (ChangePatternInstance instance : lst) {
			if (instance.getPattern().getName().contains(IF_UPD_DEEP_PATTERN)) {
				List<CtElement> changedNodes = new ArrayList<>();
				changedNodes.add(instance.getActions().get(0).getSrcNode());
				if (instance.getActions().get(0).getDstNode() != null)
					changedNodes.add(instance.getActions().get(0).getDstNode());
				updateSelectedInstances(instanceToCoveredNodes, ret, instance, changedNodes, diff);
			}
		}

		return ret;
	}

	@Override
	public boolean coversTheWholeDiff(ChangePatternInstance instance, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];
		CtElement instanceInsretedNode = null;

		if (patternType.startsWith(IF_INS_SHALLOW_PATTERN) || patternType.startsWith(IF_INS_DEEP_PATTERN)) {
			for (Operation op : instance.getActions()) {
				if (op instanceof InsertOperation) {
					instanceInsretedNode = op.getSrcNode();
				}
			}
		}

		Set<CtElement> instanceNodes = getInstanceCoveredNodes(instance, diff);

		for (Operation diffOperation : diff.getRootOperations()) {
			if (diffOperation instanceof InsertOperation) {
				CtElement opInsertedNode = diffOperation.getSrcNode();

				if (opInsertedNode instanceof CtBlock && instanceInsretedNode != null
						&& opInsertedNode == instanceInsretedNode.getParent()) {
					// Bug in Gumtree-diff?
					continue;
				}
			}

			boolean found = coveredByInstanceNodes(instance, instanceNodes, diffOperation);

			if (found == false)
				return false;
		}

		return true;
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

	private CtElement getWrapperIfConditoin(CtElement node) {
		List<CtElement> pathToRoot = ASTInfoResolver.getPathToRootNode(node);

		for (int i = pathToRoot.size() - 1; i >= 0; i--) {
			if (pathToRoot.get(i).getRoleInParent().equals(CtRole.CONDITION) && pathToRoot.get(i - 1) instanceof CtIf) {
				return pathToRoot.get(i);
			}
		}

		return null;
	}

	private Operation getInsertAction(ChangePatternInstance instance) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];
		if(!patternType.startsWith(IF_INS_SHALLOW_PATTERN) && !patternType.startsWith(IF_INS_DEEP_PATTERN)) {
			return null;
		}
		
		Operation insOp = instance.getActions().get(0) instanceof InsertOperation ? instance.getActions().get(0)
				: instance.getActions().get(1);

		return insOp;
	}

}