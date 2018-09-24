package fr.inria.coming.changeminer.analyzer.instancedetector;

import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MatchingEntity {

	protected CtElement affectedNode;
	protected PatternEntity patternEntity;

	public MatchingEntity(CtElement currentNodeFromAction, PatternEntity patternEntity) {
		this.affectedNode = currentNodeFromAction;
		this.patternEntity = patternEntity;
	}

	public CtElement getAffectedNode() {
		return affectedNode;
	}

	public void setAffectedNode(CtElement affectedNode) {
		this.affectedNode = affectedNode;
	}

	public PatternEntity getPatternEntity() {
		return patternEntity;
	}

	public void setPatternEntity(PatternEntity parentEntity) {
		this.patternEntity = parentEntity;
	}

}
