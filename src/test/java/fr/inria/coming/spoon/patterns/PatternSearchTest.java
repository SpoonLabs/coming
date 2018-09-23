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
import fr.inria.coming.changeminer.analyzer.patternspecification.ParentPatternEntity;
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
	Diff diffToAnalyze = null;

	@Before
	public void setUp() throws Exception {

		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(console);

		File s = getFile("patterns_examples/case1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case1/1205753_EmbedPooledConnection_0_t.java");
		BugFixRunner r = new BugFixRunner();
		diffToAnalyze = r.getdiff(s, t);
		System.out.println("Output: " + diffToAnalyze);
		Assert.assertEquals(1, diffToAnalyze.getRootOperations().size());
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

	@Test
	public void testDiff1MapingsParent() throws Exception {

		// Step 1 testing
		System.out.println("Case 1a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entity = new PatternEntity("*");
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);

	}

	@Test
	public void testDiff1MapingsParent2() throws Exception {

		System.out.println("Case 1b");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entity = new PatternEntity("VariableRead");
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsParent3() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Assignment");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 1);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsParent4() throws Exception {

		System.out.println("Case 2b false");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Invocation");// no invocation
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 1);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffToAnalyze, pattern);

	}

	@Test
	public void testDiff1MapingsParent3b() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Assignment");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsParent3c() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Method");
		// It's not the parent
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 1);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsParent3GrandParent() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Constructor");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 3);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsMultipleParents1() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Constructor");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 4);

		PatternEntity parentEntity = new PatternEntity("Assignment", gradparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsMultipleParents2() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Invocation");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 4);

		PatternEntity parentEntity = new PatternEntity("Assignment", gradparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsMultipleParents3() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Constructor");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 4);

		PatternEntity middleparentEntity = new PatternEntity("Block", gradparentWrapper);
		ParentPatternEntity middleparentWrapper = new ParentPatternEntity(middleparentEntity, 3);

		PatternEntity parentEntity = new PatternEntity("Assignment", middleparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsMultipleParents4() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Constructor");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 4);

		PatternEntity middleparentEntity = new PatternEntity("Class", gradparentWrapper);
		ParentPatternEntity middleparentWrapper = new ParentPatternEntity(middleparentEntity, 3);

		PatternEntity parentEntity = new PatternEntity("Assignment", middleparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffToAnalyze, pattern);
	}

	@Test
	public void testDiff1MapingsParent3GrandParentNoLevel() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Constructor");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
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
