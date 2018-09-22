package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.RepositoryInspector;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters.PatternFilter;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters.SimpleChangeFilter;
import fr.inria.coming.changeminer.analyzer.patternspecification.ParentPatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.util.ConsoleOutput;
import fr.inria.coming.changeminer.util.XMLOutput;
import fr.inria.coming.core.Parameters;
import fr.inria.coming.core.filter.commitmessage.KeyWordsMessageFilter;
import fr.inria.coming.core.filter.diff.NbHunkFilter;
import fr.inria.coming.core.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtMethod;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class RepositoryInspectorSpoonTest extends GitRepository4Test {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(console);
		java.util.logging.Logger.getLogger("fr.labri.gumtree.matchers").setLevel(java.util.logging.Level.OFF);
	}

	@Test
	public void test1() throws Exception {

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, new NullAnalyzer());
	}

	@Test
	public void testkeywordfilter() throws Exception {
		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, new NullAnalyzer(),
				new KeyWordsMessageFilter("precondition"));
		ConsoleOutput.printResultDetails(instancesFound);
		assertEquals(1, instancesFound.keySet().size());
	}

	@Test
	public void testHunkfilter() throws Exception {

		// two hunks
		RepositoryInspector c = new RepositoryInspector();
		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, new NullAnalyzer(), new NbHunkFilter(2, 2));
		ConsoleOutput.printResultDetails(instancesFound);
		assertEquals(1, instancesFound.keySet().size());

		// only additions
		instancesFound = c.analize(repoPath, new NullAnalyzer(), new NbHunkFilter(0, 0));
		ConsoleOutput.printResultDetails(instancesFound);
		assertEquals(2, instancesFound.keySet().size());
	}

	@Test
	public void searchConditionChangeSpoonParent() throws Exception {

		String messageHeuristic = "";

		PatternFilter pattern = new PatternFilter("BinaryOperator", ActionType.UPD, "If", 10);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		Parameters.MAX_AST_CHANGES_PER_FILE = 2;
		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "8d94514f4d888b7b4e8abd0d77b974a0c8e3baad"));
	}

	@Test
	public void spoonTestMI() throws Exception {

		SimpleChangeFilter pattern = new SimpleChangeFilter("Invocation", ActionType.INS);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector miner = new RepositoryInspector();
		Map<Commit, List<Operation>> instancesFound = miner.analize(repoPath, fineGrainAnalyzer);
		// ConsoleOutput.printResultDetails(instancesFound);

		for (List vales : instancesFound.values()) {
			System.out.println("Values: " + vales);
		}

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "4120ab0c714911a9c9f26b591cb3222eaf57d127", "Invocation"));

	}

	/**
	 * Search for Any change (insert, delete )in any kind of node (expression,
	 * assignment) inside an if
	 * 
	 * @throws Exception
	 */
	@Test
	public void searchConditionChangeSpoonParentStar() throws Exception {

		String messageHeuristic = "";

		PatternFilter pattern = new PatternFilter("*", ActionType.ANY, "If", 10);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		Parameters.MAX_AST_CHANGES_PER_FILE = 2;

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "8d94514f4d888b7b4e8abd0d77b974a0c8e3baad"));
	}

	@Test
	public void searchChangeAssignmentSpoon() throws Exception {

		String messageHeuristic = "";

		PatternFilter pattern = new PatternFilter("*", ActionType.ANY, "Assignment", 10);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001"));
	}

	@Test
	public void searchMultipleParentSpoon1() throws Exception {

		PatternEntity parent_e = new PatternEntity("Assignment");

		ParentPatternEntity parent = new ParentPatternEntity(parent_e, 1);

		PatternEntity affected_e = new PatternEntity("*", parent);

		PatternFilter pattern = new PatternFilter(affected_e, ActionType.ANY);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001"));

		Commit cWithinstances = getCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001");
		assertNotNull(cWithinstances);
		List<Operation> ops = instancesFound.get(cWithinstances);

		assertNotNull(ops);
		Assert.assertTrue(ops.size() > 0);

		Operation op = ops.get(0);
		System.out.println("Operator " + op.getSrcNode().getParent(CtMethod.class));
		// Assert.assertTrue(op.getSrcNode().getParent().getClass().getSimpleName().contains("BinaryOperator"));
		Assert.assertTrue(op.getSrcNode().getParent().getClass().getSimpleName().contains("Assignment"));
	}

	@Test
	public void searchMultipleParentSpoon2() throws Exception {

		ParentPatternEntity grand_parent1 = new ParentPatternEntity(new PatternEntity("Assignment"), 10);

		PatternEntity parent_e1 = new PatternEntity("BinaryOperator", grand_parent1);

		ParentPatternEntity parent_of_change = new ParentPatternEntity(parent_e1, 1);

		PatternEntity affected_e = new PatternEntity("*", parent_of_change);

		PatternFilter pattern = new PatternFilter(affected_e, ActionType.ANY);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "656aaf4049092218f99d035450ee59c40a0e1fbc"));

		Commit cWithinstances = getCommit(instancesFound, "656aaf4049092218f99d035450ee59c40a0e1fbc");
		assertNotNull(cWithinstances);
		List<Operation> ops = instancesFound.get(cWithinstances);

		assertNotNull(ops);
		Assert.assertTrue(ops.size() > 0);

		Operation op = ops.get(0);
		System.out.println("Operator " + op.getSrcNode().getParent(CtMethod.class));

		System.out.println("Changed " + op.getSrcNode());

		Assert.assertTrue(op.getSrcNode().getParent().getClass().getSimpleName().contains("BinaryOperator"));

		Assert.assertTrue(op.getSrcNode().getParent().getParent().getClass().getSimpleName(),
				op.getSrcNode().getParent().getParent().getClass().getSimpleName().contains("Invocation"));

		Assert.assertTrue(op.getSrcNode().getParent().getParent().getClass().getSimpleName(),
				op.getSrcNode().getParent().getParent().getParent().getClass().getSimpleName().contains("Assignment"));

	}

	@Test
	public void searchMultipleParentSpoon3NotPattern() throws Exception {

		// we put level 1, which is wrong, so no pattern here
		ParentPatternEntity grand_parent1 = new ParentPatternEntity(new PatternEntity("Assignment"), 1);

		PatternEntity parent_e1 = new PatternEntity("BinaryOperator", grand_parent1);

		ParentPatternEntity parent_of_change = new ParentPatternEntity(parent_e1, 1);

		PatternEntity affected_e = new PatternEntity("*", parent_of_change);

		PatternFilter pattern = new PatternFilter(affected_e, ActionType.ANY);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Commit cWithinstances = getCommit(instancesFound, "656aaf4049092218f99d035450ee59c40a0e1fbc");
		assertNotNull(cWithinstances);
		List<Operation> ops = instancesFound.get(cWithinstances);
		System.out.println(ops);
		assertNotNull(ops);
		// FAILING
		Assert.assertTrue(ops.isEmpty());
	}

	@Test
	@Ignore
	public void searchMultipleParentBuggy() throws Exception {

		PatternEntity parent_e = new PatternEntity("Assignment");

		ParentPatternEntity parent = new ParentPatternEntity(parent_e, 1);

		PatternEntity affected_e = new PatternEntity("*", parent);

		PatternFilter pattern = new PatternFilter(affected_e, ActionType.INS);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001"));

		Commit cWithinstances = getCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001");
		assertNotNull(cWithinstances);
		List<Operation> ops = instancesFound.get(cWithinstances);

		assertNotNull(ops);
		Assert.assertTrue(ops.size() > 0);

		Operation op = ops.get(0);
		System.out.println("Operator " + op.getSrcNode().getParent(CtMethod.class));
		// Assert.assertTrue(op.getSrcNode().getParent().getClass().getSimpleName().contains("BinaryOperator"));
		Assert.assertTrue(op.getSrcNode().getParent().getClass().getSimpleName().contains("Assignment"));

		boolean hasInsert = false;
		for (Operation operation : ops) {
			if (SimpleChangeFilter.matchTypes(operation.getAction(), ActionType.INS)) {
				hasInsert = true;
			}
		}

		Assert.assertTrue(hasInsert);
	}
}
