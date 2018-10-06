package fr.inria.coming.spoon.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.HunkDifftAnalyzer;
import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.HunkDiff;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.filter.commitmessage.BugfixKeywordsFilter;
import fr.inria.coming.core.filter.commitmessage.KeyWordsMessageFilter;
import fr.inria.coming.core.filter.diff.NbHunkFilter;
import fr.inria.coming.core.filter.files.CommitSizeFilter;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.spoon.core.dummies.MyTestFilter;
import fr.inria.coming.spoon.core.dummies.MyTestInput;
import fr.inria.coming.spoon.core.dummies.MyTestOutput;
import fr.inria.main.CommandSummary;
import gumtree.spoon.diff.Diff;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class MainComingTest {

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

		ComingMain.main(new String[] { "-showentities" });
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
	public void testDiffAnalysis() throws Exception {
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

		assertTrue(diff1.getAll().size() > 0);

		boolean hasRootOp = false;
		// Assert one diff with +1 root op.
		for (Diff diff : diff1.getAll()) {
			hasRootOp |= !(diff.getRootOperations().isEmpty());
		}
		assertTrue(hasRootOp);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHunkAnalysis() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", "-hunkanalysis", "true" });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;
		Map<Commit, RevisionResult> commits = cfres.getAllResults();

		// Case 1
		int nrHunks = 2;
		String commitId = "6dac8ae81bd03bcae1e1fade064d3bb03de472c0";
		assertCommit(commits, nrHunks, commitId);

		nrHunks = 1;
		commitId = "4120ab0c714911a9c9f26b591cb3222eaf57d127";
		assertCommit(commits, nrHunks, commitId);

	}

	@SuppressWarnings("unchecked")
	public void assertCommit(Map<Commit, RevisionResult> commits, int nrHunks, String commitId) {
		Commit c1 = commits.keySet().stream().filter(e -> e.getName().equals(commitId)).findFirst().get();

		DiffResult<Commit, HunkDiff> diff1 = (DiffResult<Commit, HunkDiff>) commits.get(c1)
				.getResultFromClass(HunkDifftAnalyzer.class);

		assertNotNull(diff1);
		assertTrue(diff1.getAll().size() > 0);
		for (HunkDiff hunkdiff : diff1.getAll()) {

			assertEquals(nrHunks, hunkdiff.size());
		}
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilter_hunks() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;

		List<IFilter> filters = cm.getExperiment().getFilters();

		assertTrue(filters.isEmpty());

		String[] args = new String[] { "-location", "repogit4testv0", //
				"-filter", "numberhunks",
				//
				"-parameters", "min_nb_hunks:2:max_nb_hunks:2" };

		CommandSummary cs = new CommandSummary(args);
		result = cm.run(cs.flat());
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		cfres = (CommitFinalResult) result;

		filters = cm.getExperiment().getFilters();

		assertEquals(2, ComingProperties.getPropertyInteger("min_nb_hunks").intValue());
		assertEquals(2, ComingProperties.getPropertyInteger("max_nb_hunks").intValue());
		assertFalse(filters.isEmpty());
		assertEquals(1, filters.size());
		NbHunkFilter kwfilter = (NbHunkFilter) filters.stream().filter(e -> e instanceof NbHunkFilter).findFirst()
				.get();
		assertNotNull(kwfilter);

		for (Commit c : cfres.getAllResults().keySet()) {
			assertTrue(kwfilter.accept(c));
			assertEquals("6dac8ae81bd03bcae1e1fade064d3bb03de472c0", (c.getName().toString()));
		}

		// Let's check the nr lines hunks per hunk
		cs.command.put("-parameters", "min_nb_hunks:1:max_nb_hunks:3:max_lines_per_hunk:1");

		result = cm.run(cs.flat());
		assertEquals(1, ComingProperties.getPropertyInteger("min_nb_hunks").intValue());
		assertEquals(3, ComingProperties.getPropertyInteger("max_nb_hunks").intValue());
		assertEquals(1, ComingProperties.getPropertyInteger("max_lines_per_hunk").intValue());
		cfres = (CommitFinalResult) result;
		for (Commit c : cfres.getAllResults().keySet()) {

			assertNotEquals("c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e", (c.getName().toString()));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilter_maxfiles() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;

		List<IFilter> filters = cm.getExperiment().getFilters();

		assertTrue(filters.isEmpty());

		String[] args = new String[] { "-location", "repogit4testv0", //
				"-filter", "maxfiles",
				//
				"-parameters", "max_files_per_commit:1" };

		CommandSummary cs = new CommandSummary(args);
		result = cm.run(cs.flat());
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		cfres = (CommitFinalResult) result;

		filters = cm.getExperiment().getFilters();

		assertEquals(1, ComingProperties.getPropertyInteger("max_files_per_commit").intValue());

		assertFalse(filters.isEmpty());
		assertEquals(1, filters.size());
		CommitSizeFilter kwfilter = (CommitSizeFilter) filters.stream().filter(e -> e instanceof CommitSizeFilter)
				.findFirst().get();
		assertNotNull(kwfilter);

		for (Commit c : cfres.getAllResults().keySet()) {
			assertTrue(kwfilter.accept(c));
			assertNotEquals("01dd29c37f6044d9d1126d9db55a961cccaccfb7", (c.getName().toString()));
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilter_2Filters() throws Exception {
		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		CommitFinalResult cfres = (CommitFinalResult) result;

		List<IFilter> filters = cm.getExperiment().getFilters();

		assertTrue(filters.isEmpty());

		String[] args = new String[] { "-location", "repogit4testv0", //
				"-filter", "maxfiles:numberhunks",
				//
				"-parameters", "max_files_per_commit:1:min_nb_hunks:1:max_nb_hunks:3:max_lines_per_hunk:1" };

		CommandSummary cs = new CommandSummary(args);
		result = cm.run(cs.flat());
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		cfres = (CommitFinalResult) result;

		filters = cm.getExperiment().getFilters();

		assertEquals(1, ComingProperties.getPropertyInteger("max_files_per_commit").intValue());

		assertFalse(filters.isEmpty());
		assertEquals(2, filters.size());
		CommitSizeFilter cmfilter = (CommitSizeFilter) filters.stream().filter(e -> e instanceof CommitSizeFilter)
				.findFirst().get();
		assertNotNull(cmfilter);

		NbHunkFilter kwfilter = (NbHunkFilter) filters.stream().filter(e -> e instanceof NbHunkFilter).findFirst()
				.get();
		assertNotNull(kwfilter);

		for (Commit c : cfres.getAllResults().keySet()) {
			assertTrue(cmfilter.accept(c));
			assertTrue(kwfilter.accept(c));
			assertNotEquals("01dd29c37f6044d9d1126d9db55a961cccaccfb7", (c.getName().toString()));
			assertNotEquals("c6b1cd8204b10c324b92cdc3e44fe3ab6cfb1f5e", (c.getName().toString()));
		}

	}

	@Test
	public void testLoadFilter() throws Exception {

		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", "-filter",
				"maxfiles:numberhunks:" + MyTestFilter.class.getCanonicalName(), "-parameters", "maxrevision:0" });
		assertNotNull(result);
		assertTrue(result instanceof CommitFinalResult);
		List<IFilter> filters = cm.getExperiment().getFilters();

		MyTestFilter cmfilter = (MyTestFilter) filters.stream().filter(e -> e instanceof MyTestFilter).findFirst()
				.get();
		assertNotNull(cmfilter);

		NbHunkFilter kwfilter = (NbHunkFilter) filters.stream().filter(e -> e instanceof NbHunkFilter).findFirst()
				.get();
		assertNotNull(kwfilter);

	}

	@Test
	public void testLoadInput() throws Exception {

		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", "-input", MyTestInput.class.getName(),
				"-parameters", "maxrevision:0" });

		assertTrue(cm.getExperiment() instanceof MyTestInput);

	}

	@Test
	public void testLoadOutput() throws Exception {

		ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", "-outputprocessor",
				MyTestOutput.class.getName(), "-parameters", "maxrevision:0" });

		MyTestOutput cmoutput = (MyTestOutput) cm.getExperiment().getOutputProcessors().stream()
				.filter(e -> e instanceof MyTestOutput).findFirst().get();
		assertNotNull(cmoutput);

	}

}
