package fr.inria.coming.jdt;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.RepositoryInspector;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.SimpleChangeFilter;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.coming.entity.EntityType;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.jdt.treeGenerator.CDTreeGenerator;
import fr.inria.sacha.coming.jdt.treeGenerator.JDTTreeGenerator;
import fr.inria.sacha.coming.util.ConsoleOutput;
import fr.inria.sacha.coming.util.test.GitRepository4Test;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;

/**
 * 
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class PatternSearchTest extends GitRepository4Test{


	@Before
	public void setUp() throws Exception {

		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(console);
	}

	@Before
	public void registerSetUp() throws Exception {
		
		TreeGeneratorRegistry.generators.clear();
		TreeGeneratorRegistry.addGenerator(new JDTTreeGenerator());
		TreeGeneratorRegistry.addGenerator(new CDTreeGenerator());
	}
	
	@Test
	public void cdTestMI() throws Exception {
		String messageHeuristic = "";

		SimpleChangeFilter pattern = new SimpleChangeFilter (
				EntityType.METHOD_INVOCATION.name(), ActionType.INS);

		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(pattern,GranuralityType.CD );
		
		
		
		RepositoryInspector miner = new RepositoryInspector();
		Map<FileCommit, List> instancesFound = miner.analize(repoPath,
				fineGrainAnalyzer, messageHeuristic);
		ConsoleOutput.printResultDetails(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound,
				"4120ab0c714911a9c9f26b591cb3222eaf57d127",
				EntityType.METHOD_INVOCATION.name()));

	}

	

	@Test
	public void jdtTestMI() throws Exception {
		String messageHeuristic = "";

		SimpleChangeFilter patternFilter = new SimpleChangeFilter(
				"ExpressionStatement", ActionType.INS);
		
		
		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(
				patternFilter, GranuralityType.JDT);

		RepositoryInspector miner = new RepositoryInspector();
		Map<FileCommit, List> instancesFound = miner.analize(repoPath,
				fineGrainAnalyzer, messageHeuristic);
		ConsoleOutput.printResultDetails(instancesFound);

		Assert.assertTrue(instancesFound.keySet().size() > 0);
		Assert.assertTrue(containsCommit(instancesFound,
				"4120ab0c714911a9c9f26b591cb3222eaf57d127",
				"ExpressionStatement"));

	}
	
	
	

	

}
