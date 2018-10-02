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

	public AnalysisResult analyze(T input);

	public AnalysisResult analyze(T input, RevisionResult previousResults);

}
