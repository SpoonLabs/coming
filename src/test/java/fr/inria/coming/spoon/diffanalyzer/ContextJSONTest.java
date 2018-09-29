package fr.inria.coming.spoon.diffanalyzer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.inria.coming.core.engine.files.BugFixRunner;
import fr.inria.coming.core.engine.files.DiffICSE15ContextAnalyzer;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;

public class ContextJSONTest {
	File s = getFile("patterns_examples/case2/1205753_EmbedPooledConnection_0_s.java");
	File t = getFile("patterns_examples/case2/1205753_EmbedPooledConnection_0_t.java");

	@Before
	public void setUp() throws Exception {

		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(console);

	}

	@Test
	public void testDiff2Invocation() throws Exception {
		Diff diffInsert = null;

		BugFixRunner r = new BugFixRunner();
		diffInsert = r.getdiff(s, t);
		System.out.println("Output: " + diffInsert);
		Assert.assertEquals(1, diffInsert.getRootOperations().size());
		Assert.assertTrue(diffInsert.getRootOperations().get(0) instanceof InsertOperation);

		List<Operation> opsr = diffInsert.getRootOperations();
		DiffICSE15ContextAnalyzer analyzer = new DiffICSE15ContextAnalyzer();
		Map<String, Diff> opsByFile = new HashMap<>();
		opsByFile.put("filetest", diffInsert);
		JSONObject js = analyzer.calculateCntxJSON("filetest", opsByFile);
		System.out.println("out:\n" + js);

		for (Operation operation : opsr) {
			System.out.println("src: " + operation.getSrcNode());
			System.out.println("trg: " + operation.getDstNode());
			System.out.println("node: " + operation.getNode());
		}
	}

	@Test
	public void testDiffDelete() throws Exception {

		BugFixRunner r = new BugFixRunner();
		Diff diffRemove = null;
		diffRemove = r.getdiff(t, s);
		Assert.assertEquals(1, diffRemove.getRootOperations().size());
		Assert.assertTrue(diffRemove.getRootOperations().get(0) instanceof DeleteOperation);

		List<Operation> opsr = diffRemove.getRootOperations();
		DiffICSE15ContextAnalyzer analyzer = new DiffICSE15ContextAnalyzer();
		Map<String, Diff> opsByFile = new HashMap<>();
		opsByFile.put("filetest", diffRemove);
		JSONObject js = analyzer.calculateCntxJSON("filetest", opsByFile);
		System.out.println("out:\n" + js);

		for (Operation operation : opsr) {
			System.out.println("**src: " + operation.getSrcNode());
			System.out.println("**trg: " + operation.getDstNode());
			System.out.println("**node: " + operation.getNode());
		}
	}

	public File getFile(String name) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(name).getFile());
		return file;
	}
}
