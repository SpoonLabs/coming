package fr.inria.coming.spoon.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;

import com.github.gumtreediff.matchers.Matcher;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.filter.commitmessage.BugfixKeywordsFilter;
import fr.inria.coming.core.filter.commitmessage.KeyWordsMessageFilter;
import fr.inria.coming.main.ComingMain;
import gumtree.spoon.diff.Diff;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class MainTest {

	@Before
	public void setUp() throws Exception {

		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(console);

		java.util.logging.Logger.getLogger("fr.labri.gumtree.matchers").setLevel(java.util.logging.Level.OFF);
		Matcher.LOGGER.setLevel(java.util.logging.Level.OFF);
	}

	@Test
	public void testListEntities() {

		ComingMain.main(new String[] { "-showentities" });// todo
	}

	@Test
	public void testListActions() {

		ComingMain.main(new String[] { "-showactions" });
	}

	@Test
	public void testMineBinaryOperatorMain() {
		ComingMain.main(
				new String[] { "-location", "repogit4testv0", "-entitytype", "BinaryOperator", "-action", "INS" });
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUsingRun() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;
		Map<Commit, RevisionResult> commits = cfres.getAllResults();

		Commit c1 = commits.keySet().stream()
				.filter(e -> e.getName().equals("4120ab0c714911a9c9f26b591cb3222eaf57d127")).findFirst().get();
		DiffResult<Commit> diff1 = (DiffResult<Commit>) commits.get(c1)
				.getResultFromClass(FineGrainDifftAnalyzer.class);

		assertTrue(diff1.getAllOps().size() > 0);

		boolean hasRootOp = false;
		// Assert one diff with +1 root op.
		for (Diff diff : diff1.getAllOps()) {
			hasRootOp |= !(diff.getRootOperations().isEmpty());
		}
		assertTrue(hasRootOp);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilter_bugfix() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;

		List<IFilter> filters = cm.getExperiment().getFilters();

		assertTrue(filters.isEmpty());

		result = cm.run(new String[] { "-location", "repogit4testv0", "-filter", "bugfix" });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		cfres = (CommitFinalResult) result;

		filters = cm.getExperiment().getFilters();

		assertFalse(filters.isEmpty());
		assertEquals(1, filters.size());
		BugfixKeywordsFilter bffilter = (BugfixKeywordsFilter) filters.stream()
				.filter(e -> e instanceof BugfixKeywordsFilter).findFirst().get();
		assertNotNull(bffilter);

		for (Commit c : cfres.getAllResults().keySet()) {
			assertTrue(bffilter.accept(c));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilter_keyword() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;

		List<IFilter> filters = cm.getExperiment().getFilters();

		assertTrue(filters.isEmpty());

		result = cm.run(
				new String[] { "-location", "repogit4testv0", "-filter", "keywords", "-filtervalue", "precondition" });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		cfres = (CommitFinalResult) result;

		filters = cm.getExperiment().getFilters();

		assertFalse(filters.isEmpty());
		assertEquals(1, filters.size());
		KeyWordsMessageFilter kwfilter = (KeyWordsMessageFilter) filters.stream()
				.filter(e -> e instanceof KeyWordsMessageFilter).findFirst().get();
		assertNotNull(kwfilter);

		for (Commit c : cfres.getAllResults().keySet()) {
			assertTrue(kwfilter.accept(c));
			assertEquals("fe76517014e580ddcb40ac04ea824d54ba741c8b", (c.getName().toString()));
		}
	}

}
