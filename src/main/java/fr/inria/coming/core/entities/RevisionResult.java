package fr.inria.coming.core.entities;

import java.util.HashMap;

import fr.inria.coming.core.engine.Analyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RevisionResult extends HashMap<String, AnalysisResult> {

	public AnalysisResult getResultFromClass(Class _class) {
		return this.get(_class.getSimpleName());
	}

	public AnalysisResult putResultFromClass(Class _class, AnalysisResult result) {
		return this.put(_class.getSimpleName(), result);
	}

	public AnalysisResult putResultFromClass(Analyzer analyzer, AnalysisResult result) {
		return this.put(analyzer.getClass().getSimpleName(), result);
	}
}
