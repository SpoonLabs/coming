package fr.inria.coming.changeminer.entity;

import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FinalResult<R extends IRevision> {

	Map<R, RevisionResult> allResults;
	Logger log = Logger.getLogger(CommitFinalResult.class.getName());

	public FinalResult(Map<R, RevisionResult> allResults) {
		super();
		this.allResults = allResults;
	}

	public Map<R, RevisionResult> getAllResults() {
		return allResults;
	}

	public String toString() {
		String r = "";

		for (R revision : allResults.keySet()) {

			r += "\n" + ("" + revision.toString());
			RevisionResult rv = allResults.get(revision);

			for (String processorName : rv.keySet()) {

				r += "\n" + processorName;
				AnalysisResult result = rv.get(processorName);
				try {
					r += "\n" + result;
				} catch (Exception e) {
					r += "\n -Error on toString-";
					e.printStackTrace();
				}
			}
		}
		return r;
	}
}
