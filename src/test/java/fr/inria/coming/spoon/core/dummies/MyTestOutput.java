package fr.inria.coming.spoon.core.dummies;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;

public class MyTestOutput implements IOutput {

	@Override
	public void generateFinalOutput(FinalResult finalResult) {
		System.out.println("My output");
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed) {
		System.out.println("My output");
	}

}
