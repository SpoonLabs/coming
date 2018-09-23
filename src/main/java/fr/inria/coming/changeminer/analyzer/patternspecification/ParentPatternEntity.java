package fr.inria.coming.changeminer.analyzer.patternspecification;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ParentPatternEntity {

	protected PatternEntity parent;

	protected int parentLevel;

	public ParentPatternEntity(PatternEntity parent, int parentLevel) {
		super();
		this.parent = parent;
		this.parentLevel = parentLevel;
	}

	public ParentPatternEntity() {
	}

	public int getParentLevel() {
		return parentLevel;
	}

	public void setParentLevel(int parentLevel) {
		this.parentLevel = parentLevel;
	}

	public PatternEntity getParent() {
		return parent;
	}

	public void setParent(PatternEntity parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "ParentPatternEntity [parent=" + parent + ", parentLevel=" + parentLevel + "]";
	}
}