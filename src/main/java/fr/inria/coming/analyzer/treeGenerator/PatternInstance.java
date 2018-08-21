package fr.inria.coming.analyzer.treeGenerator;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;
/**
 * 
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */
public class PatternInstance {

	List<Action> actions; 
	PatternSpecification relatedPattern;
	//Map<PatternAction, >
	public PatternInstance(List<Action> actions, PatternSpecification relatedPattern) {
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

	public PatternSpecification getRelatedPattern() {
		return relatedPattern;
	}

	public void setRelatedPattern(PatternSpecification relatedPattern) {
		this.relatedPattern = relatedPattern;
	}
	
	
	
}
