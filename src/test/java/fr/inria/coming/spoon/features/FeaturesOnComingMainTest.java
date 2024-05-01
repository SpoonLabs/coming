package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.matchers.GumtreeProperties;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
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

	@Test
	@Ignore //OutOfMemory Java heap space on CI
	public void testissue261_2() throws Exception {
		// https://github.com/SpoonLabs/coming/issues/261
		// modes  == files

		fr.inria.coming.main.ComingMain.main(new String[] { "-location", "./src/main/resources", "-input", "files", "-output", "out", "-mode", "features"});

	}

	@Test
	public void testissue261_3() throws Exception {
		// https://github.com/SpoonLabs/coming/issues/261
		// no P4J features in special example
		// bug was in "DEL" in CodeDiffer
		// change in string convention in gumtree, grrrrrr
		// "DEL".equals(operation.getAction().getName()) -> "delete-node".equals(operation.getAction().getName())

		fr.inria.coming.main.ComingMain.main(new String[] { "-location", "src/main/resources/issue261/261_s.java:src/main/resources/issue261/261_t.java", "-input", "filespair", "-output", "out", "-mode", "features"});

		JsonObject jsonObject = new Gson().fromJson(new FileReader("out/features_261_s.java->261_t.java_FeatureAnalyzer.json"), JsonObject.class);

		// jq ".files[0].features[1]"
		final Set<Map.Entry<String, JsonElement>> entries = jsonObject.get("files").getAsJsonArray().get(0).getAsJsonObject().get("features").getAsJsonArray().get(1).getAsJsonObject().entrySet();

		// check that we have a P4J feature
		assertTrue(entries.size() > 0);
		for (Map.Entry<String, JsonElement> entry : entries) {
			final JsonObject value = (JsonObject) entry.getValue();
			assertTrue(value.keySet().contains("P4J_AF_VF_CT_ABST_V_AF"));
		}

	}

	@Test
	public void testissue264() throws Exception {
		// https://github.com/SpoonLabs/coming/issues/264

		fr.inria.coming.main.ComingMain.main(new String[] { "-location", "src/main/resources/issue264/Util_before.java:src/main/resources/issue264/Util_after.java", "-input", "filespair", "-output", "out", "-mode", "features"});

		JsonObject jsonObject = new Gson().fromJson(new FileReader("out/features_Util_before.java->Util_after.java_FeatureAnalyzer.json"), JsonObject.class);

		// jq ".files[0].features[3]" out/'features_Util_before.java->Util_after.java_FeatureAnalyzer.json'

		final Set<Map.Entry<String, JsonElement>> entries = jsonObject.get("files").getAsJsonArray().get(0).getAsJsonObject().get("features").getAsJsonArray().get(3).getAsJsonObject().entrySet();

		// check that we have a P4J feature
		assertTrue(entries.size() > 0);
		for (Map.Entry<String, JsonElement> entry : entries) {
			final JsonObject value = (JsonObject) entry.getValue();
			assertEquals("1", value.get("P4J_RF_CT_REMOVE_STMT").getAsString());
		}

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
