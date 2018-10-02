package fr.inria.coming.core.entities.output;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.entities.interfaces.IOutput;

/**
 * 
 * @author Matias Martinez
 *
 */
public class StdOutput implements IOutput {

	@Override
	public void generateOutput(FinalResult finalResult) {
		System.out.println(finalResult);

	}

}
