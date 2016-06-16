package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.util.ArrayList;
import java.util.List;

import fr.inria.sacha.coming.analyzer.treeGenerator.PatternAction;
import fr.inria.sacha.spoon.diffSpoon.CtDiff;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;

public class ChildParentFilter extends PatternFilter {

	public ChildParentFilter(List<PatternAction> pac) {
		super(pac);

	}

	/**
	 * Filtre changes according to the pattern specification
	 */


	@Override
	public List<Action> process(CtDiff  diff) {

		List<Action> result = new ArrayList<Action>();

		List<Action> filtered = super.process(diff);

		List<Action> all = diff.getAllActions();//Matias, I dont remerber why we filter //diff.getAllFilterDuplicate(diff.getAllActions());

		for (Action action : filtered) {

			if (action instanceof Insert) {
				ITree insert = action.getNode();

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

	private boolean isParent(ITree insert, ITree m) {
		if (m == null)
			return false;
		if (insert == m)
			return true;
		else
			return isParent(insert, m.getParent());
	}



}
