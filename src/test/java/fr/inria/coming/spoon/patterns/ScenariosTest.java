package fr.inria.coming.spoon.patterns;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.spoon.treeGenerator.SpoonTreeGenerator;
import fr.inria.sacha.coming.util.ConsoleOutput;
import fr.inria.sacha.coming.util.Scenarios;
import fr.inria.sacha.coming.util.XMLOutput;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.labri.gumtree.matchers.Matcher;

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
		
		TreeGeneratorRegistry.addGenerator(new SpoonTreeGenerator());
		
		//Logger.getLogger(DiffSpoon.class).setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("fr.labri.gumtree.matchers").setLevel(java.util.logging.Level.OFF);
		//java.util.logging.Logger.getRootLogger().addAppender(new NullAppender());
		Matcher.LOGGER.setLevel(java.util.logging.Level.OFF);
	}

	@Before
	public void registerSetUp() throws Exception {
		
		//Logger.getLogger(DiffSpoon.class).setLevel(Level.OFF);
		TreeGeneratorRegistry.generators.clear();
		TreeGeneratorRegistry.addGenerator(new SpoonTreeGenerator());
	}
		
	@Test
	public void searchArithBugsSpoon() throws Exception {
		
		String messageHeuristic = "";
				
		
		String path = "repogit4testv0";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.getArithmetics_Spoon(messageHeuristic , path);
		
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
		
		Map<FileCommit, List> instancesFound = 	Scenarios.getArithmeticsBinary(messageHeuristic , path);
		
		Parameters.printParameters();
		
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
	
	
	}
	
	@Test
	public void search1SC() throws Exception {
		
		String repoPath = //"/home/matias/develop/repositories/commons-lang";//
		"repogit4testv0";//"/home/matias/develop/repositories/commons-math";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.get1SC_CD("", repoPath);

		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
		
	}
	
	
	@Test
	public void searchPreconditions() throws Exception {
		
		String repoPath = "repogit4testv0";//"/home/matias/develop/repositories/analyzed/commons-math";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.preconditionsCD("", repoPath);

		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
		
	}
	

}
