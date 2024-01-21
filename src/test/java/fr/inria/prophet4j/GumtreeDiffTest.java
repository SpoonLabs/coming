package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.junit.Test;
import com.github.gumtreediff.actions.model.Action;
import java.io.File;
import java.util.List;

public class GumtreeDiffTest {

	/**
	 * changes: append("a").append("c") ->
	 * append("c");.append("a").append("b").append("c"); Expect three actions
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChainSyntax() throws Exception {
		AstComparator comparator = new AstComparator();
		File oldFile = new File("src/test/resources/prophet4j/oldChainSyntax.java");
		File newFile = new File("src/test/resources/prophet4j/newChainSyntax.java");
		Diff diff = comparator.compare(oldFile, newFile);
		List<Operation> operations = diff.getRootOperations();
		// Three operations are expected
		assertEquals(3, operations.size());
		for (Operation operation : operations) {
			Action action = operation.getAction();
			if ("UPD".equals(action.getName())) {
				// 'c' -> 'b'
				assertEquals("\"c\"", operation.getSrcNode().toString());
				assertEquals("\"b\"", operation.getDstNode().toString());
			}
		}
	}

	/*
	 * insert a new line string: "b" + EOL
	 */

	@Test
	public void testMultiLinesString() throws Exception {
		AstComparator comparator = new AstComparator();
		File oldFile = new File("src/test/resources/prophet4j/oldMultiLinesString.java");
		File newFile = new File("src/test/resources/prophet4j/newMultiLinesString.java");
		Diff diff = comparator.compare(oldFile, newFile);
		List<Operation> operations = diff.getRootOperations();
		assertEquals(3, operations.size());
	}

	/**
	 * This test contains one update operation.
	 * 
	 * @throws Exception
	 */

	@Test
	public void testExplicitConversion() throws Exception {
		AstComparator comparator = new AstComparator();
		String a = "class Foo{public void bar(){\ndouble b = 1;\n}}";
		String b = "class Foo{public void bar(){\ndouble b = (double) 1;\n}}";
		Diff diff = comparator.compare(a, b);
		List<Operation> operations = diff.getRootOperations();
		// Update Literal at Foo: 1 to ((double) (1))
		assertEquals(1, operations.size());
		assertEquals("update-node", operations.get(0).getAction().getName());

		a = "class Foo{public void bar(){\nint a = 0;\n}}";
		b = "class Foo{public void bar(){\nint a = 1;\n}}";
		diff = comparator.compare(a, b);
		operations = diff.getRootOperations();
		assertEquals(1, operations.size());
		assertEquals("update-node", operations.get(0).getAction().getName());
		assertEquals("0", operations.get(0).getSrcNode().toString());
		assertEquals("1", operations.get(0).getDstNode().toString());
	}

	/**
	 * This test shows there is bug/limitation in the gumtree diff. Diff exists but
	 * not found.
	 * 
	 * @throws Exception
	 */

	@Test
	public void DiffNotFoundTest() throws Exception {
		/*
		 * SRC: x0 = 0.5 * (x0 + x1 - FastMath.max(rtol * FastMath.abs(x1), atol));
		 * TARGET: x0 = 0.5 * ((int)x0 + x1 - FastMath.max(rtol * FastMath.abs(x1),
		 * atol));
		 */

		AstComparator comparator = new AstComparator();
		File oldFile = new File("src/test/resources/prophet4j/buggyBaseSecantSolver.java");
		File newFile = new File("src/test/resources/prophet4j/patchedBaseSecantSolver.java");
		Diff diff = comparator.compare(oldFile, newFile);
		List<Operation> operations = diff.getRootOperations();
		// with new Gumtree 3, this is detected
		assertEquals(1, operations.size());

		/*
		 * SRC: n1n2prod * (n1 + n2 + 1) / 12.0; TARGET: (double) ((double) n1n2prod *
		 * (n1 + n2 + 1)) / 12.0;
		 */
		oldFile = new File("src/test/resources/prophet4j/buggyMannWhitneyUTest.java");
		newFile = new File("src/test/resources/prophet4j/patchedMannWhitneyUTest.java");
		diff = comparator.compare(oldFile, newFile);

		operations = diff.getRootOperations();
		// gumtree3 now supports detection of cast
		assertEquals(2, operations.size());
	}
}
