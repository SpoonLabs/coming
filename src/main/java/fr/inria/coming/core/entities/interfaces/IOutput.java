package fr.inria.coming.core.entities.interfaces;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.entities.RevisionResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface IOutput {
	/**
	 * Compute output for the final results
	 * 
	 * @param finalResult
	 */
	public void generateFinalOutput(FinalResult finalResult);

	/**
	 * Compute the outputs for the results of a revision
	 * 
	 * @param finalResult
	 */
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed);

}
