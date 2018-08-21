package fr.inria.coming.spoon.patterns;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.github.gumtreediff.matchers.Matcher;

import fr.inria.coming.changeminer.analyzer.Parameters;
import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.util.ConsoleOutput;
import fr.inria.coming.changeminer.util.Scenarios;
import fr.inria.coming.changeminer.util.XMLOutput;
import fr.inria.coming.core.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ScenariosTest {

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

		// Logger.getLogger(DiffSpoon.class).setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("fr.labri.gumtree.matchers").setLevel(java.util.logging.Level.OFF);
		// java.util.logging.Logger.getRootLogger().addAppender(new
		// NullAppender());
		Matcher.LOGGER.setLevel(java.util.logging.Level.OFF);
	}

	@Test
	public void searchArithBugsSpoon() throws Exception {

		String messageHeuristic = "";

		String path = "repogit4testv0";

		Map<Commit, List<Operation>> instancesFound = Scenarios.getArithmetics_Spoon(messageHeuristic, path);

		Parameters.printParameters();

		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

	}

	@Test
	public void searchArithBugsSpoon2() throws Exception {

		String messageHeuristic = "";

		Parameters.MAX_FILES_PER_COMMIT = 2;
		Parameters.ONLY_COMMIT_WITH_TEST_CASE = false;
		Parameters.MAX_AST_CHANGES_PER_FILE = 5;

		String path = "repogit4testv0";

		Map<Commit, List<Operation>> instancesFound = Scenarios.getArithmeticsBinary(messageHeuristic, path);

		Parameters.printParameters();

		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

	}

	@Test
	public void searchPreconditions() throws Exception {

		String repoPath = "repogit4testv0";// "/home/matias/develop/repositories/analyzed/commons-math";
		Map<Commit, List<Operation>> instancesFound = Scenarios.preconditions("", repoPath, GranuralityType.SPOON);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);

	}

}
