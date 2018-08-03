package fr.inria.coming.spoon.comparison;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import fr.inria.coming.spoon.patterns.GitRepository4Test;
import fr.inria.sacha.coming.analyzer.RepositoryInspector;
import fr.inria.sacha.coming.analyzer.bfdiff.MapCounter;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SummarizationTest extends GitRepository4Test {
	Logger log = Logger.getLogger(SummarizationTest.class.getName());

	@Test
	public void testSummarization() {
		MapCounter<String> counter = new MapCounter<>();
		FineGrainChangeCommitAnalyzer fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer();

		RepositoryInspector miner = new RepositoryInspector();
		Map<Commit, List<Operation>> instancesFound = miner.analize(repoPath, fineGrainAnalyzer);

		System.out.println("Printing commits: ");
		for (Commit commit : instancesFound.keySet()) {
			List<Operation> ops = instancesFound.get(commit);
			System.out.println("Commit: " + commit.getName() + ", #ops: " + ops.size());

			for (Operation operation : ops) {
				System.out.println("-op->" + operation);
				counter.add(operation.getNode().getClass().getSimpleName());
			}
		}
		Map sorted = counter.sorted();
		log.info("\n***\nSorted:" + sorted);

		log.info("\n***\nProb: " + counter.getProbabilies());

	}

}
