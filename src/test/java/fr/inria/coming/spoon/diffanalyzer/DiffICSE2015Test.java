package fr.inria.coming.spoon.diffanalyzer;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.json.simple.JSONArray;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.core.engine.files.DiffICSE15ContextAnalyzer;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapCounter;

@Ignore
public class DiffICSE2015Test {

	@Test
	public void testICSE2015() throws Exception {
		DiffICSE15ContextAnalyzer analyzer = new DiffICSE15ContextAnalyzer();
		analyzer.run(ComingProperties.getProperty("icse15difffolder"));
	}

	@Test
	public void testFailingTimeoutCase_584756() throws Exception {
		String diffId = "584756";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_1421510() throws Exception {

		String diffId = "1421510";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_613948() throws Exception {

		String diffId = "613948";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_1305909() throws Exception {

		String diffId = "1305909";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_985877() throws Exception {

		String diffId = "985877";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingTimeoutCase_932564() throws Exception {

		String diffId = "932564";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testFailingCase_1103681() throws Exception {
		// To see

		String diffId = "1103681";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testNoChangesCaseCase_1329010() throws Exception {

		String diffId = "1329010";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testChangesCaseCase_1185675() throws Exception {

		String diffId = "1185675";

		runAndAssertSingleDiff(diffId);
	}

	@Test
	public void testNoChangesCase_1381711() throws Exception {

		String diffId = "1381711";

		runAndAssertSingleDiff(diffId);
	}

	public void runAndAssertSingleDiff(String case1421510) {
		DiffICSE15ContextAnalyzer analyzer = new DiffICSE15ContextAnalyzer();

		File fileDiff = new File(ComingProperties.getProperty("icse15difffolder") + "/" + case1421510);
		JSONArray arrayout = analyzer.processDiff(new MapCounter<>(), new MapCounter<>(), fileDiff);

		assertTrue(arrayout.size() > 0);
		System.out.println(arrayout);
	}

}
