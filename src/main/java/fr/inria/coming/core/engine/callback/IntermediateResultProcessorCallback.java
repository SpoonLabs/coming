package fr.inria.coming.core.engine.callback;

import fr.inria.coming.core.entities.RevisionResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface IntermediateResultProcessorCallback {

	public void handleResult(RevisionResult result);

}
