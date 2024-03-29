package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.github.difflib.text.DiffRow;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Ignore;
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
	public void testissue261_1() throws Exception {
		// https://github.com/SpoonLabs/coming/issues/261
		// mode == repo

		//names = {"-f", "--feature-option"},
		//		description = "Feature Option" + commonInfo

		fr.inria.coming.main.ComingMain.main(new String[]{"-location", "./repogit4testv0/", "-input", "git", "-output", "out", "-mode", "features"});

		assertTrue(new File("out/features_c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e_FeatureAnalyzer.json").exists());

		// open features_c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e_FeatureAnalyzer.json as json
		// check if the json file contains the expected content
		JsonObject jsonObject = new Gson().fromJson(new FileReader("out/features_c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e_FeatureAnalyzer.json"), JsonObject.class);

	}

	public void testissue261_2() throws Exception {
		// https://github.com/SpoonLabs/coming/issues/261
		// modes  == files

		fr.inria.coming.main.ComingMain.main(new String[] { "-location", "./src/main/resources", "-input", "files", "-output", "out", "-mode", "features"});


	}

	@Test
	public void testFeaturesOnComingEvolutionFromGit1() throws Exception {
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
			assertNotNull(fresults);
		}

	}

	@Test
	public void testFeaturesWithHunkOnComingEvolutionFromGit2() throws Exception {
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

			assertNotNull(hunkresults);

			List<HunkDiff> hunks = hunkresults.getAll();
			System.out.println(hunks);

			System.out.println(featureResult);

			assertNotNull(hunks.size());

			assertTrue(featureResult instanceof FeaturesResult);
			FeaturesResult fresults = (FeaturesResult) featureResult;
			assertNotNull(fresults);
		}

	}

	/**
	 *  Unit test to extract coming code features
	 * 
	 */
	@Test
	public void testFeaturesOnS4REvolutionFromFolder1() throws Exception {
		ComingMain main = new ComingMain();
		CommandSummary cs = new CommandSummary();
		cs.append("-input", "files");
		cs.append("-location", (new File("src/main/resources/pairsD4j")).getAbsolutePath());
		cs.append("-mode", "features");
		cs.append("-output", "./out_features_d4j");
		cs.append("-parameters", "outputperrevision:true");
		FinalResult finalResult = null;

		finalResult = main.run(cs.flat());
		//pairsD4j contains two file pairs, expected to output two JSON feature files.
		assertTrue(finalResult.getAllResults().values().size() == 2);
		
	}
	
	

}
