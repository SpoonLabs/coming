package fr.inria.coming.spoon.repairability.repairtools;


import fr.inria.coming.spoon.repairability.RepairabilityTestUtils;

import org.junit.Test;

public class NPEfixTest {
	
	@Test
	public void testGroundTruthCreatedPatches() throws Exception {
    	RepairabilityTestUtils.checkGroundTruthPatches(getClass(), "NPEfix", 83, 0);
	}

//	@Test
//	public void testFilePairs() throws Exception {
//
//		File left = getFile("repairability_test_files/ground_truth/NPEfix/patch7-Math-1115/NPEfix/00125/NPEfix_00125_s.java");
//		File right = getFile("repairability_test_files/ground_truth/NPEfix/patch7-Math-1115/NPEfix/00125/NPEfix_00125_t.java");
//
//		ComingMain cm = new ComingMain();
//
//		FinalResult result = cm
//				.run(new String[] { "-location", left.getAbsolutePath() + File.pathSeparator + right.getAbsolutePath(),
//						"-input", "filespair", "-mode", "repairability", "-repairtool", "NPEfix", "-parameters", 
//						"include_all_instances_for_each_tool:true:max_nb_commit_analyze:300:max_time_for_a_git_repo:-1"});
//
//		assertNotNull(result);
//	}
//	
//	public File getFile(String name) {
//		ClassLoader classLoader = getClass().getClassLoader();
//		File file = new File(classLoader.getResource(name).getFile());
//		return file;
//	}
}
