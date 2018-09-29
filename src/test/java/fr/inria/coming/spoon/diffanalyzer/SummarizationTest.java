package fr.inria.coming.spoon.diffanalyzer;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.core.engine.files.MapCounter;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.spoon.patterns.GitRepository4Test;
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
		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		GITRepositoryInspector miner = new GITRepositoryInspector();
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
