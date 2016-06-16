package fr.inria.coming.spoon.comparison;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.DiffEngineFacade;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.spoon.diffSpoon.CtDiff;

import com.github.gumtreediff.actions.model.Action;

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

		CtDiff diff = gt.compareFiles(fl, fr, GranuralityType.SPOON);

		assertNotNull(diff);
		
		List<Action> actionsCD = diff.getAllActions();

		assertNotNull(actionsCD);
		assertTrue(actionsCD.size() > 0);
		// assertEquals(3,actionsCD.size());
	}

}
