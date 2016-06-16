package fr.inria.sacha.coming.analyzer;

import java.io.File;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.spoon.diffSpoon.CtDiff;
import fr.inria.sacha.spoon.diffSpoon.DiffSpoonImpl;

/**
 * Facade for GumTree functionality.
 * Fine granularity comparison between two files according to a given
 * granularity (JDT, CD, Spoon). 
 * It uses GT Matching algorithm.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */

public class DiffEngineFacade {

	private Logger log = Logger.getLogger(DiffEngineFacade.class.getName());

	public CtDiff compareContent(String contentL, String contentR,
			GranuralityType granularity) throws Exception {
		

			DiffSpoonImpl diffEngine = new DiffSpoonImpl();
		
			return  diffEngine.compare(contentL,contentR);

	};
	
	public CtDiff compareFiles(File contentL, File contentR,
			GranuralityType granularity) throws Exception {
		

			DiffSpoonImpl diffEngine = new DiffSpoonImpl();
			return  diffEngine.compare(contentL,contentR);

	};
	

	
}
