package fr.inria.coming.changeminer.analyzer.patternspecification;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternEntity {

	protected String entityType = null;
	protected String value = null;
	protected ParentPatternEntity parent = null;

	public final static PatternEntity ANY_ENTITY = new PatternEntity("*");

	public PatternEntity(String entityType) {
		super();
		this.entityType = entityType;
	}

	public PatternEntity(String entity, String value) {
		super();
		this.entityType = entity;
		this.value = value;
	}

	public PatternEntity(String entity, ParentPatternEntity data) {
		super();
		this.entityType = entity;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entity) {
		this.entityType = entity;
	}

	public ParentPatternEntity getParentPatternEntity() {
		return parent;
	}

	public void setParent(PatternEntity parent, int level) {
		this.parent = new ParentPatternEntity(parent, level);

	}

	@Override
	public String toString() {
		return "PatternEntity [entityName=" + entityType + ", parent=" + parent + "]";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
