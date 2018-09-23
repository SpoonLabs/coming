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
import fr.inria.coming.changeminer.analyzer.patternspecification.EntityRelation;
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
	Diff diffUpdate = null;
	Diff diffInsert = null;

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
		diffUpdate = r.getdiff(s, t);
		System.out.println("Output: " + diffUpdate);
		Assert.assertEquals(1, diffUpdate.getRootOperations().size());

		s = getFile("patterns_examples/case2/1205753_EmbedPooledConnection_0_s.java");
		t = getFile("patterns_examples/case2/1205753_EmbedPooledConnection_0_t.java");
		diffInsert = r.getdiff(s, t);
		System.out.println("Output: " + diffInsert);
		Assert.assertEquals(1, diffInsert.getRootOperations().size());

	}

	@Test
	public void testDiff1Mapings1() throws Exception {

		List<Operation> actions = diffUpdate.getRootOperations();
		// result.debugInformation();
		assertEquals(1, actions.size());
		// assertTrue(diffUpdate.containsOperation(OperationKind.Update,
		// "VariableRead", "p"));
		UpdateOperation updateOp = (UpdateOperation) actions.get(0);
		CtElement dst = updateOp.getDstNode();
		assertNotNull(dst);
		assertEquals((dst).toString(), "p");
	}

	@Test
	public void testDiff1Mapings2() throws Exception {
		// Step 1 testing
		System.out.println("Case 1");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(PatternEntity.ANY_ENTITY, ActionType.UPD));

		assertPattern(diffUpdate, pattern);

	}

	@Test
	public void testDiff1Mapings3() throws Exception {
		System.out.println("Case 2");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("VariableRead"), ActionType.UPD));

		assertPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1Mapings4() throws Exception {
		System.out.println("Case 3");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("VariableRead", "p"), ActionType.UPD));

		assertPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1Mapings5() throws Exception {
		System.out.println("Case 4");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("*", "p"), ActionType.UPD));
		assertPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1Mapings6() throws Exception {
		System.out.println("Case 5");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		pattern.addChange(new PatternAction(new PatternEntity("*", "d"), ActionType.UPD));
		assertNoPattern(diffUpdate, pattern);

	}

	@Test
	public void testDiff1MapingsParent() throws Exception {

		// Step 1 testing
		System.out.println("Case 1a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entity = new PatternEntity("*");
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffUpdate, pattern);

	}

	@Test
	public void testDiff1MapingsParent2() throws Exception {

		System.out.println("Case 1b");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entity = new PatternEntity("VariableRead");
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1MapingsParent3() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Assignment");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 1);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1MapingsParent4() throws Exception {

		System.out.println("Case 2b false");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Invocation");// no invocation
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 1);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffUpdate, pattern);

	}

	@Test
	public void testDiff1MapingsParent3b() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Assignment");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffUpdate, pattern);
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
		assertNoPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1MapingsParent3GrandParent() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Constructor");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 3);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffUpdate, pattern);
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
		assertPattern(diffUpdate, pattern);
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
		assertNoPattern(diffUpdate, pattern);
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
		assertPattern(diffUpdate, pattern);
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
		assertNoPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1MapingsMultipleValues1() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Assignment", "username = p");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1MapingsMultipleValues2() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity parentEntity = new PatternEntity("Assignment", "usernameOther = p");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff1MapingsParent3GrandParentNoLevel() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("Constructor");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);
		pattern.addChange(new PatternAction(entity, ActionType.UPD));
		assertNoPattern(diffUpdate, pattern);
	}

	@Test
	public void testDiff2() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Class");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 4);

		PatternEntity middleparentEntity = new PatternEntity("Constructor", gradparentWrapper);
		ParentPatternEntity middleparentWrapper = new ParentPatternEntity(middleparentEntity, 3);

		PatternEntity parentEntity = new PatternEntity("If", middleparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("*", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		assertPattern(diffInsert, pattern);
	}

	@Test
	public void testDiff2Invocation() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Class");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 5);

		PatternEntity parentEntity = new PatternEntity("If", gradparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		assertPattern(diffInsert, pattern);
	}

	@Test
	public void testDiff2IValue() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Class");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 5);

		PatternEntity parentEntity = new PatternEntity("If", gradparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);
		entity.setNewValue("isActive");

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		assertPattern(diffInsert, pattern);
	}

	@Test
	public void testDiff2NoValue() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Class");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 5);

		PatternEntity parentEntity = new PatternEntity("If", gradparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);
		entity.setNewValue("NotisActive");// wrong value

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		assertNoPattern(diffInsert, pattern);
	}

	@Test
	public void testDiff2Relation() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();

		PatternEntity grandparentEntity = new PatternEntity("Class");
		ParentPatternEntity gradparentWrapper = new ParentPatternEntity(grandparentEntity, 5);

		PatternEntity parentEntity = new PatternEntity("If", gradparentWrapper);
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));

		List<EntityRelation> relations = pattern.calculateRelations();
		assertEquals(0, relations.size());
	}

	@Test
	public void testDiff2Actions2Relation() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		// TODO: check with grandparent
		PatternEntity parentEntity = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		pattern.addChange(new PatternAction(parentEntity, ActionType.INS));

		List<EntityRelation> relations = pattern.calculateRelations();
		for (EntityRelation entityRelation : relations) {
			System.out.println(entityRelation);
		}

		assertEquals(1, relations.size());

		EntityRelation relation = relations.get(0);
		assertEquals(parentEntity, relation.getEntity());
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
