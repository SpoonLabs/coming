package fr.inria.coming.changeminer.entity;

import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CommitFinalResult extends FinalResult {

	Map<Commit, RevisionResult> allResults;
	Logger log = Logger.getLogger(CommitFinalResult.class.getName());

	public CommitFinalResult(Map<Commit, RevisionResult> allResults) {
		super();
		this.allResults = allResults;
	}

	public Map<Commit, RevisionResult> getAllResults() {
		return allResults;
	}

	public String toString() {
		String r = "";

		for (Commit commit : allResults.keySet()) {

			r += "\n" + ("" + commit.getName());
			RevisionResult rv = allResults.get(commit);

			for (String processorName : rv.keySet()) {

				r += "\n" + processorName;
				AnalysisResult result = rv.get(processorName);
				r += "\n" + result;
			}
		}
		return r;
	}

}
