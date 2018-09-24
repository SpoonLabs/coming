package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import fr.inria.astor.util.MapList;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResultMapping {
	// Mapping between pattern and a tree
	MapList<PatternAction, MatchingAction> mappings;
	// Elements from the pattern not matched
	List<PatternAction> notMapped;

	public ResultMapping(MapList<PatternAction, MatchingAction> mappings, List<PatternAction> notMapped) {
		super();
		this.mappings = mappings;
		this.notMapped = notMapped;
	}

	public MapList<PatternAction, MatchingAction> getMappings() {
		return mappings;
	}

	public void setMappings(MapList<PatternAction, MatchingAction> mappings) {
		this.mappings = mappings;
	}

	public List<PatternAction> getNotMapped() {
		return notMapped;
	}

	public void setNotMapped(List<PatternAction> notMapped) {
		this.notMapped = notMapped;
	}

}
