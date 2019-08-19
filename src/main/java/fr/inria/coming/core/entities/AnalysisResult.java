package fr.inria.coming.core.entities;

import java.util.List;

/**
 * Store the results from the analysis of one revision
 * 
 * @author Matias Martinez
 *
 */
public class AnalysisResult<T> {

	protected T analyzed;

    public AnalysisResult(T analyzed) {
		this.analyzed = analyzed;
	};

	public Boolean sucessful() {
		return true;
	}

	public T getAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(T analyzed) {
		this.analyzed = analyzed;
	}

}
