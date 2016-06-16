package fr.inria.coming.spoon.comparison;


import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.PatternFilter;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.spoon.diffSpoon.CtDiff;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;

/**
 * Test simple change pattern ocurrences from two hunks of codes
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */
public class SpoonGranularitySingleChangePatternTest {


/**
 * Failing the root action filtering.
 * @throws Exception
 */
	@Test
	@Ignore //TODO: to see
	public void assignmentTest() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ " a = b ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ " a = c + d; "
				+ " return null;" + "}};";
		
		PatternEntity parent_e = new PatternEntity("Assignment");

		PatternEntity affected_e = new PatternEntity("*",parent_e,1);
		
		PatternFilter patternFilter = new PatternFilter(
				affected_e, ActionType.ANY );
		
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		
		List<Action> actions;
		
		actions = patternFilter.process(diff);
		assertTrue(actions.size() > 0);
		System.out.println(actions);
	}

	@Test
	public void assignmentTest2() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b = 1,c,d; a = b ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b = 1 ,c,d = 1; a = b + d; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	@Test
	public void assignmentTest3() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b= 1,c= 1,d; a = b + c; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b= 1,c,d; a = b ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	@Test
	public void assignmentTest4() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b = 1,c,d; a = - b ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a,b = 1,c,d; a = b ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	@Test
	public void assignmentTest5() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1;"
				+ " a = a + b * c +d ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1;"
				+ " a = a + b / c +d  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	@Test
	public void assignmentTest6() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ " a = a + b / c +d ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ " a = a + b / a + d  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	
	@Test
	public void assignmentTest8() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ " a = a + b / d +5 ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ "a = a + b / d + c  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	
	@Test
	public void assignmentTest7() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ "a = a + b / c +d ; "
				+ " return null;" + "}};";
		
		
		String contentRigh = "" + "class X {" + "public Object foo() {"
				+ " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; "
				+ " a = a + b / c + d + (a+b+d+c)  ; "
				+ " return null;" + "}};";
		
		PatternFilter patternFilter = new PatternFilter(
				"*", ActionType.ANY);
		FineGrainChangeCommitAnalyzer 	fineGrainAnalyzer = new FineGrainChangeCommitAnalyzer(patternFilter,GranuralityType.SPOON );
		
		CtDiff diff= fineGrainAnalyzer.compareContent(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println(""+diff.getRootActions());
		System.out.println();
	}
	
	
}
