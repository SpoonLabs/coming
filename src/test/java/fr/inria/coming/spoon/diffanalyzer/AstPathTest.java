package fr.inria.coming.spoon.diffanalyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Test;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtPath;

/**
 * 
 * @author Matias Martinez
 *
 */
public class AstPathTest {

	@Test
	public void test_Path_from_Spoon() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/main/resources/diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_s.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("WildcardQuery", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

	}

	@Test
	public void test_Paths() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/main/resources/diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_s.java");
		File fr = new File("src/main/resources/diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_t.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("WildcardQuery", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

		CtType<?> astRight = diff.getCtType(fr);

		assertNotNull(astRight);
		assertEquals("WildcardQuery", astRight.getSimpleName());
		assertEquals("org.apache.lucene.search.WildcardQuery", astRight.getQualifiedName());

		CtPath pathRight = astRight.getPath();
		assertNotNull(pathRight);

		retrievePathOfStmt(astLeft);
		retrievePathOfStmt(astRight);

	}

	private void retrievePathOfStmt(CtType<?> ast) {
		for (CtMethod method : ast.getAllMethods()) {

			CtPath path = method.getPath();
			assertNotNull(path);

			for (CtStatement stmt : method.getBody().getStatements()) {
				path = stmt.getPath();
				assertNotNull(path);
			}

		}
	}

	@Test
	public void test_Path_of_affected__failing2() throws Exception {
		File fl = new File("src/main/resources/diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_s.java");
		File fr = new File("src/main/resources/diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_t.java");
		this.test_Path_of_affected_nodes(fl, fr);
	}

	@Test
	public void test_Path_of_affected__failing() throws Exception {
		File fl = new File("src/main/resources/diffcases/919148/ReplicationRun/919148_ReplicationRun_0_s.java");
		File fr = new File("src/main/resources/diffcases/919148/ReplicationRun/919148_ReplicationRun_0_t.java");
		this.test_Path_of_affected_nodes(fl, fr);
	}

	@Test
	public void test_Path_of_affected__working() throws Exception {
		File fl = new File("src/main/resources/diffcases/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/main/resources/diffcases/test8/right_QuickNotepad_1.14.java");
		this.test_Path_of_affected_nodes(fl, fr);
	}

	protected void test_Path_of_affected_nodes(File fl, File fr) throws Exception {
		AstComparator comparator = new AstComparator();

		CtType<?> astLeft = comparator.getCtType(fl);

		assertNotNull(astLeft);
		assertNotNull(fl.getPath());

		CtType<?> astRight = comparator.getCtType(fr);
		assertNotNull(astRight);
		assertNotNull(fr.getPath());

		retrievePathOfStmt(astLeft);
		retrievePathOfStmt(astRight);

		Diff diffResult = comparator.compare(astLeft, astRight);
		List<Operation> rootOperations = diffResult.getRootOperations();
		retrievePathOFAffectedElements(rootOperations);
		List<Operation> allOperations = diffResult.getAllOperations();
		retrievePathOFAffectedElements(allOperations);

	}

	private void retrievePathOFAffectedElements(List<Operation> rootops) {
		for (Operation<?> op : rootops) {

			CtElement left = op.getSrcNode();

			CtPath pleft = left.getPath();
			assertNotNull(pleft);

			CtElement right = op.getSrcNode();
			CtPath pright = right.getPath();
			assertNotNull(pright);

		}
	}

}
