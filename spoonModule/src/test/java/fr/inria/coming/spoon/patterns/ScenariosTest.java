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
import fr.inria.sacha.coming.util.XMLOutput;
import fr.inria.sacha.coming.util.test.Scenarios;
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
		
		
		//Parameters.setUpProperties();
		
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
				
		
		String path = "commons-math.git";
		
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
		
		String path = "commons-math.git";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.getArithmeticsBinary(messageHeuristic , path);
		
		Parameters.printParameters();
		
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
	
	
	}
	
	@Test
	public void search1SC() throws Exception {
		
		String repoPath = //"/home/matias/develop/repositories/commons-lang";//
		"commons-math.git";//"/home/matias/develop/repositories/commons-math";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.get1SC_CD("", repoPath);

		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
		
	}
	
	
	@Test
	public void searchPreconditions() throws Exception {
		
		String repoPath = "commons-math.git";//"/home/matias/develop/repositories/analyzed/commons-math";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.preconditionsCD("", repoPath);

		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
		
	}
	
	@Test
	public void searchIFAddMove() throws Exception {
		
		String repoPath = //"/home/matias/develop/repositories/analyzed/commons-lang";
				"commons-lang.git";
		
		Map<FileCommit, List> instancesFound = 	Scenarios.getAddIf2SCWithTest("", repoPath);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
		assertTrue(RepositoryInspectorSpoonTest.containsCommit(instancesFound, "300f4dcd0b94a76ddc6145ca6e1e780ee418223d"));
		
		assertTrue(RepositoryInspectorSpoonTest.containsCommit(instancesFound, "e34df07747691c5fc25e4375a5974387bf38771c"));
		
		assertTrue(RepositoryInspectorSpoonTest.containsCommit(instancesFound, "f04f0749fff9f3f2c90068356ba1c3e5400d21a0"));
		//da0612b348fcfa7679b92a5e9b8e1603e8e2da3a no
		assertFalse(RepositoryInspectorSpoonTest.containsCommit(instancesFound, "12e2d2bc2826d3f761dd3522deaa522159f67d46"));
		
		//132b060527622e6100a18c276095694883921449 yes
		//34918ebe73b0e0e605d05cd805054f89c003862b tes
		
		//MATH 
		//4fe1f135a368aa3ad0cf57dc9e97e474c86954da yes
		
	}
}
