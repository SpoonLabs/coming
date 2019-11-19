package fr.inria.coming.core.entities.output;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;

/**
 * 
 * @author Matias Martinez
 *
 */
public class StdOutput implements IOutput {
	
	Logger log = Logger.getLogger(StdOutput.class.getName());

	@Override
	public void generateFinalOutput(FinalResult finalResult) {
		log.debug(finalResult);
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed) {

	}

}
