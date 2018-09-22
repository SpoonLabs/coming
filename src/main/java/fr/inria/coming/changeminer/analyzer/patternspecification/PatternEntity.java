package fr.inria.coming.changeminer.analyzer.patternspecification;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternEntity {

	protected String entityName;
	protected String value;
	protected ParentPatternEntity parent = new ParentPatternEntity();

	public final static PatternEntity ANY_ENTITY = new PatternEntity("*");

	public PatternEntity(String entity) {
		super();
		this.entityName = entity;
	}

	public PatternEntity(String entity, String value) {
		super();
		this.entityName = entity;
		this.value = value;
	}

	public PatternEntity(String entity, ParentPatternEntity data) {
		super();
		this.entityName = entity;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntity(String entity) {
		this.entityName = entity;
	}

	public PatternEntity getParent() {
		return parent.parent;
	}

	public void setParent(PatternEntity parent, int level) {
		this.parent.parent = parent;
		this.parent.parentLevel = level;
	}

	public int getParentLevel() {
		return parent.parentLevel;
	}

	@Override
	public String toString() {
		return "PatternEntity [entityName=" + entityName + ", parent=" + parent.parent + ", parentLevel="
				+ parent.parentLevel + "]";
	}

}
