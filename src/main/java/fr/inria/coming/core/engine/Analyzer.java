package fr.inria.coming.core.engine;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface Analyzer<T extends IRevision> {

	/**
	 * Analyze the input and return the results
	 * 
	 * @param input           input to be analyzer
	 * @param previousResults results of previous analysis that can be used in case
	 *                        of doing a chain of analysis
	 * @return result of the analysis
	 */
	public AnalysisResult analyze(T input, RevisionResult previousResults);

	/**
	 * Returns the Key (identfier) of the Analyzer. By default is the class name.
	 * 
	 * @return
	 */
	public default String key() {
		return this.getClass().getSimpleName();
	}

}
