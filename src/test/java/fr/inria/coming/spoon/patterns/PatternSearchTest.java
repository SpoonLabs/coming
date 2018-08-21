package fr.inria.coming.spoon.patterns;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.RepositoryInspector;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters.SimpleChangeFilter;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.core.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class PatternSearchTest extends GitRepository4Test {

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

}
