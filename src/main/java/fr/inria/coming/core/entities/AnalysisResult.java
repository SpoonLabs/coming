package fr.inria.coming.core.entities;

import java.util.List;

/**
 * Store the results from the analysis of one revision
 * 
 * @author Matias Martinez
 *
 */
public class AnalysisResult<T,K> {

	protected T analyzed;

	public List<K> getRow_list() {
		return row_list;
	}

	public void setRow_list(List<K> row_list) {
		this.row_list = row_list;
	}

	public List<K> row_list;

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
