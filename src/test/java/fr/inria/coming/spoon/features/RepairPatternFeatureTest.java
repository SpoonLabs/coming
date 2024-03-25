package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import fr.inria.coming.main.ComingMain;
import fr.inria.coming.utils.CommandSummary;

public class RepairPatternFeatureTest {
		
	@Test
		public void testRepairPatternFeatures() throws Exception {
			ComingMain main = new ComingMain();
			CommandSummary cs = new CommandSummary();
			cs.append("-input", "files"); 
			cs.append("-location", (new File("src/main/resources/pairsD4j/")).getAbsolutePath());
			cs.append("-mode", "features");
			cs.append("-output", "./tmp_pairsD4j");
			cs.append("-parameters", "outputperrevision:true");	
			main.run(cs.flat());
			
			
			//We test the output JSON files for Defects4J Math_73 bug
			File file = new File("./tmp_pairsD4j/features_Math_73.json");
			/**
			 *  +  if (yMin * yMax > 0) {            
                +  	 throw MathRuntimeException.createIllegalArgumentException(            
                +     NON_BRACKETING_MESSAGE, min, max, yMin, yMax);            
                +   }
			 */
			if (file.isFile() && file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str ="";
				while((str = br.readLine())!=null) {
					if(str.contains("addLineNo")) {
						//added four lines of new code
						String value = str.split(": ")[1].split(",")[0];
						assertEquals(value,"4");
					}
					if(str.contains("insertNewConstLiteral")) {
						//The patch adds  a new constant literal:0
						String value = str.split(": ")[1].split(",")[0];
						assertEquals(value,"1");
					}
				}
				
			}
		
	}
}
