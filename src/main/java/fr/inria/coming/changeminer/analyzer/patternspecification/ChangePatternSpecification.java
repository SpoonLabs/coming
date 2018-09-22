package fr.inria.coming.changeminer.analyzer.patternspecification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification of a Pattern
 * 
 * @author Matias Martinez
 *
 */
public class ChangePatternSpecification {

	private String name;
	private List<PatternAction> changes;

	public String getName() {
		return name;
	}

	public List<PatternAction> getChanges() {
		return changes;
	}

	public ChangePatternSpecification() {
		changes = new ArrayList<PatternAction>();
	}

	public ChangePatternSpecification(String name) {
		this.name = name;
		changes = new ArrayList<PatternAction>();
	}

	public ChangePatternSpecification(List<PatternAction> changes) {

		this.changes = new ArrayList<PatternAction>();
	}

	public void addChange(PatternAction pa) {
		this.changes.add(pa);
	}

	@Override
	public String toString() {
		return "ChangePattern [" + ((name == null) ? "" : "name=") + name + ", changes=" + changes + "]";
	}

}
