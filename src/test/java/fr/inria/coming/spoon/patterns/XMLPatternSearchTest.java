package fr.inria.coming.spoon.patterns;

import java.io.File;
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
import fr.inria.sacha.coming.analyzer.treeGenerator.ChangePattern;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.spoon.treeGenerator.SpoonTreeGenerator;
import fr.inria.sacha.coming.util.ConsoleOutput;
import fr.inria.sacha.coming.util.PatternXMLParser;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
/**
 * 
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */
public class XMLPatternSearchTest extends GitRepository4Test {

	
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
		TreeGeneratorRegistry.addGenerator(new SpoonTreeGenerator());
	}
	
	@Test
	public void searchPatternFromFile(){
		File fl = new File(getClass().
				getResource("/pattern_test_1.xml").getFile());
		
		
			ChangePattern patternParsed = PatternXMLParser.parseFile(fl.getAbsolutePath());
		
			Assert.assertNotNull(patternParsed );
			Assert.assertTrue(patternParsed.getChanges().size() == 1);
			
			String messageHeuristic = "";
			
			SimpleChangeFilter  patternFilter= new SimpleChangeFilter (patternParsed.getChanges().get(0));

			FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
			
			
			RepositoryInspector miner = new RepositoryInspector();
			Map<Commit, List> instancesFound = miner.analize(repoPath,
					fineGrainAnalyzer);
			ConsoleOutput.printResultDetails(instancesFound);

			Assert.assertTrue(instancesFound.keySet().size() > 0);
			Assert.assertTrue(containsCommit(instancesFound,
					"4120ab0c714911a9c9f26b591cb3222eaf57d127", "Invocation"));

	
	}
	
}
