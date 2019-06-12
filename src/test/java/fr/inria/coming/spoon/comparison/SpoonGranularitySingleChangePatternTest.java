package fr.inria.coming.spoon.comparison;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ParentPatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.GranuralityType;
import gumtree.spoon.diff.Diff;

/**
 * Test simple change pattern ocurrences from two hunks of codes
 * 
 * @author Matias Martinez matias.martinez@inria.fr
 *
 */
public class SpoonGranularitySingleChangePatternTest {

	/**
	 * Failing the root action filtering.
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore // TODO: to see
	public void assignmentTest() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = b ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = c + d; " + " return null;" + "}};";

		PatternEntity parent_e = new PatternEntity("Assignment");

		PatternEntity affected_e = new PatternEntity("*", new ParentPatternEntity(parent_e, 1));

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);

		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest2() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a,b = 1,c,d; a = b ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a,b = 1 ,c,d = 1; a = b + d; " + " return null;" + "}};";

		// PatternFilter patternFilter = new PatternFilter("*", ActionType.ANY);
		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest3() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a,b= 1,c= 1,d; a = b + c; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a,b= 1,c,d; a = b ; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest4() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a,b = 1,c,d; a = - b ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a,b = 1,c,d; a = b ; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest5() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1;" + " a = a + b * c +d ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1;" + " a = a + b / c +d  ; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest6() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = a + b / c +d ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = a + b / a + d  ; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest8() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = a + b / d +5 ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + "a = a + b / d + c  ; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

	@Test
	public void assignmentTest7() throws Exception {
		String contentLeft = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + "a = a + b / c +d ; " + " return null;" + "}};";

		String contentRigh = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1; " + " a = a + b / c + d + (a+b+d+c)  ; " + " return null;" + "}};";

		FineGrainDifftAnalyzer fineGrainAnalyzer = new FineGrainDifftAnalyzer();

		Diff diff = fineGrainAnalyzer.compare(contentLeft, contentRigh, GranuralityType.SPOON);
		System.out.println("" + diff.getRootOperations());
		System.out.println();
		assertTrue(diff.getRootOperations().size() > 0);
	}

}
