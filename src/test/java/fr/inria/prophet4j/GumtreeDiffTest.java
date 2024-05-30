package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		// meld src/test/resources/prophet4j/oldChainSyntax.java src/test/resources/prophet4j/newChainSyntax.java
		File oldFile = new File("src/test/resources/prophet4j/oldChainSyntax.java");
		File newFile = new File("src/test/resources/prophet4j/newChainSyntax.java");
		Diff diff = comparator.compare(oldFile, newFile);
		List<Operation> operations = diff.getRootOperations();

		// Insert Invocation at org.apache.logging.log4j.core.config.builder.ConfigurationBuilderTest:37
		//	.append()
		//, Insert Literal at org.apache.logging.log4j.core.config.builder.ConfigurationBuilderTest:39
		//	"b"
		//, Move Invocation from org.apache.logging.log4j.core.config.builder.ConfigurationBuilderTest:37 to org.apache.logging.log4j.core.config.builder.ConfigurationBuilderTest:37
		//	buffer.append("a").append("c")
		//, Move Literal from org.apache.logging.log4j.core.config.builder.ConfigurationBuilderTest:39 to org.apache.logging.log4j.core.config.builder.ConfigurationBuilderTest:40
		//	"c"

		assertEquals(4, operations.size());
		Operation operation = operations.get(0);
		Action action = operation.getAction();
		// big change in recent GT UPD -> update-node
		assertEquals("insert-node", action.getName());
		// 'c' -> 'b'
		assertEquals("buffer.append(\"a\").append(\"b\").append(\"c\")", operation.getSrcNode().toString());

		assertEquals("insert-node",  operations.get(1).getAction().getName());

		assertEquals("move-tree",  operations.get(2).getAction().getName());

	}

	/*
	 * insert a new line string: "b" + EOL
	 */

	@Test
	public void testMultiLinesString() throws Exception {
		AstComparator comparator = new AstComparator();
		// meld src/test/resources/prophet4j/oldMultiLinesString.java src/test/resources/prophet4j/newMultiLinesString.java
		File oldFile = new File("src/test/resources/prophet4j/oldMultiLinesString.java");
		File newFile = new File("src/test/resources/prophet4j/newMultiLinesString.java");
		Diff diff = comparator.compare(oldFile, newFile);
		List<Operation> operations = diff.getRootOperations();
		assertTrue(operations.size() >= 3);
	}

	/**
	 * This test contains one update operation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDelete() throws Exception {
		AstComparator comparator = new AstComparator();
		String a = "class Foo{public void bar(){\ndouble b = (double) 1;\n}}";
		String b = "class Foo{public void bar(){\n}}";
		Diff diff = comparator.compare(a, b);
		List<Operation> operations = diff.getRootOperations();
		// Update Literal at Foo: 1 to ((double) (1))
		assertEquals(1, operations.size());
		assertEquals("delete-node", operations.get(0).getAction().getName());

	}

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

	@Test
	public void diffFoundTest() throws Exception {
		// contract: we can find a diff with cast difference

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
		System.out.println(operations);

		// [Insert TypeReference at org.apache.commons.math.analysis.solvers.BaseSecantSolver:188
		//	int
		//]
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
