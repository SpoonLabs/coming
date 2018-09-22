package fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
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
		// Get the filter elements according to Change type and affected entity
		List<Operation> filtered = super.process(diff);

		List<Operation> all = diff.getAllOperations();

		for (PatternAction patternaction : this.patternActions) {
			boolean keepsRelation = false;
			// For the others,
			for (Operation action : filtered) {
				keepsRelation |= checkparent(patternaction.getAffectedEntity().getParentPatternEntity().getParent(),
						patternaction.getAffectedEntity().getParentPatternEntity().getParentLevel(), filtered, action,
						all);

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
		// Let's get the parent of the affected
		CtElement parentNodeFromAction = affectedOperation.getNode().getParent();

		int i_levels = 1;
		// Scale the hierarchie and check types.
		while (parentNodeFromAction != null && i_levels <= parentLevel) {
			String type = getNodeLabelFromCtElement(parentNodeFromAction);

			if (type != null && type.equals(parentEntityFromPattern.getEntityType())) {
				i_levels = 1;
				parentLevel = parentEntityFromPattern.getParentPatternEntity().getParentLevel();
				parentEntityFromPattern = parentEntityFromPattern.getParentPatternEntity().getParent();

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
