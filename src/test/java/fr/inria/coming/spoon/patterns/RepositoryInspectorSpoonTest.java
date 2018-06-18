package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.coming.analyzer.RepositoryInspector;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.filters.PatternFilter;
import fr.inria.sacha.coming.analyzer.filter.NbHunkFilter;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.util.ConsoleOutput;
import fr.inria.sacha.coming.util.XMLOutput;
import fr.inria.sacha.gitanalyzer.filter.KeyWordsMessageFilter;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;

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

		String messageHeuristic = "";

		PatternEntity parent_e = new PatternEntity("Assignment");

		PatternEntity affected_e = new PatternEntity("*", parent_e, 1);

		PatternFilter pattern = new PatternFilter(affected_e, ActionType.ANY);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001"));
	}

	// @Test
	public void searchMultipleParentSpoon2() throws Exception {

		String messageHeuristic = "";

		PatternEntity parent_e = new PatternEntity("Assignment");

		PatternEntity parent_e1 = new PatternEntity("BinaryOperator", parent_e, 10);

		PatternEntity affected_e = new PatternEntity("*", parent_e1, 1);

		PatternFilter pattern = new PatternFilter(affected_e, ActionType.ANY);

		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,
				GranuralityType.SPOON);

		RepositoryInspector c = new RepositoryInspector();

		Map<Commit, List<Operation>> instancesFound = c.analize(repoPath, fineGrainAnalyzer);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound, "656aaf4049092218f99d035450ee59c40a0e1fbc"));
	}

}
