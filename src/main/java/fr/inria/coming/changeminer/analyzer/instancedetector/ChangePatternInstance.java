package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import fr.inria.coming.changeminer.analyzer.treeGenerator.ChangePatternSpecification;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */

public class ChangePatternInstance {

	protected List<Operation> actions;
	protected ChangePatternSpecification pattern;

	public ChangePatternInstance(List<Operation> actions, ChangePatternSpecification relatedPattern) {
		super();
		this.actions = actions;
		this.pattern = relatedPattern;
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

}
