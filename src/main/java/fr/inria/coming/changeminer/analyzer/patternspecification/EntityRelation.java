package fr.inria.coming.changeminer.analyzer.patternspecification;

/**
 * 
 * @author Matias Martinez
 *
 */
public class EntityRelation {

	private PatternEntity entity;
	private PatternAction action1;
	private PatternAction action2;

	public EntityRelation(PatternEntity entity, PatternAction action1, PatternAction action2) {
		super();
		this.entity = entity;
		this.action1 = action1;
		this.action2 = action2;
	}

	public PatternEntity getEntity() {
		return entity;
	}

	public void setEntity(PatternEntity entity) {
		this.entity = entity;
	}

	public PatternAction getAction1() {
		return action1;
	}

	public void setAction1(PatternAction action1) {
		this.action1 = action1;
	}

	public PatternAction getAction2() {
		return action2;
	}

	public void setAction2(PatternAction action2) {
		this.action2 = action2;
	}

	@Override
	public String toString() {
		return "EntityRelation [entity=" + entity + ", action1=" + action1 + ", action2=" + action2 + "]";
	}

}
