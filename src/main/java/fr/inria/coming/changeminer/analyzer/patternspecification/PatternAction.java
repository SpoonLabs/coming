package fr.inria.coming.changeminer.analyzer.patternspecification;

import fr.inria.coming.changeminer.entity.ActionType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternAction {
	
	PatternEntity affectedEntity;
	ActionType action;
	
	public PatternAction(PatternEntity affectedEntity, ActionType action) {
		super();
		this.affectedEntity = affectedEntity;
		this.action = action;
	}

	@Override
	public String toString() {
		return "PatternAction [affectedEntity=" + affectedEntity + ", action="
				+ action + "]";
	}

	public PatternEntity getAffectedEntity() {
		return affectedEntity;
	}

	public ActionType getAction() {
		return action;
	}
	
	
	

}
