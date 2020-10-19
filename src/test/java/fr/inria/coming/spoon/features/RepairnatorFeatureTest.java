package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertEquals;
import java.io.File;
import org.junit.Test;
import fr.inria.coming.codefeatures.RepairnatorFeatures;

public class RepairnatorFeatureTest {
 
	 	@Test
		public void correctPatch() throws Exception  {
		 File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Lang_36");
		 String label = new RepairnatorFeatures().getLabel(pairFolder);
		 assertEquals(label,"correct");
	 }
	 	@Test
	 	public void unknownPatch() throws Exception  {
			 File pairFolder = new File("src/main/resources/Defects4J_all_pairs/Math_42");
			 String label = new RepairnatorFeatures().getLabel(pairFolder);
			 assertEquals(label,"unknown");
		 }
}
