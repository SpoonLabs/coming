package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;


/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternInstancesFromRevision extends AnalysisResult<IRevision> {

	/**
	 * Stores the information of the Diff.
	 */
	protected List<PatternInstancesFromDiff> infoPerDiff = null;

	public PatternInstancesFromRevision(IRevision analyzed) {
		super(analyzed);

	}

	public PatternInstancesFromRevision(IRevision analyzed, List<PatternInstancesFromDiff> instances) {
		super(analyzed);
		this.infoPerDiff = instances;
	}

	public List<PatternInstancesFromDiff> getInfoPerDiff() {
		return infoPerDiff;
	}

	public void setInstances(List<PatternInstancesFromDiff> instances) {
		this.infoPerDiff = instances;
	}

//	@Override
//	public String toString() {
//		// return "PatternInstancesFromRevision [instances=" + instances + "]";
//		String r = "";
//		for (PatternInstancesFromDiff patternInstancesFromDiff : infoPerDiff) {
//			r += "\n" + patternInstancesFromDiff.toString();
//		}
//
//		return r;
//	}

}
