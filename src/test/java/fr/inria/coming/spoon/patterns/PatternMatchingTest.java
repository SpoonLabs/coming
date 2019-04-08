package fr.inria.coming.spoon.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.DetectorChangePatternInstanceEngine;
import fr.inria.coming.changeminer.analyzer.instancedetector.MatchingAction;
import fr.inria.coming.changeminer.analyzer.instancedetector.ResultMapping;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.EntityRelation;
import fr.inria.coming.changeminer.analyzer.patternspecification.ParentPatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternRelations;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

/**
 * 
 * 
 * @author Matias Martinez
 *
 */
public class PatternMatchingTest {
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
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		diffUpdate = r.getDiff(s, t);
		System.out.println("Output: " + diffUpdate);
		Assert.assertEquals(1, diffUpdate.getRootOperations().size());

		s = getFile("patterns_examples/case2/1205753_EmbedPooledConnection_0_s.java");
		t = getFile("patterns_examples/case2/1205753_EmbedPooledConnection_0_t.java");
		diffInsert = r.getDiff(s, t);
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

		List<EntityRelation> relations = pattern.calculateRelations().getRelations();
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

		List<EntityRelation> relations = pattern.calculateRelations().getRelations();
		for (EntityRelation entityRelation : relations) {
			System.out.println(entityRelation);
		}

		assertEquals(1, relations.size());

		EntityRelation relation = relations.get(0);
		assertEquals(parentEntity, relation.getEntity());
	}

	@Test
	public void testAllCombinations1() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		// TODO: check with grandparent
		PatternEntity parentEntity = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		pattern.addChange(new PatternAction(parentEntity, ActionType.INS));

		List<EntityRelation> relations = pattern.calculateRelations().getRelations();
		for (EntityRelation entityRelation : relations) {
			System.out.println(entityRelation);
		}

		assertEquals(1, relations.size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diffInsert);
		assertFalse(mappings.getMappings().isEmpty());
		List<ChangePatternInstance> allcom = detector.allCombinations(pattern, mappings.getMappings());
		System.out.println("all com " + allcom);
		assertTrue(allcom.size() > 0);

	}

	@Test
	public void testDiff2Linking1() throws Exception {

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		pattern.addChange(new PatternAction(parentEntity, ActionType.INS));

		List<EntityRelation> relations = pattern.calculateRelations().getRelations();
		for (EntityRelation entityRelation : relations) {
			System.out.println(entityRelation);
		}

		assertEquals(1, relations.size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diffInsert);
		assertFalse(mappings.getMappings().isEmpty());
		List<ChangePatternInstance> linkedInstances = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		System.out.println("all com " + linkedInstances);
		assertTrue(linkedInstances.size() > 0);
		assertEquals(2, linkedInstances.get(0).getActions().size());
	}

	@Test
	public void testDiff3Linking1() throws Exception {

		File s = getFile("patterns_examples/case3/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case3/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diffInsertUpdate = r.getDiff(s, t);
		System.out.println("Output: " + diffInsert);
		Assert.assertEquals(2, diffInsertUpdate.getRootOperations().size());

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		pattern.addChange(new PatternAction(parentEntity, ActionType.INS));

		// New one, not connected to the rest
		PatternEntity entityPassword = new PatternEntity("VariableRead");
		pattern.addChange(new PatternAction(entityPassword, ActionType.UPD));

		List<EntityRelation> relations = pattern.calculateRelations().getRelations();
		for (EntityRelation entityRelation : relations) {
			System.out.println(entityRelation);
		}

		assertEquals(1, relations.size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diffInsertUpdate);
		assertFalse(mappings.getMappings().isEmpty());
		assertTrue(mappings.getNotMapped().isEmpty());
		List<ChangePatternInstance> linkedInstances = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		System.out.println("all com " + linkedInstances);
		assertTrue(linkedInstances.size() > 0);
		assertEquals(3, linkedInstances.get(0).getActions().size());

	}

	@Test
	public void testDiff3NotPatternMatching() throws Exception {

		File s = getFile("patterns_examples/case3/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case3/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diffInsertUpdate = r.getDiff(s, t);

		System.out.println("Case 2a");
		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		pattern.addChange(new PatternAction(entity, ActionType.INS));
		pattern.addChange(new PatternAction(parentEntity, ActionType.INS));

		// New one, not connected to the rest
		PatternEntity entityPassword = new PatternEntity("FieldRead", parentWrapper);
		pattern.addChange(new PatternAction(entityPassword, ActionType.UPD));

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diffInsertUpdate);
		assertFalse(mappings.getMappings().isEmpty());
		assertFalse(mappings.getNotMapped().isEmpty());

	}

	@Test
	public void testDiff4Pattern1_readfiled_with2Instances() throws Exception {

		System.out.println("Case 2 instances");
		File s = getFile("patterns_examples/case4/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case4/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diff2Inserts = r.getDiff(s, t);
		System.out.println(diff2Inserts.getRootOperations());

		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity parentEntity = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(parentEntity, 2);
		PatternEntity entity = new PatternEntity("FieldRead", parentWrapper);

		PatternAction paInsertRf = new PatternAction(entity, ActionType.INS);
		pattern.addChange(paInsertRf);
		PatternAction paInsertIf = new PatternAction(parentEntity, ActionType.INS);
		pattern.addChange(paInsertIf);

		PatternRelations calculateRelations = pattern.calculateRelations();
		assertEquals(2, calculateRelations.getPaEntity().size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertRf).size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertIf).size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff2Inserts);
		assertFalse(mappings.getMappings().isEmpty());
		assertTrue(mappings.getNotMapped().isEmpty());

		List<ChangePatternInstance> allCombinations = detector.allCombinations(pattern, mappings.getMappings());
		for (ChangePatternInstance changePatternInstance : allCombinations) {
			System.out.println("-->" + changePatternInstance.getActions());
		}
		// 2 X 2
		assertEquals(4, allCombinations.size());

		List<ChangePatternInstance> linkedInstances = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		// Instances
		assertEquals(2, linkedInstances.size());
		// Actions per instance

		assertEquals(2, linkedInstances.get(0).getActions().size());
		System.out.println("final matching:");
		for (ChangePatternInstance finalInstance : linkedInstances) {
			System.out.println("-fi->\n " + finalInstance);
		}

	}

	@Test
	public void testDiff4Pattern2_Assingnemnt_with2Instances() throws Exception {
		// Assignment on if
		System.out.println("Case 2 instances");
		File s = getFile("patterns_examples/case4/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case4/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diff2Inserts = r.getDiff(s, t);
		System.out.println(diff2Inserts.getRootOperations());

		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entityIf = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(entityIf, 3);

		PatternEntity entityAssignement = new PatternEntity("Assignment", parentWrapper);

		PatternAction paInsertRf = new PatternAction(entityAssignement, ActionType.INS);
		pattern.addChange(paInsertRf);
		PatternAction paInsertIf = new PatternAction(entityIf, ActionType.INS);
		pattern.addChange(paInsertIf);

		PatternRelations calculateRelations = pattern.calculateRelations();
		assertEquals(2, calculateRelations.getPaEntity().size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertRf).size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertIf).size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff2Inserts);
		assertFalse(mappings.getMappings().isEmpty());
		assertTrue(mappings.getNotMapped().isEmpty());

		List<ChangePatternInstance> allCombinations = detector.allCombinations(pattern, mappings.getMappings());
		for (ChangePatternInstance changePatternInstance : allCombinations) {
			System.out.println("-->" + changePatternInstance.getActions());
		}
		// 2 X 2
		assertEquals(4, allCombinations.size());

		List<ChangePatternInstance> linkedInstances = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		// Instances
		assertEquals(2, linkedInstances.size());
		// Actions per instance

		assertEquals(2, linkedInstances.get(0).getActions().size());
		System.out.println("final matching:");
		for (ChangePatternInstance finalInstance : linkedInstances) {
			System.out.println("-fi->\n " + finalInstance);
		}

	}

	@Test
	public void testDiff5Pattern_assignment_1_instance() throws Exception {
		// Assignment on if
		System.out.println("Case 2 instances");
		File s = getFile("patterns_examples/case5/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case5/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diff2Inserts = r.getDiff(s, t);
		System.out.println(diff2Inserts.getRootOperations());

		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entityIf = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(entityIf, 3);

		PatternEntity entityAssignement = new PatternEntity("Assignment", parentWrapper);

		PatternAction paInsertAssignment = new PatternAction(entityAssignement, ActionType.INS);
		pattern.addChange(paInsertAssignment);
		PatternAction paInsertIf = new PatternAction(entityIf, ActionType.INS);
		pattern.addChange(paInsertIf);

		PatternRelations calculateRelations = pattern.calculateRelations();
		assertEquals(2, calculateRelations.getPaEntity().size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertAssignment).size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertIf).size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff2Inserts);
		assertFalse(mappings.getMappings().isEmpty());
		assertTrue(mappings.getNotMapped().isEmpty());

		List<ChangePatternInstance> allCombinations = detector.allCombinations(pattern, mappings.getMappings());
		for (ChangePatternInstance changePatternInstance : allCombinations) {
			System.out.println("-->" + changePatternInstance.getActions());
		}

		List<ChangePatternInstance> linkedInstances = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		// Instances
		assertEquals(1, linkedInstances.size());
		// Actions per instance

		assertEquals(2, linkedInstances.get(0).getActions().size());
		System.out.println("final matching:");
		for (ChangePatternInstance finalInstance : linkedInstances) {
			System.out.println("-fi->\n " + finalInstance);
		}
		ChangePatternInstance instance = linkedInstances.get(0);
		MatchingAction maInvo = instance.getMapping().get(paInsertAssignment);
		assertTrue(maInvo.getMatching().get(0).getAffectedNode() instanceof CtAssignment);
	}

	@Test
	public void testDiff5Pattern_invocation_1_instance() throws Exception {

		System.out.println("Case 2 instances");
		File s = getFile("patterns_examples/case5/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case5/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diff2Inserts = r.getDiff(s, t);
		System.out.println(diff2Inserts.getRootOperations());

		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entityIf = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(entityIf, 3);

		PatternEntity entityInvocation = new PatternEntity("Invocation", parentWrapper);

		PatternAction paInsertInvocation = new PatternAction(entityInvocation, ActionType.INS);
		pattern.addChange(paInsertInvocation);
		PatternAction paInsertIf = new PatternAction(entityIf, ActionType.INS);
		pattern.addChange(paInsertIf);

		PatternRelations calculateRelations = pattern.calculateRelations();
		assertEquals(2, calculateRelations.getPaEntity().size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertInvocation).size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertIf).size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff2Inserts);
		assertFalse(mappings.getMappings().isEmpty());
		assertTrue(mappings.getNotMapped().isEmpty());

		List<ChangePatternInstance> allCombinations = detector.allCombinations(pattern, mappings.getMappings());
		for (ChangePatternInstance changePatternInstance : allCombinations) {
			System.out.println("-->" + changePatternInstance.getActions());
		}

		List<ChangePatternInstance> linkedInstances = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		// Instances
		assertEquals(1, linkedInstances.size());
		// Actions per instance

		assertEquals(2, linkedInstances.get(0).getActions().size());
		System.out.println("final matching:");
		for (ChangePatternInstance finalInstance : linkedInstances) {
			System.out.println("-fi->\n " + finalInstance);
		}

		ChangePatternInstance instance = linkedInstances.get(0);
		MatchingAction maInvo = instance.getMapping().get(paInsertInvocation);
		assertTrue(maInvo.getMatching().get(0).getAffectedNode() instanceof CtInvocation);
	}

	@Test
	public void testDiff5_Two_patterns_1_instance_each() throws Exception {
		// Method Invocation on if
		System.out.println("Case 2 instances");
		File s = getFile("patterns_examples/case5/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("patterns_examples/case5/1205753_EmbedPooledConnection_0_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();

		Diff diff2Inserts = r.getDiff(s, t);
		System.out.println(diff2Inserts.getRootOperations());

		ChangePatternSpecification pattern = new ChangePatternSpecification();
		PatternEntity entityIf = new PatternEntity("If");
		ParentPatternEntity parentWrapper = new ParentPatternEntity(entityIf, 3);

		// Pattern 1: if-assignment
		PatternEntity entityAssignement = new PatternEntity("Assignment", parentWrapper);
		PatternAction paInsertAssignment = new PatternAction(entityAssignement, ActionType.INS);
		pattern.addChange(paInsertAssignment);
		PatternAction paInsertIf = new PatternAction(entityIf, ActionType.INS);
		pattern.addChange(paInsertIf);

		PatternRelations calculateRelations = pattern.calculateRelations();
		assertEquals(2, calculateRelations.getPaEntity().size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertAssignment).size());
		assertEquals(1, calculateRelations.getPaEntity().get(paInsertIf).size());

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff2Inserts);
		assertFalse(mappings.getMappings().isEmpty());
		assertTrue(mappings.getNotMapped().isEmpty());

		List<ChangePatternInstance> allCombinations = detector.allCombinations(pattern, mappings.getMappings());
		for (ChangePatternInstance changePatternInstance : allCombinations) {
			System.out.println("-->" + changePatternInstance.getActions());
		}

		List<ChangePatternInstance> linkedInstancesPattern1 = detector.calculateValidInstancesFromMapping(pattern,
				mappings.getMappings());
		// Instances
		assertEquals(1, linkedInstancesPattern1.size());
		// Actions per instance
		assertEquals(2, linkedInstancesPattern1.get(0).getActions().size());
		System.out.println("final matching:");
		for (ChangePatternInstance finalInstance : linkedInstancesPattern1) {
			System.out.println("-fi->\n " + finalInstance);
		}

		System.out.println("******Pattern 2*****");

		// Pattern 2- If with invication
		ChangePatternSpecification pattern2 = new ChangePatternSpecification();
		PatternEntity anotherEntityIf = new PatternEntity("If");
		ParentPatternEntity anotherParentWrapper = new ParentPatternEntity(anotherEntityIf, 3);
		PatternEntity entityInvocation = new PatternEntity("Invocation", anotherParentWrapper);

		PatternAction paInsertInvocation = new PatternAction(entityInvocation, ActionType.INS);
		pattern2.addChange(paInsertInvocation);
		PatternAction paAnotherInsertIf = new PatternAction(anotherEntityIf, ActionType.INS);
		pattern2.addChange(paAnotherInsertIf);

		detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappingsp2 = detector.mappingActions(pattern2, diff2Inserts);
		assertFalse(mappingsp2.getMappings().isEmpty());
		assertTrue(mappingsp2.getNotMapped().isEmpty());

		List<ChangePatternInstance> linkedInstancesPattern2 = detector.calculateValidInstancesFromMapping(pattern2,
				mappingsp2.getMappings());
		assertEquals(1, linkedInstancesPattern2.size());

		/// Now, checking entities
		ChangePatternInstance instanceIfAssignment = linkedInstancesPattern1.get(0);
		MatchingAction maAssigb = instanceIfAssignment.getMapping().get(paInsertAssignment);
		assertTrue(maAssigb.getMatching().get(0).getAffectedNode() instanceof CtAssignment);

		ChangePatternInstance instanceIfInvocation = linkedInstancesPattern2.get(0);
		MatchingAction maInvo = instanceIfInvocation.getMapping().get(paInsertInvocation);
		assertTrue(maInvo.getMatching().get(0).getAffectedNode() instanceof CtInvocation);

		// Now, lets check the poited If
		MatchingAction maifpattern1 = instanceIfAssignment.getMapping().get(paInsertIf);
		MatchingAction maifpattern2 = instanceIfInvocation.getMapping().get(paAnotherInsertIf);

		CtElement affectedNodeIfpattern1 = maifpattern1.getMatching().get(0).getAffectedNode();
		CtElement affectedNodeIfpattern2 = maifpattern2.getMatching().get(0).getAffectedNode();
		assertTrue(affectedNodeIfpattern1 instanceof CtIf);
		assertTrue(affectedNodeIfpattern2 instanceof CtIf);
		assertTrue(affectedNodeIfpattern1 != affectedNodeIfpattern2);
		assertNotEquals(affectedNodeIfpattern1, affectedNodeIfpattern2);
	}

	@Test
	public void testPatternInstanceMath3() throws Exception {
		File fl = new File(getClass().getResource("/pattern_specification/pattern_test_5_1_Actions2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("casesDj4/math_3/MathArrays_s.java");
		File t = getFile("casesDj4/math_3/MathArrays_t.java");
		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertEquals(1, diff.getRootOperations().size());

		assertPattern(diff, pattern);
	}

	@Test
	public void testPatternInstanceToInstanceIssue38() throws Exception {

		File s = getFile("testInsert4/1205753/EmbedPooledConnection/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("testInsert4/1205753/EmbedPooledConnection/1205753_EmbedPooledConnection_0_t.java");

		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);
		System.out.println("Output: " + diff);
		Assert.assertEquals(1, diff.getRootOperations().size());

		File fl = new File(getClass().getResource("/pattern_specification/pattern_INS_2TypeRef.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		assertNotNull(pattern);

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff);
		assertTrue(mappings.getMappings().size() > 0);
		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diff);
		System.out.println("nr instances: " + instances.size());
		assertTrue(instances.size() == 0);

	}

	public void assertPattern(Diff diffToAnalyze, ChangePatternSpecification pattern) {
		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diffToAnalyze);
		assertTrue(mappings.getMappings().size() > 0);
		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diffToAnalyze);
		assertTrue(instances.size() > 0);
	}

	public void assertNoPattern(Diff diffToAnalyze, ChangePatternSpecification pattern) {
		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diffToAnalyze);
		assertTrue(mappings.getMappings().isEmpty());

	}

	public File getFile(String name) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(name).getFile());
		return file;
	}

	@Test
	public void testMatchingRoleIssue36() throws Exception {
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

		File s = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_t.java");

		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);

		System.out.println("Test result " + diff.getRootOperations());

		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff);
		assertTrue(mappings.getMappings().size() > 0);
		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diff);
		System.out.println("nr instances: " + instances.size());
		assertEquals(2, instances.size());

		System.out.println(instances);
	}

	@Test
	public void testMatchingRoleIssue36_1_matching_binop() throws Exception {
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());
		// add role
		assertEquals(2, pattern.getAbstractChanges().get(0).getAffectedEntity().getId());

		assertEquals(1,
				pattern.getAbstractChanges().get(0).getAffectedEntity().getParentPatternEntity().getParent().getId());

		pattern.getAbstractChanges().get(0).getAffectedEntity()// .getParentPatternEntity().getParent()
				.setRoleInParent(CtRole.CONDITION.name());

		File s = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_t.java");

		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);

		System.out.println("Test result " + diff.getRootOperations());

		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff);
		assertTrue(mappings.getMappings().size() > 0);

		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diff);
		System.out.println("nr instances: " + instances.size());
		System.out.println(instances);

		assertEquals(1, instances.size());

		ChangePatternInstance instancefound = instances.get(0);

	}

	@Test
	public void testMatchingRoleIssue36_1_role_matching_binop() throws Exception {
		// role specified in file
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if_role.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());
		// add role
		assertEquals(2, pattern.getAbstractChanges().get(0).getAffectedEntity().getId());

		assertEquals(1,
				pattern.getAbstractChanges().get(0).getAffectedEntity().getParentPatternEntity().getParent().getId());

		File s = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_t.java");

		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);

		System.out.println("Test result " + diff.getRootOperations());

		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff);
		assertTrue(mappings.getMappings().size() > 0);

		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diff);
		System.out.println("nr instances: " + instances.size());
		System.out.println(instances);

		assertEquals(1, instances.size());

	}

	@Test
	public void testMatchingRoleIssue36_2_matching_inv() throws Exception {
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if2.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());
		// add role
		assertEquals(2, pattern.getAbstractChanges().get(0).getAffectedEntity().getId());
		PatternEntity parent = pattern.getAbstractChanges().get(0).getAffectedEntity().getParentPatternEntity()
				.getParent();
		assertEquals(3, parent.getId());

		assertEquals(1, parent.getParentPatternEntity().getParent().getId());

		//
		parent.setRoleInParent(CtRole.THEN.name());
		// parent.setEntityType(PatternEntity.ANY);

		File s = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_t.java");

		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);

		System.out.println("Test result " + diff.getRootOperations());

		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff);
		assertTrue(mappings.getMappings().size() > 0);

		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diff);
		System.out.println("nr instances: " + instances.size());
		System.out.println(instances);

		assertEquals(1, instances.size());

	}

	@Test
	public void testMatchingRoleIssue36_2_role_matching_inv() throws Exception {
		// role specified in parent
		File fl = new File(getClass().getResource("/pattern_specification/pattern_modif_if2_role.xml").getFile());

		ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());
		// add role
		assertEquals(2, pattern.getAbstractChanges().get(0).getAffectedEntity().getId());
		PatternEntity parent = pattern.getAbstractChanges().get(0).getAffectedEntity().getParentPatternEntity()
				.getParent();
		assertEquals(3, parent.getId());

		assertEquals(1, parent.getParentPatternEntity().getParent().getId());

		File s = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_s.java");
		File t = getFile("diffcases/diffroleinparent1/1205753_EmbedPooledConnection_0_t.java");

		FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
		Diff diff = r.getDiff(s, t);

		System.out.println("Test result " + diff.getRootOperations());

		Assert.assertTrue(diff.getRootOperations().size() > 0);

		assertPattern(diff, pattern);

		DetectorChangePatternInstanceEngine detector = new DetectorChangePatternInstanceEngine();
		ResultMapping mappings = detector.mappingActions(pattern, diff);
		assertTrue(mappings.getMappings().size() > 0);

		List<ChangePatternInstance> instances = detector.findPatternInstances(pattern, diff);
		System.out.println("nr instances: " + instances.size());
		System.out.println(instances);

		assertEquals(1, instances.size());

	}
}
