package fr.inria.coming.spoon.comparison;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.DiffEngineFacade;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.spoon.treeGenerator.SpoonTreeGenerator;
import fr.labri.gumtree.actions.model.Action;


/**
 * 
 *  @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class SpoonFileComparatorTest {


	@Before
	public void registerSetUp() throws Exception {
		
		TreeGeneratorRegistry.generators.clear();
		TreeGeneratorRegistry.addGenerator(new SpoonTreeGenerator());
	}
		
	@Test
	public void testSpoonCompareFiles() throws URISyntaxException {
		
	
	
		DiffEngineFacade gt = new DiffEngineFacade();
		File fl = new File(getClass().
				getResource("/test1_left.txt").getFile());
		File fr = new File(getClass().
				getResource("/test1_right.txt").getFile());
		
		
		List<Action> actionsCD = gt.compareFiles(fl, fr, GranuralityType.SPOON, true);
		System.out.println(actionsCD);
		assertNotNull(actionsCD);
		//assertEquals(3,actionsCD.size());
	}


	
}
