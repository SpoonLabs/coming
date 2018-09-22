package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;

import diffanalyzer.BugFixRunner;
import fr.inria.astor.util.MapList;
import fr.inria.coming.changeminer.analyzer.instancedetector.DetectorChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
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
	public void testDiff1Mapings() throws Exception {
		File s = getFile("patterns_examples/case1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case1/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		Diff diffToAnalyze = r.getdiff(s, t);
		System.out.println("Output: " + diffToAnalyze);
		Assert.assertEquals(1, diffToAnalyze.getRootOperations().size());

		List<Operation> actions = diffToAnalyze.getRootOperations();
		// result.debugInformation();
		assertEquals(1, actions.size());
		// assertTrue(diffToAnalyze.containsOperation(OperationKind.Update,
		// "VariableRead", "p"));
		UpdateOperation updateOp = (UpdateOperation) actions.get(0);
		CtElement dst = updateOp.getDstNode();
		assertNotNull(dst);
		assertEquals((dst).toString(), "p");

		// Step 1 testing
		System.out.println("Case 1");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(PatternEntity.ANY_ENTITY, ActionType.UPD));

		assertPattern(diffToAnalyze, pattern);
		System.out.println("Case 2");
		pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("VariableRead"), ActionType.UPD));

		assertPattern(diffToAnalyze, pattern);

		System.out.println("Case 3");
		pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("VariableRead", "p"), ActionType.UPD));

		assertPattern(diffToAnalyze, pattern);

		System.out.println("Case 4");
		pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("*", "p"), ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);

		System.out.println("Case 5");
		pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("*", "d"), ActionType.UPD));
		assertNoPattern(diffToAnalyze, pattern);

	}

	public void assertPattern(Diff diffToAnalyze, ChangePatternSpecification pattern) {
		DetectorChangePatternInstance detector = new DetectorChangePatternInstance();
		MapList<PatternAction, Operation<Action>> mappings = detector.s1mappingActions(pattern, diffToAnalyze);
		assertTrue(mappings.size() > 0);
		System.out.println(mappings);
	}

	public void assertNoPattern(Diff diffToAnalyze, ChangePatternSpecification pattern) {
		DetectorChangePatternInstance detector = new DetectorChangePatternInstance();
		MapList<PatternAction, Operation<Action>> mappings = detector.s1mappingActions(pattern, diffToAnalyze);
		assertTrue(mappings.isEmpty());

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
