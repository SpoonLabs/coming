package fr.inria.coming.jdt.pattern;

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

import fr.inria.sacha.coming.analyzer.RepositoryInspector;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.PatternFilter;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.SimpleChangeFilter;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.coming.entity.EntityType;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.jdt.treeGenerator.CDTreeGenerator;
import fr.inria.sacha.coming.jdt.treeGenerator.JDTTreeGenerator;
import fr.inria.sacha.coming.util.ConsoleOutput;
import fr.inria.sacha.coming.util.XMLOutput;
import fr.inria.sacha.coming.util.test.GitRepository4Test;
import fr.inria.sacha.coming.util.test.Scenarios;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;

/**
 *  
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class RepositoryInspectorJDTTest extends GitRepository4Test{

	
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
	
	@Before
	public void registerSetUp() throws Exception {
		
		TreeGeneratorRegistry.generators.clear();
		TreeGeneratorRegistry.addGenerator(new JDTTreeGenerator());
		TreeGeneratorRegistry.addGenerator(new CDTreeGenerator());
	}
		

	@Test
	public void searchConditionChangeCD() throws Exception {
		
		String messageHeuristic = "";

		SimpleChangeFilter pattern = 	new SimpleChangeFilter(EntityType.IF_STATEMENT.name(), ActionType.UPD);

		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,GranuralityType.SPOON );
		
		
		RepositoryInspector c = new RepositoryInspector();

		Map<FileCommit, List> instancesFound = c.analize(
				repoPath,
				fineGrainAnalyzer, messageHeuristic);
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
		
		Assert.assertTrue(instancesFound.keySet().size() > 0);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void search1SCCommits(){
		Map<FileCommit, List> instancesFound = 	Scenarios.get1SC_CD("" ,this.repoPath);
		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertFalse(containsCommit(instancesFound, "c8cf81ce1f01d4cb213b389a7b85aa13634b7d95"));
		Assert.assertTrue(containsCommit(instancesFound, "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001"));
		Assert.assertFalse(containsCommit(instancesFound, "01dd29c37f6044d9d1126d9db55a961cccaccfb7"));
		Assert.assertFalse(containsCommit(instancesFound, "6dac8ae81bd03bcae1e1fade064d3bb03de472c0"));
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void searchPreconditions(){
		Map<FileCommit, List> instancesFound = 	Scenarios.preconditionsCD("" ,this.repoPath);
		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertFalse(containsCommit(instancesFound, "c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e"));
		Assert.assertTrue(containsCommit(instancesFound, "fe76517014e580ddcb40ac04ea824d54ba741c8b"));
		/*Assert.assertFalse(containsCommit(instancesFound, "01dd29c37f6044d9d1126d9db55a961cccaccfb7"));
		Assert.assertFalse(containsCommit(instancesFound, "6dac8ae81bd03bcae1e1fade064d3bb03de472c0"));
		*/
	}
	
}
