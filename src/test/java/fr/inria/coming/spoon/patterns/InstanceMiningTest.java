package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.utils.CommandSummary;
import gumtree.spoon.diff.Diff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class InstanceMiningTest {

	@Test
	public void testMain1() throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");

		FinalResult fr = main.run(cs.flat());

		assertNotNull(fr);
	}

	@Test
	public void testMainInputArg() throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-input", "git");

		FinalResult fr = main.run(cs.flat());

		assertNotNull(fr);

		try {
			cs.command.put("-input", "vvvvv");
			fr = main.run(cs.flat());
			fail();
		} catch (Throwable t) {
			// expected
		}
	}

	@Test
	public void testMainModeArg() throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "diff");

		FinalResult fr = main.run(cs.flat());

		assertNotNull(fr);

		try {
			cs.command.put("-mode", "vvvvv");
			fr = main.run(cs.flat());
			fail();
		} catch (Throwable t) {
			// expected
		}
	}

	@Test
	public void testMainPatternArguments() throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		FinalResult fr = null;
		try {
			System.out.println("Case 1 missing action");
			fr = main.run(cs.flat());

			assertFalse(true);
		} catch (Throwable t) {
			// expected missing action

		}

		cs.command.put("-action", ActionType.INS.toString());

		try {
			System.out.println("Case 2 missing entity");
			fr = main.run(cs.flat());

			assertFalse(true);
		} catch (Throwable t) {
			// expected

		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMainDiff() throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "diff");
		FinalResult finalResult = null;

		cs.command.put("-action", ActionType.ANY.toString());
		cs.command.put("-entitytype", PatternEntity.ANY.toString());

		finalResult = main.run(cs.flat());

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;
		System.out.println("FinalResults: \n" + finalResult);
		for (Commit commit : commitResult.getAllResults().keySet()) {

			DiffResult<Commit, Diff> diffs = (DiffResult<Commit, Diff>) commitResult.getAllResults().get(commit)
					.getResultFromClass(FineGrainDifftAnalyzer.class);
			assertNotNull(diffs);

			if ("60b54977abe45f662daaa80ebfdf63ab4fe3a9b2".equals(commit.getName())
					|| "ab71649c481971a9ad54f04797f5fd9cb133789b".equals(commit.getName())) {
				// firsts commits, no changes, file introduction
				assertTrue(diffs.getAll().isEmpty());
			} else
				assertTrue("Not changes at: " + commit.getName(), diffs.getAll().size() > 0);

			for (Diff diff : diffs.getAll()) {
				assertTrue(diff.getRootOperations().size() > 0);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMainPattern1Any() throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		FinalResult finalResult = null;

		cs.command.put("-action", ActionType.ANY.toString());
		cs.command.put("-entitytype", PatternEntity.ANY.toString());

		finalResult = main.run(cs.flat());

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;
		System.out.println("FinalResults: \n" + finalResult);
		for (Commit commit : commitResult.getAllResults().keySet()) {

			PatternInstancesFromRevision patterns = (PatternInstancesFromRevision) commitResult.getAllResults()
					.get(commit).getResultFromClass(PatternInstanceAnalyzer.class);
			assertNotNull(patterns);

			if ("60b54977abe45f662daaa80ebfdf63ab4fe3a9b2".equals(commit.getName())
					|| "ab71649c481971a9ad54f04797f5fd9cb133789b".equals(commit.getName())) {
				// firsts commits, no changes, file introduction
				assertTrue(patterns.getInfoPerDiff().isEmpty());
			} else
				assertTrue("Not changes at: " + commit.getName(), patterns.getInfoPerDiff().size() > 0);

			for (PatternInstancesFromDiff instancesdiff : patterns.getInfoPerDiff()) {
				assertTrue(instancesdiff.getInstances().size() > 0);
			}
		}

	}

	@Test
	public void testMainPattern1AnyRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("6dac8ae81bd03bcae1e1fade064d3bb03de472c0");
		runnerMainPatterngeneric(ActionType.ANY.toString(), PatternEntity.ANY.toString(), null, possitiveCommits);
	}

	@Test
	public void testMainPattern1AssignementRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("fe76517014e580ddcb40ac04ea824d54ba741c8b");

		runnerMainPatterngeneric(ActionType.ANY.toString(), "Assignment", null, possitiveCommits);
	}

	@Test
	public void testMainPattern1BinaryRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c8cf81ce1f01d4cb213b389a7b85aa13634b7d95");

		runnerMainPatterngeneric(ActionType.ANY.toString(), "BinaryOperator", null, "Assignment", 10, possitiveCommits);
	}

	@Test
	public void testMainPattern1LiteralRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c8cf81ce1f01d4cb213b389a7b85aa13634b7d95");

		runnerMainPatterngeneric(ActionType.INS.toString(), "Literal", null, "Assignment", 10, possitiveCommits);
	}

	@Test
	public void testMainPattern1LiteralRunnerSingleOutput() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c8cf81ce1f01d4cb213b389a7b85aa13634b7d95");

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		FinalResult finalResult = null;

		cs.command.put("-action", ActionType.INS.toString());
		cs.command.put("-entitytype", "Literal");

		cs.command.put("-parenttype", "Assignment");
		cs.command.put("-parameters", "outputperrevision:true");

		finalResult = main.run(cs.flat());
	}

	@Test
	public void testMainPattern1ParentAssingRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001");

		runnerMainPatterngeneric(ActionType.ANY.toString(), "*", null, "Assignment", 10, possitiveCommits);
	}

	@Test
	public void testMainPattern1AnyInvocationRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("4120ab0c714911a9c9f26b591cb3222eaf57d127");

		runnerMainPatterngeneric(ActionType.ANY.toString(), "Invocation", null, possitiveCommits);
	}

	@Test
	public void testMainPattern1InsertInvocationRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("4120ab0c714911a9c9f26b591cb3222eaf57d127");

		runnerMainPatterngeneric(ActionType.INS.toString(), "Invocation", null, possitiveCommits);
	}

	@Test
	public void testMainPattern1UpdateAssigRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001");

		runnerMainPatterngeneric(ActionType.UPD.toString(), "*", null, "Assignment", 10, possitiveCommits);
	}

	@Test
	public void testMainPattern1ParentIfRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("8d94514f4d888b7b4e8abd0d77b974a0c8e3baad");

		runnerMainPatterngeneric(ActionType.ANY.toString(), "*", null, "If", 10, possitiveCommits);
	}

	@Test
	public void testMainPattern1IfAddRunner() throws Exception {

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e");

		runnerMainPatterngeneric(ActionType.INS.toString(), "If", null, "Method", 10, possitiveCommits);
	}

	private void runnerMainPatterngeneric(String action, String entitytype, String entityvalue, Set<String> commitsId)
			throws Exception {
		this.runnerMainPatterngeneric(action, entitytype, entityvalue, null, null, commitsId);
	}

	@SuppressWarnings("unchecked")
	private void runnerMainPatterngeneric(String action, String entitytype, String entityvalue, String parenttype,
			Integer levelParent, Set<String> commitsId) throws Exception {

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		FinalResult finalResult = null;

		cs.command.put("-action", action);
		cs.command.put("-entitytype", entitytype);
		if (entityvalue != null)
			cs.command.put("-entityvalue", entityvalue);
		if (parenttype != null) {
			cs.command.put("-parenttype", parenttype);
			cs.command.put("-parentlevel", levelParent.toString());

		}
		finalResult = main.run(cs.flat());

		assertResult(commitsId, finalResult);

	}

	public void assertResult(Set<String> commitsId, FinalResult finalResult) {
		CommitFinalResult commitResult = (CommitFinalResult) finalResult;
		System.out.println("FinalResults: \n" + finalResult);
		for (Commit commit : commitResult.getAllResults().keySet()) {

			PatternInstancesFromRevision patterns = (PatternInstancesFromRevision) commitResult.getAllResults()
					.get(commit).getResultFromClass(PatternInstanceAnalyzer.class);
			assertNotNull(patterns);

			if (commitsId.contains(commit.getName())) {
				assertTrue("Not changes at: " + commit.getName(), patterns.getInfoPerDiff().size() > 0);
				System.out.println("Instance found: " + patterns.getInfoPerDiff());

				for (PatternInstancesFromDiff instancesdiff : patterns.getInfoPerDiff()) {
					assertTrue(instancesdiff.getInstances().size() > 0);
				}
			} else {
				// firsts commits, no changes, file introduction
				// assertTrue(patterns.getInstances().isEmpty());
			}

		}
	}

	public void assertNotPresentResult(Set<String> commitsId, FinalResult finalResult) {
		CommitFinalResult commitResult = (CommitFinalResult) finalResult;
		System.out.println("FinalResults: \n" + finalResult);
		for (Commit commit : commitResult.getAllResults().keySet()) {

			PatternInstancesFromRevision patterns = (PatternInstancesFromRevision) commitResult.getAllResults()
					.get(commit).getResultFromClass(PatternInstanceAnalyzer.class);
			assertNotNull(patterns);

			if (commitsId.contains(commit.getName())) {
				// the revision has at least one diff
				assertTrue("Not changes at: " + commit.getName(), patterns.getInfoPerDiff().size() > 0);
				// All the diffs have not any instance
				for (PatternInstancesFromDiff instancesdiff : patterns.getInfoPerDiff()) {
					assertTrue(instancesdiff.getInstances().isEmpty());
				}
			}

		}
	}

	@Test
	public void testXMLPattern1_INS_INV_FromFile() throws Exception {
		File fl = new File(getClass().getResource("/pattern_specification/pattern_test_1_INS_INV.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fl.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 1);

		PatternAction pa1 = patternParsed.getAbstractChanges().get(0);
		assertEquals(ActionType.INS, pa1.getAction());
		assertEquals("Invocation", pa1.getAffectedEntity().getEntityType());
		assertEquals("*", pa1.getAffectedEntity().getNewValue());
		Assert.assertNull(pa1.getAffectedEntity().getParentPatternEntity());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fl.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);
		ChangePatternSpecification patternFromAnalyzer = patternAnalyzer.getPatternToMine();

		PatternAction patternAction = patternFromAnalyzer.getAbstractChanges().get(0);
		assertEquals(patternAction.getAction(), patternParsed.getAbstractChanges().get(0).getAction());
		assertEquals(patternAction.getAffectedEntity().getEntityType(), "Invocation");
		assertEquals("*", patternAction.getAffectedEntity().getNewValue());
		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("4120ab0c714911a9c9f26b591cb3222eaf57d127");
		assertResult(possitiveCommits, finalResult);

	}

	@Test
	public void testXMLPattern2_INS_INV_FromFile() throws Exception {
		File fileWithPattern = new File(
				getClass().getResource("/pattern_specification/pattern_test_2_INS_INV_PARENT.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fileWithPattern.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 1);

		PatternAction pa1 = patternParsed.getAbstractChanges().get(0);
		assertEquals(ActionType.INS, pa1.getAction());
		assertEquals("BinaryOperator", pa1.getAffectedEntity().getEntityType());
		assertEquals("*", pa1.getAffectedEntity().getNewValue());
		Assert.assertNotNull(pa1.getAffectedEntity().getParentPatternEntity());
		assertEquals("Assignment", pa1.getAffectedEntity().getParentPatternEntity().getParent().getEntityType());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c8cf81ce1f01d4cb213b389a7b85aa13634b7d95");
		assertResult(possitiveCommits, finalResult);

	}

	@Test
	public void testXMLPattern3_INS_INV_FromFile_twoparent() throws Exception {
		File fileWithPattern = new File(
				getClass().getResource("/pattern_specification/pattern_test_3_INS_INV_PARENT2.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fileWithPattern.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 1);

		PatternAction pa1 = patternParsed.getAbstractChanges().get(0);
		assertEquals(ActionType.INS, pa1.getAction());
		assertEquals("BinaryOperator", pa1.getAffectedEntity().getEntityType());
		assertEquals("*", pa1.getAffectedEntity().getNewValue());
		Assert.assertNotNull(pa1.getAffectedEntity().getParentPatternEntity());
		assertEquals("Assignment", pa1.getAffectedEntity().getParentPatternEntity().getParent().getEntityType());
		// Let's check the grandparent
		assertEquals("Method", pa1.getAffectedEntity().getParentPatternEntity().getParent().getParentPatternEntity()
				.getParent().getEntityType());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c8cf81ce1f01d4cb213b389a7b85aa13634b7d95");
		assertResult(possitiveCommits, finalResult);

	}

	@Test
	public void testXMLPattern4_2_Actions_1move() throws Exception {
		File fileWithPattern = new File(
				getClass().getResource("/pattern_specification/pattern_test_4_2_Actions.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fileWithPattern.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 2);

		PatternAction paInsert = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.INS)).findFirst().get();
		PatternAction paMove = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.MOV)).findFirst().get();

		assertNotNull(paInsert);
		assertNotNull(paMove);

		assertTrue(paMove.getAffectedEntity().getParentPatternEntity().getParent() == paInsert.getAffectedEntity());
		assertEquals("Assignment", paMove.getAffectedEntity().getEntityType());
		assertEquals("If", paInsert.getAffectedEntity().getEntityType());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("fe76517014e580ddcb40ac04ea824d54ba741c8b");
		assertResult(possitiveCommits, finalResult);

	}

	@Test
	public void testXMLPattern5_2_Actions_2Insert() throws Exception {
		File fileWithPattern = new File(
				getClass().getResource("/pattern_specification/pattern_test_5_1_Actions2.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fileWithPattern.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 2);

		PatternAction paInsertIf = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.INS) && e.getAffectedEntity().getEntityType().equals("If"))
				.findFirst().get();
		PatternAction paInseeR = patternParsed.getAbstractChanges().stream().filter(
				e -> e.getAction().equals(ActionType.INS) && e.getAffectedEntity().getEntityType().equals("Return"))
				.findFirst().get();

		assertNotNull(paInsertIf);
		assertNotNull(paInseeR);

		assertTrue(paInseeR.getAffectedEntity().getParentPatternEntity().getParent() == paInsertIf.getAffectedEntity());
		assertEquals("Return", paInseeR.getAffectedEntity().getEntityType());
		assertEquals("If", paInsertIf.getAffectedEntity().getEntityType());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e");
		assertResult(possitiveCommits, finalResult);

	}

	@Test
	public void testXMLPattern5_b_2_Actions_2Insert_with_value() throws Exception {
		File fileWithPattern = new File(
				getClass().getResource("/pattern_specification/pattern_test_5_2_Actions2.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fileWithPattern.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 2);

		PatternAction paInsertIf = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.INS) && e.getAffectedEntity().getEntityType().equals("If"))
				.findFirst().get();
		PatternAction paInseeR = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.INS)
						&& e.getAffectedEntity().getEntityType().equals("Return")
						&& e.getAffectedEntity().getNewValue().equals("return null"))
				.findFirst().get();

		assertNotNull(paInsertIf);
		assertNotNull(paInseeR);

		assertTrue(paInseeR.getAffectedEntity().getParentPatternEntity().getParent() == paInsertIf.getAffectedEntity());
		assertEquals("Return", paInseeR.getAffectedEntity().getEntityType());
		assertEquals("If", paInsertIf.getAffectedEntity().getEntityType());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e");
		assertResult(possitiveCommits, finalResult);

	}

	@Test
	public void testXMLPattern5_b_2_Actions_2Insert_with_value_wrongvalue() throws Exception {
		File fileWithPattern = new File(
				getClass().getResource("/pattern_specification/pattern_test_5_3_Actions2_wrong.xml").getFile());

		ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fileWithPattern.getAbsolutePath());

		Assert.assertNotNull(patternParsed);
		Assert.assertTrue(patternParsed.getAbstractChanges().size() == 2);

		PatternAction paInsertIf = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.INS) && e.getAffectedEntity().getEntityType().equals("If"))
				.findFirst().get();
		PatternAction paInseeR = patternParsed.getAbstractChanges().stream()
				.filter(e -> e.getAction().equals(ActionType.INS)
						&& e.getAffectedEntity().getEntityType().equals("Return")
						&& e.getAffectedEntity().getNewValue().equals("return 1null"))
				.findFirst().get();

		assertNotNull(paInsertIf);
		assertNotNull(paInseeR);

		assertTrue(paInseeR.getAffectedEntity().getParentPatternEntity().getParent() == paInsertIf.getAffectedEntity());
		assertEquals("Return", paInseeR.getAffectedEntity().getEntityType());
		assertEquals("If", paInsertIf.getAffectedEntity().getEntityType());

		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "mineinstance");
		cs.command.put("-pattern", fileWithPattern.getAbsolutePath());
		FinalResult finalResult = main.run(cs.flat());

		PatternInstanceAnalyzer patternAnalyzer = (PatternInstanceAnalyzer) main.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof PatternInstanceAnalyzer).findFirst().get();

		assertNotNull(patternAnalyzer);

		Set<String> possitiveCommits = new HashSet<>();
		possitiveCommits.add("c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e");
		assertNotPresentResult(possitiveCommits, finalResult);

		// assertResult(possitiveCommits, finalResult);

	}

}
