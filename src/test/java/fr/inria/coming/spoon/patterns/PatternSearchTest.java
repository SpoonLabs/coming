package fr.inria.coming.spoon.patterns;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import diffanalyzer.BugFixRunner;
import gumtree.spoon.diff.Diff;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * 
 * @author Matias Martinez
 *
 */
public class PatternSearchTest {

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
	public void testDiff1Case1() throws Exception {
		File s = getFile("patterns_examples/case1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case1/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffOut = r.getdiff(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());

		// PatternFilter pattern = new PatternFilter(Entity.ANY, ActionType.ANY,
		// "Assignment", 1);
		// List result = pattern.process(diffOut);
		// assertTrue(result.size() > 0);
	}

	public void compare(CtElement left, CtElement right) {

		// pattern.process(diff);
	}

	public File getFile(String name) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(name).getFile());
		return file;
	}
}
