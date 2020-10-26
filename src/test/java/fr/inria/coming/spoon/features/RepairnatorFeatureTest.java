package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertEquals;
import java.io.File;
import org.junit.Test;
import fr.inria.coming.codefeatures.RepairnatorFeatures;
import fr.inria.coming.codefeatures.RepairnatorFeatures.ODSLabel;

public class RepairnatorFeatureTest {
 
	 	@Test
		public void correctPatch() throws Exception  {
		 File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Lang_36");
		 ODSLabel label = new RepairnatorFeatures().getLabel(pairFolder);
		 assertEquals(label,ODSLabel.CORRECT);
	 }
	 	@Test
	 	public void unknownPatch() throws Exception  {
			 File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Math_42");
			 ODSLabel label = new RepairnatorFeatures().getLabel(pairFolder);
			 assertEquals(label,ODSLabel.UNKNOWN);
		 }
}
