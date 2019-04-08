package fr.inria.coming.changeminer.analyzer.patternspecification;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternEntity {

	public final static String ANY = "*";
	protected int id;
	protected String entityType = ANY;
	protected String newValue = null;
	protected String oldValue = null;
	protected String roleInParent = ANY;

	protected ParentPatternEntity parent = null;

	public final static PatternEntity ANY_ENTITY = new PatternEntity("*");

	public PatternEntity(String entityType) {
		super();
		this.entityType = entityType;
		this.newValue = ANY;
		this.oldValue = ANY;
	}

	public PatternEntity(String entity, String newValue) {
		super();
		this.entityType = entity;
		this.newValue = newValue;
		this.oldValue = null;
	}

	public PatternEntity(String entity, String newValue, String oldValue) {
		super();
		this.entityType = entity;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public PatternEntity(String entityType, ParentPatternEntity parent) {
		super();
		this.entityType = entityType;
		this.parent = parent;
		this.newValue = ANY;
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

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String value) {
		this.newValue = value;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRoleInParent() {
		return roleInParent;
	}

	public void setRoleInParent(String roleInParent) {
		this.roleInParent = roleInParent;
	}

}
