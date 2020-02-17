package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */

public class ChangePatternInstance {
	/**
	 * The pattern that this instance belongs
	 */
	protected ChangePatternSpecification pattern;
	/**
	 * The mapping between all actions and operators from the diff
	 */
	protected Map<PatternAction, Operation> actionOperation;
	protected List<Operation> actions;
	/**
	 * The mapping of the elements
	 */
	protected Map<PatternAction, MatchingAction> mapping = new HashMap<>();

	public ChangePatternInstance(ChangePatternSpecification relatedPattern) {
		super();
		this.actions = new ArrayList();
		this.actionOperation = new HashMap();
		this.pattern = relatedPattern;
	}

	public ChangePatternInstance(List<Operation> actions, ChangePatternSpecification relatedPattern) {
		super();
		this.actions = actions;
		this.pattern = relatedPattern;
		this.actionOperation = new HashMap();
	}

	public void addInstance(PatternAction pa, Operation op) {
		this.actions.add(op);
		this.actionOperation.put(pa, op);
	}

	public List<Operation> getActions() {
		return actions;
	}

	public void setActions(List<Operation> actions) {
		this.actions = actions;
	}

	public ChangePatternSpecification getRelatedPattern() {
		return pattern;
	}

	public void setRelatedPattern(ChangePatternSpecification relatedPattern) {
		this.pattern = relatedPattern;
	}

	public Map<PatternAction, Operation> getActionOperation() {
		return actionOperation;
	}

	public void setActionOperation(Map<PatternAction, Operation> actionOperation) {
		this.actionOperation = actionOperation;
	}

	public ChangePatternSpecification getPattern() {
		return pattern;
	}

	public void setPattern(ChangePatternSpecification pattern) {
		this.pattern = pattern;
	}

	@Override
	public String toString() {
//		try {
		return "ChangePatternInstance [actions=" + actions + "]";
//		} catch (Exception e) {
//			return "";
//		}
	}

	public Map<PatternAction, MatchingAction> getMapping() {
		return mapping;
	}

	public void setMapping(Map<PatternAction, MatchingAction> actionMatchAction) {
		this.mapping = actionMatchAction;
	}

}
