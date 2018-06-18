package fr.inria.coming.spoon.comparison;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import fr.inria.sacha.coming.analyzer.DiffEngineFacade;
import fr.inria.sacha.coming.entity.GranuralityType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class SpoonFileComparatorTest {

	@Test
	public void testSpoonCompareFiles() throws Exception {

		DiffEngineFacade gt = new DiffEngineFacade();
		File fl = new File(getClass().getResource("/test1_left.java").getFile());
		File fr = new File(getClass().getResource("/test1_right.java").getFile());

		Diff diff = gt.compareFiles(fl, fr, GranuralityType.SPOON);

		assertNotNull(diff);

		List<Operation> actionsCD = diff.getAllOperations();

		assertNotNull(actionsCD);
		assertTrue(actionsCD.size() > 0);
		// assertEquals(3,actionsCD.size());
	}

}
