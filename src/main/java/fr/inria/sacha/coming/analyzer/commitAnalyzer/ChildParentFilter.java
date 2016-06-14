package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.util.ArrayList;
import java.util.List;

import fr.inria.sacha.coming.analyzer.DiffResult;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternAction;
import fr.labri.gumtree.actions.model.Action;
import fr.labri.gumtree.actions.model.Insert;
import fr.labri.gumtree.actions.model.Move;
import fr.labri.gumtree.tree.Tree;

public class ChildParentFilter extends PatternFilter {

	public ChildParentFilter(List<PatternAction> pac) {
		super(pac);

	}

	/**
	 * Filtre changes according to the pattern specification
	 */

	public List<Action> processold(DiffResult diff) {

		List<Action> result = new ArrayList<Action>();

		List<Action> filtered = super.process(diff);

		List<Action> all = diff.getAllFilterDuplicate(diff.getAllActions());

		for (Action action : filtered) {

			Tree affected = action.getNode();

			boolean childAffected = searchChildAffected(all, affected);

			if (!childAffected) {
				result.add(action);
			}

		}

		return result;
	}

	@Override
	public List<Action> process(DiffResult diff) {

		List<Action> result = new ArrayList<Action>();

		List<Action> filtered = super.process(diff);

		List<Action> all = diff.getAllFilterDuplicate(diff.getAllActions());

		for (Action action : filtered) {

			if (action instanceof Insert) {
				Tree insert = action.getNode();

				for (Action acall : all) {
					if (acall instanceof Move) {
						Move m = (Move) acall;
						if (isParent(insert, m.getParent())) {
							result.add(action);
							break;
						}
					}

				}

			}

		}

		return result;
	}

	private boolean isParent(Tree insert, Tree m) {
		if (m == null)
			return false;
		if (insert == m)
			return true;
		else
			return isParent(insert, m.getParent());
	}

	private boolean searchChildAffected(List<Action> all, Tree affected) {

		boolean childAffected = false;

		for (Tree child : affected.getChildren()) {

			if (child.getTypeLabel().equals("THEN_STATEMENT")) {
				childAffected |= searchChildAffected(all, child);

			} else {
				childAffected |= isNodeAffectedbyAction(all, child);
			}
		}
		return childAffected;
	}

}
