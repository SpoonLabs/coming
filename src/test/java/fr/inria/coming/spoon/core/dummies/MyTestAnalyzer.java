package fr.inria.coming.spoon.core.dummies;

import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MyTestAnalyzer implements Analyzer<Commit> {

	@Override
	public AnalysisResult analyze(Commit input, RevisionResult previousResults) {
		System.out.println("MyAnalyzer");
		return null;
	}

}
