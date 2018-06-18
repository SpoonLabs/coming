package fr.inria.sacha.coming.analyzer.commitAnalyzer.filters;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

import fr.inria.sacha.coming.analyzer.treeGenerator.PatternAction;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.entity.ActionType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternFilter extends SimpleChangeFilter {

	public PatternFilter(List<PatternAction> patternAction) {
		super(patternAction);
	}

	public PatternFilter(String typeLabel, ActionType operationType) {

		super(typeLabel, operationType);

	}

	public PatternFilter(String typeLabel, ActionType operationType, String parentTypeLabel, int parentLevels) {

		super(typeLabel, operationType);

		this.patternActions.get(0).getAffectedEntity().setParent(new PatternEntity(parentTypeLabel), parentLevels);
	}

	public PatternFilter(PatternEntity entity, ActionType operationType) {
		super(entity, operationType);

	}

	public PatternFilter(PatternAction patternAction) {
		super(patternAction);
	}

	/**
	 * Filtre changes according to the pattern specification
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<Operation> process(Diff diff) {

		List<Operation> result = new ArrayList<>();

		List<Operation> filtered = super.process(diff);

		List<Operation> all = diff.getAllOperations();

		for (PatternAction patternaction : this.patternActions) {
			boolean keepsRelation = false;
			for (Operation action : filtered) {
				keepsRelation |= checkparent(patternaction.getAffectedEntity().getParent(),
						patternaction.getAffectedEntity().getParentLevel(), filtered, action, all);

				if (keepsRelation) {
					result.add(action);
				}
			}

		}

		return result;
	}

	/**
	 * For each parent entity we validate the parent relation is valid
	 * 
	 * @param parentEntityFromPattern
	 * @param filtered
	 * @param affectedAction
	 * @param allActions
	 * @return
	 */
	protected boolean checkparent(PatternEntity parentEntityFromPattern, int parentLevel, List<Operation> filtered,
			Operation affectedOperation, List<Operation> allActions) {

		if (parentEntityFromPattern == null) {
			return true;
		}

		// ITree parentNodeFromAction =
		// affectedOperation.getAction().getNode().getParent();
		// TODO: here we navigate the hierarchy of spoon model instead of
		// gumtree

		CtElement parentNodeFromAction = affectedOperation.getNode().getParent();

		int i_levels = 1;

		while (parentNodeFromAction != null && i_levels <= parentLevel) {
			String type = getNodeLabelFromCtElement(parentNodeFromAction);
			// SpoonGumTreeBuilder.gtContext.getTypeLabel(parentNodeFromAction.getType());

			if (type != null && type.equals(parentEntityFromPattern.getEntityName())
			// Martin commented this
			// && !isNodeAffectedbyAction(allActions, parentNodeFromAction)
			) {
				i_levels = 1;
				parentLevel = parentEntityFromPattern.getParentLevel();
				parentEntityFromPattern = parentEntityFromPattern.getParent();

				if (parentEntityFromPattern == null) {
					return true;
				}

			} else {
				i_levels++;
			}
			parentNodeFromAction = parentNodeFromAction.getParent();
		}

		return false;

	}

	protected boolean isNodeAffectedbyAction(List<Action> actions, ITree node) {
		for (Action action : actions) {

			if (action.getNode().getId() == node.getId()) {
				return true;
			}
		}
		return false;
	}

}
