package fr.inria.coming.spoon.features;

import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.main.CommandSummary;

public class CardumenFeaturesTest {

	@Test
	@Ignore
	public void testFeaturesOnComing1() throws Exception {
		ComingMain main = new ComingMain();

		CommandSummary cs = new CommandSummary();
		cs.append("-location",
				"/Users/matias/develop/overfitting-research/diffuniftopairs/patchesunidiff/outCardumenMath");
		cs.append("-input", "files");
		cs.append("-mode", "features");
		cs.append("-output", "./outcoming/");
		cs.append("-hunkanalysis", "true");
		cs.append("-parameters", "maxrevision:100000:excludecommitnameinfile:false:"
				+ "avoidgroupsubfeatures:true:save_result_revision_analysis:false:outputperrevision:true");
		FinalResult finalResult = null;

		finalResult = main.run(cs.flat());

		FinalResult commitResult = (FinalResult) finalResult;

		// assertTrue(commitResult.getAllResults().values().size() > 0);

	}
}
