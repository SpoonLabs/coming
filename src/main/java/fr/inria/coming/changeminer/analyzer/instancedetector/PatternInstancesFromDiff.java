package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.List;

import com.github.difflib.text.DiffRow;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import gumtree.spoon.diff.Diff;
import org.apache.log4j.Logger;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternInstancesFromDiff extends AnalysisResult<IRevision> {

	protected List<ChangePatternInstance> instances = null;
	protected Diff diff = null;
	protected String location = null;

    Logger log = Logger.getLogger(this.getClass().getName());

	public PatternInstancesFromDiff(IRevision analyzed) {
		super(analyzed);
	}

	public PatternInstancesFromDiff(IRevision analyzed, List<ChangePatternInstance> instances, Diff diff) {
		super(analyzed);
		this.instances = instances;
		this.diff = diff;
	}

	public PatternInstancesFromDiff(IRevision analyzed, List<ChangePatternInstance> instances, Diff diff,
			String location) {
		super(analyzed);
		this.instances = instances;
		this.diff = diff;
		this.location = location;
	}


	@Override
	public String toString() {
		try {
			if (diff == null) {
			    log.error("Diff null");
//				System.err.println("Diff null");
				return "--Diff null--";
			}
			String diffString = "";

			try {
				diffString = diff.toString();

			} catch (Exception e) {
                log.error("Error when printing diff result: " + e.getMessage());
//				System.err.println("Error when printing diff result: " + e.getMessage());

				e.printStackTrace();
				diffString = "wrong diff";
			}
			String resultString = "\n----For Diff:" + diffString + "\n: number instances found: " + instances.size();
			for (ChangePatternInstance instance : instances) {
				resultString += "\n" + instance.toString();
			}
			return resultString;
		} catch (Exception e) {
			if (!(e instanceof RuntimeException)) throw new RuntimeException(e);
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
