package fr.inria.coming.spoon.diffanalyzer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import diffanalyzer.BugFixRunner;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class BugFixRunnerTest {

	@Test
	public void testDiff1FieldDiff() throws Exception {
		File s = getFile("diffcases/differror1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror1/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff986499_c1() throws Exception {
		File s = getFile("diffcases/arrayerror/case1/986499/AddColumnFamily/986499_AddColumnFamily_0_s.java");
		File t = getFile("diffcases/arrayerror/case1/986499/AddColumnFamily/986499_AddColumnFamily_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff986499_c2() throws Exception {
		File s = getFile("diffcases/arrayerror/case2/986499/AddColumnFamily/986499_AddColumnFamily_0_s.java");
		File t = getFile("diffcases/arrayerror/case2/986499/AddColumnFamily/986499_AddColumnFamily_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff2FieldOperatorIf() throws Exception {
		File s = getFile("diffcases/differror2/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror2/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff3() throws Exception {
		File s = getFile("diffcases/differror3/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror3/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff4() throws Exception {
		File s = getFile("diffcases/differror4/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror4/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff5() throws Exception {
		File s = getFile("diffcases/differror5/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror5/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff6() throws Exception {
		File s = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/differror6/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
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
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(2, diffOut.getRootOperations().size());
	}

	@Test
	public void testDiff387581() throws Exception {
		File s = getFile("diffcases/387581/IndexTaskTest/387581_IndexTaskTest_0_s.java");
		File t = getFile("diffcases/387581/IndexTaskTest/387581_IndexTaskTest_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff1139461() throws Exception {
		File s = getFile("diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_s.java");
		File t = getFile("diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output Root:  " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff919148() throws Exception {
		File s = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_s.java");
		File t = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output Root: (" + diffOut.getRootOperations().size() + "): " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	public void testDiff919148_Future() throws Exception {
		File s = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_s.java");
		File t = getFile("diffcases/919148/ReplicationRun/919148_ReplicationRun_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiffFuture(s, t);
		System.out.println("Output Root: (" + diffOut.getRootOperations().size() + "): " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	@Test
	@Ignore
	public void testDiff150611() throws Exception {
		File s = getFile("diffcases/150611/QueryParser/150611_QueryParser_0_s.java");
		File t = getFile("diffcases/150611/QueryParser/150611_QueryParser_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output Root: (" + diffOut.getRootOperations().size() + "): " + diffOut.getRootOperations());
		Assert.assertTrue(diffOut.getRootOperations().size() > 0);
	}

	public File getFile(String name) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(name).getFile());
		return file;
	}
}
