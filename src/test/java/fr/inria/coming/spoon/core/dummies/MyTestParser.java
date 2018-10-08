package fr.inria.coming.spoon.core.dummies;

import java.io.File;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.core.extensionpoints.changepattern.PatternFileParser;

public class MyTestParser implements PatternFileParser {

	@Override
	public ChangePatternSpecification parse(File patternFile) {
		System.out.println("Test pattern parser");
		return null;
	}

}
