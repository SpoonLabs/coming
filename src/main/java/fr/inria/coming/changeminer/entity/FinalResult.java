package fr.inria.coming.changeminer.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;

/**
 * 
 * @author Matias Martinez
 *
 */
@SuppressWarnings("serial")
public class FinalResult<R extends IRevision> extends LinkedHashMap<R, RevisionResult> {

	Logger log = Logger.getLogger(CommitFinalResult.class.getName());

	public FinalResult() {
		super();
	}

	public Map<R, RevisionResult> getAllResults() {
		return this;
	}

	public String toString() {
		String r = "";

		for (R revision : this.keySet()) {

			r += "\n" + ("" + revision.toString());
			RevisionResult rv = this.get(revision);

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
