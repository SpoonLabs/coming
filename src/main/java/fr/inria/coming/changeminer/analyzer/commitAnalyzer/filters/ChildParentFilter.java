package fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

public class ChildParentFilter extends PatternFilter {

	public ChildParentFilter(List<PatternAction> pac) {
		super(pac);

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

		for (Operation operation : filtered) {
			Action action = operation.getAction();
			if (action instanceof Insert) {
				CtElement insertCtElement = operation.getNode();

				for (Operation acall : all) {
					if (acall.getAction() instanceof Move) {
						MoveOperation moveOp = (MoveOperation) acall;
						if (isParent(insertCtElement, moveOp.getNode().getParent())) {
							result.add(operation);
							break;
						}
					}

				}

			}

		}

		return result;
	}

	private boolean isParent(CtElement insert, CtElement m) {
		if (m == null)
			return false;
		if (insert == m)
			return true;
		else
			return isParent(insert, m.getParent());
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
