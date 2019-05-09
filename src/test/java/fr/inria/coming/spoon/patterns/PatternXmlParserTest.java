package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.util.PatternXMLParser;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternXmlParserTest {

	@Test
	public void testNamePatternWithName1() {
		// This pattern was a name
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		assertEquals("Modif_If_Child", pattern.getName());

	}

	@Test
	public void testNamePatternWithoutName1() {
		// This pattern was a name
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if_noname.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		assertEquals("pattern_modif_if_noname", pattern.getName());

	}
}
