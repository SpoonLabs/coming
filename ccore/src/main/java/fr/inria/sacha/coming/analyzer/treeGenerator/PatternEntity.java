package fr.inria.sacha.coming.analyzer.treeGenerator;
/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternEntity {
	
	protected String entityName;
	protected String value;
	protected PatternEntity parent;
	protected int parentLevel;
	
	public static PatternEntity ANY_ENTITY = new PatternEntity("*");
	
	
	public PatternEntity(String entity) {
		super();
		this.entityName = entity;
	}	
	
	public PatternEntity(String entity,String value) {
		super();
		this.entityName = entity;
		this.value = value;
	}	
	
	public PatternEntity(String entity, PatternEntity parent, int level) {
		super();
		this.entityName = entity;
		this.parent = parent;
		this.parentLevel = level;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntity(String entity) {
		this.entityName = entity;
	}
	public PatternEntity getParent() {
		return parent;
	}
	
	public void setParent(PatternEntity parent,int level) {
		this.parent = parent;
		this.parentLevel = level;
	}
	
	public int getParentLevel() {
		return parentLevel;
	}


	@Override
	public String toString() {
		return "PatternEntity [entityName=" + entityName + ", parent=" + parent
				+ ", parentLevel=" + parentLevel + "]";
	}
	
	

}
