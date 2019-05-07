package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.HunkDifftAnalyzer;
import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.codefeatures.FeatureAnalyzer;
import fr.inria.coming.codefeatures.FeaturesResult;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.HunkDiff;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.utils.CommandSummary;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FeaturesOnComingMainTest {

	@Test
	public void testFeaturesOnComing1() throws Exception {
		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "features");
		FinalResult finalResult = null;

		finalResult = main.run(cs.flat());

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		assertTrue(commitResult.getAllResults().values().size() > 0);

		for (Commit iCommit : commitResult.getAllResults().keySet()) {

			RevisionResult resultofCommit = commitResult.getAllResults().get(iCommit);
			// Get the results of this analyzer
			AnalysisResult featureResult = resultofCommit.get(FeatureAnalyzer.class.getSimpleName());

			assertTrue(featureResult instanceof FeaturesResult);
			FeaturesResult fresults = (FeaturesResult) featureResult;

		}

	}

	@Test
	public void testFeaturesOnComing2() throws Exception {
		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location", "repogit4testv0");
		cs.append("-mode", "features");
		cs.append("-hunkanalysis", "true");
		FinalResult finalResult = null;

		finalResult = main.run(cs.flat());

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		assertTrue(commitResult.getAllResults().values().size() > 0);

		for (Commit iCommit : commitResult.getAllResults().keySet()) {

			RevisionResult resultofCommit = commitResult.getAllResults().get(iCommit);
			// Get the results of this analyzer
			AnalysisResult featureResult = resultofCommit.get(FeatureAnalyzer.class.getSimpleName());
			AnalysisResult hunkResult = resultofCommit.get(HunkDifftAnalyzer.class.getSimpleName());

			assert (hunkResult instanceof DiffResult);
			DiffResult<Commit, HunkDiff> hunkresults = (DiffResult<Commit, HunkDiff>) hunkResult;

			List<HunkDiff> hunks = hunkresults.getAll();
			System.out.println(hunks);

			System.out.println(featureResult);

		}

	}

}
