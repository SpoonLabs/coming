package fr.inria.coming.core.extensionpoints.changepattern;

import java.io.File;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface PatternFileParser {
	public ChangePatternSpecification parse(File patternFile);
}
