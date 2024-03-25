package fr.inria.coming.spoon.diffanalyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.main.ComingMain;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DiffCasesTest {

	@Test
	public void testDiff1FieldDiff() throws Exception {
		File s = getFile("diffcases/differror1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror1/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff986499_c1() throws Exception {
		File s = getFile("diffcases/arrayerror/case1/986499/AddColumnFamily/986499_AddColumnFamily_0_s.java");
		File t = getFile("diffcases/arrayerror/case1/986499/AddColumnFamily/986499_AddColumnFamily_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff986499_c2() throws Exception {
		File s = getFile("diffcases/arrayerror/case2/986499/AddColumnFamily/986499_AddColumnFamily_0_s.java");
		File t = getFile("diffcases/arrayerror/case2/986499/AddColumnFamily/986499_AddColumnFamily_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff2FieldOperatorIf() throws Exception {
		File s = getFile("diffcases/differror2/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror2/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff3() throws Exception {
		File s = getFile("diffcases/differror3/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror3/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff4() throws Exception {
		File s = getFile("diffcases/differror4/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror4/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff5() throws Exception {
		File s = getFile("diffcases/differror5/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror5/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff6() throws Exception {
		File s = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(2, diffOut.getRootOperations().size());
	}

	@Test
	public void test_testdiff6bis() throws Exception {
		AstComparator diff = new AstComparator();
		File s = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_t.java");
		Diff result = diff.compare(s, t);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(2, actions.size());
	}

	@Test
	public void testDiff7() throws Exception {
		File s = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(2, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff387581() throws Exception {
		File s = getFile("diffcases/387581/IndexTaskTest/387581_IndexTaskTest_0_s.java");
		File t = getFile("diffcases/387581/IndexTaskTest/387581_IndexTaskTest_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff1139461() throws Exception {
		File s = getFile("diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_s.java");
		File t = getFile("diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_t.java");
		// FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output Root:  " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff919148() throws Exception {
		File s = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_s.java");
		File t = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output Root: (" + diffOut.getRootOperations().size() + "): " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff919148_Future() throws Exception {
		File s = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_s.java");
		File t = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getdiffFuture(s, t);
		System.out.println("Output Root: (" + diffOut.getRootOperations().size() + "): " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	@Ignore
	public void testDiff150611() throws Exception {
		File s = getFile("diffcases/150611/QueryParser/150611_QueryParser_0_s.java");
		File t = getFile("diffcases/150611/QueryParser/150611_QueryParser_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getDiff(s, t);
		System.out.println("Output Root: (" + diffOut.getRootOperations().size() + "): " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	public File getFile(String name) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(name).getFile());
		return file;
	}

	@Test
	public void testFileNameGivenByArg1() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + "a = a + b / c +d ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = a + b / c + d ; b = 0; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		String rightName = "mynameRight.java";
		String leftName = "mynameLeft.java";
		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, leftName, rightName);

		assertTrue(diff.getRootOperations().size() > 0);

		Operation opInser = diff.getRootOperations().get(0);
		assertTrue(opInser instanceof InsertOperation);
		InsertOperation ins = (InsertOperation) opInser;
		assertEquals(rightName, opInser.getSrcNode().getPosition().getFile().getName());
		assertEquals(leftName, ins.getParent().getPosition().getFile().getName());

	}

	@Test
	public void testFileNameFromFile2() throws Exception {
		String nameLeft = "diffcases/919148/ReplicationRun/919148_ReplicationRun_0_s.java";
		File s = getFile(nameLeft);
		String nameRight = "diffcases/919148/ReplicationRun/919148_ReplicationRun_0_t.java";
		File t = getFile(nameRight);
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diffOut = r.getdiffFuture(s, t);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);

		Optional<Operation> firstInsert = diffOut.getRootOperations().stream().filter(e -> e instanceof InsertOperation)
				.findFirst();
		assertTrue(firstInsert.isPresent());
		InsertOperation insop = (InsertOperation) firstInsert.get();
		assertNotNull(insop);

		assertEquals(t.getName(), insop.getSrcNode().getPosition().getFile().getName());
		assertEquals(s.getName(), insop.getParent().getPosition().getFile().getName());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNameFileGit() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;
		Map<Commit, RevisionResult> commits = cfres.getAllResults();

		Commit c1 = commits.keySet().stream()
				.filter(e -> e.getName().equals("4120ab0c714911a9c9f26b591cb3222eaf57d127")).findFirst().get();
		DiffResult<Commit, Diff> diff1 = (DiffResult<Commit, Diff>) commits.get(c1)
				.getResultFromClass(FineGrainDifftAnalyzer.class);

		assertEquals(1, diff1.getAll().size());

		Diff diffOut = diff1.getAll().get(0);
		Optional<Operation> firstInsert = diffOut.getRootOperations().stream().filter(e -> e instanceof InsertOperation)
				.findFirst();
		assertTrue(firstInsert.isPresent());
		InsertOperation insop = (InsertOperation) firstInsert.get();
		assertNotNull(insop);

		assertEquals("CharSequenceUtils", insop.getSrcNode().getParent(CtType.class).getSimpleName());

	}

	@Test
	public void testDiff1Comment() throws Exception {
		File s = getFile("diffcases/diffcomment1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffcomment1/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		boolean includeComments = true;
		Diff diffOut = r.getDiff(s, t, includeComments);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
		Operation op = diffOut.getRootOperations().get(0);
		Assert.assertTrue(op.getSrcNode().getComments().size() > 0);

		List<Operation> allop = diffOut.getAllOperations();
		boolean hasComment = false;
		for (Operation operation : allop) {
			hasComment = hasComment || (operation.getSrcNode() instanceof CtComment);
		}
		assertTrue(hasComment);

	}

	@Test
	public void testDiff2Comment() throws Exception {
		File s = getFile("diffcases/diffcomment2/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffcomment2/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		boolean includeComments = true;
		Diff diffOut = r.getDiff(s, t, includeComments);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());

		List<Operation> allop = diffOut.getAllOperations();
		boolean hasComment = false;
		for (Operation operation : allop) {
			hasComment = hasComment || (operation.getSrcNode() instanceof CtComment);
		}
		assertTrue(hasComment);

	}

	@Test
	public void testDiff3Comment() throws Exception {
		File s = getFile("diffcases/diffcomment3/RectangularCholeskyDecomposition_s.java");
		File t = getFile("diffcases/diffcomment3/RectangularCholeskyDecomposition_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		boolean includeComments = true;
		Diff diffOut = r.getDiff(s, t, includeComments);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
		Operation op = diffOut.getRootOperations().get(0);
		Assert.assertTrue(op.getSrcNode().getComments().size() > 0);

		assertFalse(op.getSrcNode() instanceof CtComment);

		List<Operation> allop = diffOut.getAllOperations();
		boolean hasComment = false;
		for (Operation operation : allop) {
			if ((operation.getSrcNode() instanceof CtComment)) {
				hasComment = true;
				System.out.println(operation.getSrcNode());
			}
		}
		assertTrue(hasComment);

	}

}
