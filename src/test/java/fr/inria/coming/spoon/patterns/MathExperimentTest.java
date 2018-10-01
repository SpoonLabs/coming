package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.DetectorChangePatternInstanceEngine;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.core.engine.files.BugFixRunner;
import fr.inria.coming.main.ComingMain;
import fr.inria.main.CommandSummary;
import gumtree.spoon.diff.Diff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MathExperimentTest {

	@Test
	public void testMath4_2_Actions_1move() throws Exception {
		File fileWithPattern = new File(getClass().getResource("/pattern_INS_IF_MOVE_ASSIG.xml").getFile());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "/Users/matias/develop/commons-math/");
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
	public void testMath4_2_Actions_2Inserts() throws Exception {
		File fileWithPattern = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "/Users/matias/develop/commons-math/");
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

	@Test
	public void testPatternInstanceMath3() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_3/MathArrays_s.java");
		File t = getFile("casesDj4/math_3/MathArrays_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertEquals(1, diff.getRootOperations().size());

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath3_ADD_IF_BREAK() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_BRK.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_1/BigFraction_s.java");
		File t = getFile("casesDj4/math_1/BigFraction_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath4() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_4/SubLine_s.java");
		File t = getFile("casesDj4/math_4/SubLine_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertEquals(1, diff.getRootOperations().size());

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath53() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_53/Complex_s.java");
		File t = getFile("casesDj4/math_53/Complex_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertEquals(1, diff.getRootOperations().size());

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath60() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_60/NormalDistributionImpl_s.java");
		File t = getFile("casesDj4/math_60/NormalDistributionImpl_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertEquals(1, diff.getRootOperations().size());

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath84() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_84/MultiDirectional_s.java");
		File t = getFile("casesDj4/math_84/MultiDirectional_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath92_INS_IF_RET() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_92/MathUtils_s.java");
		File t = getFile("casesDj4/math_92/MathUtils_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath92_DEL_IF_THR() throws Exception {
		// The if is not remove, it removes the BinaryOperatorInside the fil
		File fl = new File(getClass().getResource("/pattern_DEL_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_92/MathUtils_s.java");
		File t = getFile("casesDj4/math_92/MathUtils_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertNoPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath93() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_93/MathUtils_s.java");
		File t = getFile("casesDj4/math_93/MathUtils_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);
		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath21() throws Exception {
		File fl = new File(getClass().getResource("/pattern_modif_if.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_21/RectangularCholeskyDecomposition_s.java");
		File t = getFile("casesDj4/math_21/RectangularCholeskyDecomposition_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath37_if_modif() throws Exception {
		File fl = new File(getClass().getResource("/pattern_modif_if.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_37/Complex_s.java");
		File t = getFile("casesDj4/math_37/Complex_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath37_if_return() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_37/Complex_s.java");
		File t = getFile("casesDj4/math_37/Complex_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath19_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_19/CMAESOptimizer_s.java");
		File t = getFile("casesDj4/math_19/CMAESOptimizer_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath25_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_25/HarmonicFitter_s.java");
		File t = getFile("casesDj4/math_25/HarmonicFitter_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath45_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_45/OpenMapRealMatrix_s.java");
		File t = getFile("casesDj4/math_45/OpenMapRealMatrix_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath48_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_48/BaseSecantSolver_s.java");
		File t = getFile("casesDj4/math_48/BaseSecantSolver_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath73_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_73/BrentSolver_s.java");
		File t = getFile("casesDj4/math_73/BrentSolver_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath86_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_86/CholeskyDecompositionImpl_s.java");
		File t = getFile("casesDj4/math_86/CholeskyDecompositionImpl_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);
		// See explanation
		assertNoPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath99_if_Tr() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_THR.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_99/MathUtils_s.java");
		File t = getFile("casesDj4/math_99/MathUtils_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath99_if_Assing() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_INS_ASSIG.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_102/ChiSquareTestImpl_s.java");
		File t = getFile("casesDj4/math_102/ChiSquareTestImpl_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath36_if_Assing() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_INS_ASSIG.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_36/BigFraction_s.java");
		File t = getFile("casesDj4/math_36/BigFraction_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath51_if_Assing() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_INS_ASSIG.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_51/BaseSecantSolver_s.java");
		File t = getFile("casesDj4/math_51/BaseSecantSolver_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath54_if_Assing() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_INS_ASSIG.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_54/Dfp_s.java");
		File t = getFile("casesDj4/math_54/Dfp_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath54_if_return() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_54/Dfp_s.java");
		File t = getFile("casesDj4/math_54/Dfp_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertNoPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath55_if_return() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_55/Vector3D_s.java");
		File t = getFile("casesDj4/math_55/Vector3D_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertNoPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath64_if_X2_anid() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_X2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_64/LevenbergMarquardtOptimizer_s.java");
		File t = getFile("casesDj4/math_64/LevenbergMarquardtOptimizer_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath64_DEL_IF_RET() throws Exception {
		File fl = new File(getClass().getResource("/pattern_DEL_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_64/LevenbergMarquardtOptimizer_s.java");
		File t = getFile("casesDj4/math_64/LevenbergMarquardtOptimizer_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		// SEE next test case
		assertNoPattern(diff, pattern);

	}

	@Test
	public void testPatternInstanceMath64_MOV_IF_RET() throws Exception {
		File fl = new File(getClass().getResource("/pattern_MOV_IF_RET.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_64/LevenbergMarquardtOptimizer_s.java");
		File t = getFile("casesDj4/math_64/LevenbergMarquardtOptimizer_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertNoPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath68_if_X2_anid() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_X2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_68/LevenbergMarquardtOptimizer_s.java");
		File t = getFile("casesDj4/math_68/LevenbergMarquardtOptimizer_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath78_if_X2_anid() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_X2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_78/EventState_s.java");
		File t = getFile("casesDj4/math_78/EventState_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath29_if_X2_anid() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_X2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_29/OpenMapRealVector_s.java");
		File t = getFile("casesDj4/math_29/OpenMapRealVector_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMath39_if_X2_anid() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_X2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_39/EmbeddedRungeKuttaIntegrator_s.java");
		File t = getFile("casesDj4/math_39/EmbeddedRungeKuttaIntegrator_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceMat95_if_mov_assig() throws Exception {
		File fl = new File(getClass().getResource("/pattern_INS_IF_MOVE_ASSIG.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_95/FDistributionImpl_s.java");
		File t = getFile("casesDj4/math_95/FDistributionImpl_t.java");

		BugFixRunner r = new BugFixRunner();
		Diff diff = r.getdiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);
	}

	public void assertPattern(Diff diffToAnalyze, ChangePatternSpecification pattern) {
		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		// ResultMapping mappings = detector.mappingActions(pattern, diffToAnalyze);
		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diffToAnalyze);
		assertTrue(instances.size() > 0);
		for (ChangePatternInstance changePatternInstance : instances) {
			System.out.println(changePatternInstance);
		}

	}

	public void assertNoPattern(Diff diffToAnalyze, ChangePatternSpecification pattern) {
		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diffToAnalyze);
		assertTrue(instances.isEmpty());
	}

	public File getFile(String name) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(name).getFile());
		return file;
	}
}
