package fr.inria.coming.spoon.comparison;


import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.DiffResult;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.PatternFilter;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.spoon.treeGenerator.SpoonTreeGenerator;
import fr.labri.gumtree.actions.model.Action;
import fr.labri.gumtree.tree.Tree;

/**
 * Test simple change pattern ocurrences from two hunks of codes
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */
public class SpoonGranularitySingleChangePatternTest {

	@Before
	public void setUp() throws Exception {
		
		//Logger.getLogger(DiffSpoon.class).setLevel(Level.OFF);
		TreeGeneratorRegistry.generators.clear();
		TreeGeneratorRegistry.addGenerator(new SpoonTreeGenerator());
	}
/**
 * Failing the root action filtering.
 * @throws Exception
 */
	@Test
	public void assignmentTest() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = b ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = c + d; "
				+ " return null;" + "}};";
		
		PatternEntity parent_e = new PatternEntity("Assignment");

		PatternEntity affected_e = new PatternEntity("*",parent_e,1);
		
		PatternFilter patternFilter = new PatternFilter(
				affected_e, ActionType.ANY );
		
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		
		List<Action> actions;
		
		actions = patternFilter.process(diff);
		assertTrue(actions.size() > 0);
		System.out.println(actions);
	}

	@Test
	public void assignmentTest2() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = b ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = b + d; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	@Test
	public void assignmentTest3() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = b + c; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = b ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	@Test
	public void assignmentTest4() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = - b ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = b ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	@Test
	public void assignmentTest5() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b * c +d ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / c +d  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	@Test
	public void assignmentTest6() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / c +d ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / a + d  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	
	@Test
	public void assignmentTest8() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / d +5 ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / d + c  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	
	@Test
	public void assignmentTest7() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / c +d ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b,c,d; a = a + b / c + d + (a+b+d+c)  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		DiffResult diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	public void printParents(Tree parent){
		System.out.println("-");
		while(parent!= null){
			System.out.println(parent.getTypeLabel());
			parent = parent.getParent();
		}
		
	}
}
