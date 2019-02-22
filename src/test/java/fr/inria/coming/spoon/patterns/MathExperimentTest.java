package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.CommandSummary;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MathExperimentTest {

	@Test
	@Ignore
	public void testMath4_2_Actions_1move() throws Exception {
		File fileWithPattern = new File(getClass().getResource("/pattern_INS_IF_MOVE_ASSIG.xml").getFile());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", ComingProperties.getProperty("git_commons_math_location"));
		cs.append("-mode", "mineinstance");
		cs.append("-filter", "keywords:withtest");
		cs.append("-filtervalue", "[MATH-");

		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();

		// 2bc9e7ea86a8cc1c3b2436c46115c87080228004
	}

	@Test
	@Ignore
	public void testMath4_2_Actions_1move_max() throws Exception {
		File fileWithPattern = new File(getClass().getResource("/pattern_INS_IF_MOVE_ASSIG.xml").getFile());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", ComingProperties.getProperty("git_commons_math_location"));
		cs.append("-mode", "mineinstance");
		cs.append("-filter", "keywords:withtest");
		cs.append("-filtervalue", "[MATH-");

		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		cs.command.put("-parameters", "maxrevision:10");
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();

		// 2bc9e7ea86a8cc1c3b2436c46115c87080228004
	}

	@Test
	@Ignore
	public void testMath4_2_Actions_2Inserts() throws Exception {
		File fileWithPattern = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", ComingProperties.getProperty("git_commons_math_location"));
		cs.append("-mode", "mineinstance");
		// cs.append("-filter", "keywords:withtest");
		cs.append("-filter", "keywords");
		cs.append("-filtervalue", "MATH");

		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();

		// 2bc9e7ea86a8cc1c3b2436c46115c87080228004
	}

}
