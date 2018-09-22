package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;

import fr.inria.coming.changeminer.analyzer.treeGenerator.ChangePatternSpecification;

/**
 * 
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */
@Deprecated
public class PatternInstance {

	List<Action> actions;
	ChangePatternSpecification relatedPattern;

	// Map<PatternAction, >
	public PatternInstance(List<Action> actions, ChangePatternSpecification relatedPattern) {
		super();
		this.actions = actions;
		this.relatedPattern = relatedPattern;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public ChangePatternSpecification getRelatedPattern() {
		return relatedPattern;
	}

	public void setRelatedPattern(ChangePatternSpecification relatedPattern) {
		this.relatedPattern = relatedPattern;
	}

}
