package fr.inria.coming.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.MappingStore;

import com.github.gumtreediff.tree.Tree;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * Classifies operations
 * 
 * @author Matias Martinez
 *
 */
public class OperationClassifier {

	public static MapList<Operation, Operation> getOperationHierarchy(Diff iDiff) {

		MapList<Operation, Operation> hierarchy = new MapList<>();

		Set<Tree> srcUpdTrees = new HashSet<>();
		Set<Tree> dstUpdTrees = new HashSet<>();
		Set<Tree> srcMvTrees = new HashSet<>();
		Set<Tree> dstMvTrees = new HashSet<>();
		Set<Tree> srcDelTrees = new HashSet<>();
		Set<Tree> dstAddTrees = new HashSet<>();
		Map<Tree, Operation> originalActionsSrc = new HashMap<>();
		Map<Tree, Operation> originalActionsDst = new HashMap<>();

		//
		MappingStore mappings = iDiff.getMappingsComp();
		// First step, classification of
		for (Operation operation : iDiff.getAllOperations()) {

			Action action = operation.getAction();

			final Tree original = action.getNode();
			if (action instanceof Delete) {
				srcDelTrees.add(original);
				originalActionsSrc.put(original, operation);
			} else if (action instanceof Insert) {
				dstAddTrees.add(original);
				originalActionsDst.put(original, operation);
			} else if (action instanceof Update) {
				Tree dest = mappings.getDstForSrc(original);
				original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST,
						dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				srcUpdTrees.add(original);
				dstUpdTrees.add(dest);
				originalActionsSrc.put(original, operation);
			} else if (action instanceof Move) {
				Tree dest = mappings.getDstForSrc(original);
				original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST,
						dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				srcMvTrees.add(original);
				dstMvTrees.add(dest);
				originalActionsDst.put(dest, operation);
			}
		}
		// Now, the hierarchy of Operations:

		for (Tree deletedSrc : srcDelTrees) {

			if (srcDelTrees.contains(deletedSrc.getParent()) || srcUpdTrees.contains(deletedSrc.getParent())) {

				Operation sonOperation = originalActionsSrc.get(deletedSrc);
				Operation parentOperation = originalActionsSrc.get(deletedSrc.getParent());
				hierarchy.add(parentOperation, sonOperation);
			}

		}

		for (Tree addedDst : dstAddTrees) {

			if (dstAddTrees.contains(addedDst.getParent()) || dstUpdTrees.contains(addedDst.getParent())) {

				Operation sonOperation = originalActionsDst.get(addedDst);
				Operation parentOperation = originalActionsDst.get(addedDst.getParent());
				hierarchy.add(parentOperation, sonOperation);

			}
		}

		for (Tree movedDst : dstMvTrees) {

			if (dstMvTrees.contains(movedDst.getParent())) {
				Operation sonOperation = originalActionsDst.get(movedDst);
				Operation parentOperation = originalActionsDst.get(movedDst.getParent());
				hierarchy.add(parentOperation, sonOperation);
			}

		}
		// We return the hierarchy
		return hierarchy;
	}
}
