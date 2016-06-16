package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.util.ArrayList;
import java.util.List;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternAction;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.spoon.diffSpoon.CtDiff;
import fr.inria.sacha.spoon.diffSpoon.SpoonGumTreeBuilder;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;

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
	@Override
	public List<Action> process(CtDiff  diff) {

		List<Action> result = new ArrayList<Action>();

		List<Action> filtered = super.process(diff);

		List<Action> all = diff.getAllActions();

		for (PatternAction patternaction : this.patternActions) {
			boolean keepsRelation = false;
			for (Action action : filtered) {
				keepsRelation |= checkparent(patternaction.getAffectedEntity().getParent(), patternaction
						.getAffectedEntity().getParentLevel(), filtered, action, all);

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
	protected boolean checkparent(PatternEntity parentEntityFromPattern, int parentLevel, List<Action> filtered,
			Action affectedAction, List<Action> allActions) {

		if (parentEntityFromPattern == null) {
			return true;
		}

		ITree parentNodeFromAction = affectedAction.getNode().getParent();
		int i_levels = 1;

		while (parentNodeFromAction != null && i_levels <= parentLevel) {
			String type = SpoonGumTreeBuilder.gtContext.getTypeLabel(parentNodeFromAction.getType());
			if (type != null && type.equals(parentEntityFromPattern.getEntityName())
					// Martin commented this
					//&& !isNodeAffectedbyAction(allActions, parentNodeFromAction)
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
