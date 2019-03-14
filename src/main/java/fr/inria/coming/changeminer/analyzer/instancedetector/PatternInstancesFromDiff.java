package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import gumtree.spoon.diff.Diff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternInstancesFromDiff extends AnalysisResult<IRevision> {

	protected List<ChangePatternInstance> instances = null;
	protected Diff diff = null;

	public PatternInstancesFromDiff(IRevision analyzed) {
		super(analyzed);
	}

	public PatternInstancesFromDiff(IRevision analyzed, List<ChangePatternInstance> instances, Diff diff) {
		super(analyzed);
		this.instances = instances;
		this.diff = diff;
	}

	@Override
	public String toString() {
		try {
			if (diff == null) {
				System.err.println("Diff null");
				return "--Diff null--";
			}
			String diffString = "";

			try {
				diffString = diff.toString();

			} catch (Exception e) {
				System.err.println("Error when printing diff result: " + e.getMessage());

				e.printStackTrace();
				diffString = "wrong diff";
			}
			String resultString = "\n----For Diff:" + diffString + "\n: number instances found: " + instances.size();
			for (ChangePatternInstance instance : instances) {
				resultString += "\n" + instance.toString();
			}
			return resultString += "\n----";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "--Diff ex--";
	}

	public List<ChangePatternInstance> getInstances() {
		return instances;
	}

	public void setInstances(List<ChangePatternInstance> instances) {
		this.instances = instances;
	}

	public Diff getDiff() {
		return diff;
	}

	public void setDiff(Diff diff) {
		this.diff = diff;
	}

}
