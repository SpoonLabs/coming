package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MatchingAction {
	/**
	 * The operation from the diff
	 */
	private Operation operation;
	/**
	 * the action from the pattern mapped to operation
	 */
	private PatternAction patternAction;
	/**
	 * A list with all entities mapped (affected entity and all its parents)
	 */
	private List<MatchingEntity> matching;

	public MatchingAction(Operation operation, PatternAction patternAction, List<MatchingEntity> matching) {
		this.operation = operation;
		this.patternAction = patternAction;
		this.matching = matching;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public PatternAction getPatternAction() {
		return patternAction;
	}

	public void setPatternAction(PatternAction patternAction) {
		this.patternAction = patternAction;
	}

	public List<MatchingEntity> getMatching() {
		return matching;
	}

	public void setMatching(List<MatchingEntity> matching) {
		this.matching = matching;
	}

}
