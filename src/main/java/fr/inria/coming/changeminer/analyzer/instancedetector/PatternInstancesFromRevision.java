package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import com.github.difflib.text.DiffRow;
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
	List<DiffRow> row_list = null;

	public PatternInstancesFromRevision(IRevision analyzed) {
		super(analyzed);

	}

	public PatternInstancesFromRevision(IRevision analyzed, List<PatternInstancesFromDiff> instances) {
		super(analyzed);
		this.infoPerDiff = instances;
	}

	public PatternInstancesFromRevision(IRevision analyzed, List<PatternInstancesFromDiff> instances, List<DiffRow> rowList) {
		super(analyzed);
		this.infoPerDiff = instances;
		this.row_list=rowList;
	}

	public List<DiffRow> getRow_list() {
		return row_list;
	}

	public void setRow_list(List<DiffRow> row_list) {
		this.row_list = row_list;
	}

	public List<PatternInstancesFromDiff> getInfoPerDiff() {
		return infoPerDiff;
	}

	public void setInstances(List<PatternInstancesFromDiff> instances) {
		this.infoPerDiff = instances;
	}

	@Override
	public String toString() {
		// return "PatternInstancesFromRevision [instances=" + instances + "]";
		String r = "";
		for (PatternInstancesFromDiff patternInstancesFromDiff : infoPerDiff) {
			r += "\n" + patternInstancesFromDiff.toString();
		}

		return r;
	}

}
