package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import fr.inria.coming.codefeatures.RepairnatorFeatures;
import fr.inria.coming.codefeatures.RepairnatorFeatures.ODSLabel;
import fr.inria.coming.main.ComingProperties;

public class RepairnatorFeatureTest {

	@Test
	public void correctPatch() throws Exception {
		File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Lang_36");
		ODSLabel label = new RepairnatorFeatures().getLabel(pairFolder);
		assertEquals(label, ODSLabel.CORRECT);
	}

	@Test
	public void correctPatch2() throws Exception {
		File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Math_42");
		ODSLabel label = new RepairnatorFeatures().getLabel(pairFolder);
		assertEquals(label, ODSLabel.CORRECT);
	}

	@Test
	public void correctPatchAbsoluteLocationModel() throws Exception {
		File locationToModel = new File("./src/main/resources/ODSTraining/ODSmodel.bin");
		String locationString = locationToModel.getAbsolutePath();
		ComingProperties.setProperty(RepairnatorFeatures.ODS_MODEL, locationString);

		assertEquals(ComingProperties.getProperty(RepairnatorFeatures.ODS_MODEL), locationString);

		File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Lang_36");
		ODSLabel label = new RepairnatorFeatures().getLabel(pairFolder);
		assertEquals(label, ODSLabel.CORRECT);
	}
}
