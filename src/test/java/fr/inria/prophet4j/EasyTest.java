package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

//import gumtree.spoon.AstComparator;
//import gumtree.spoon.diff.Diff;
//import gumtree.spoon.diff.operations.Operation;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtIf;
import spoon.reflect.factory.CoreFactory;

import java.util.StringJoiner;

//import java.io.File;
//import java.util.List;

public class EasyTest {
    @Test
    public void testFeatureLearner() {
        assertEquals(Boolean.TRUE, true);
    }

//    private void compare4MathUtils(int v) {
//        System.out.println(v);
//        StringJoiner stringJoiner = new StringJoiner("\n\t", "factorial : \n\t", "");
//        try {
//            stringJoiner.add(String.valueOf(MathUtils.factorial(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(MathUtilsHuman.factorial(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(MathUtilsPatch.factorial(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        System.out.println(stringJoiner.toString());
//        stringJoiner = new StringJoiner("\n\t", "factorialDouble : \n\t", "");
//        try {
//            stringJoiner.add(String.valueOf(MathUtils.factorialDouble(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(MathUtilsHuman.factorialDouble(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(MathUtilsPatch.factorialDouble(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        System.out.println(stringJoiner.toString());
//        stringJoiner = new StringJoiner("\n\t", "factorialLog : \n\t", "");
//        try {
//            stringJoiner.add(String.valueOf(MathUtils.factorialLog(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(MathUtilsHuman.factorialLog(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(MathUtilsPatch.factorialLog(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        System.out.println(stringJoiner.toString());
//        System.out.println("================");
//    }
//
//    @Test
//    public void testMathUtils() {
//        for (int v = -2; v < 23; v++) {
//            compare4MathUtils(v);
//        }
//    }

//    private void compare4ClassUtils(String v) {
//        System.out.println(v);
//        StringJoiner stringJoiner = new StringJoiner("\n\t", "factorial : \n\t", "");
//        try {
//            stringJoiner.add(String.valueOf(ClassUtils.getShortClassName(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(ClassUtilsHuman.getShortClassName(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(ClassUtilsPatch.getShortClassName(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        System.out.println(stringJoiner.toString());
//        stringJoiner = new StringJoiner("\n\t", "factorial : \n\t", "");
//        try {
//            stringJoiner.add(String.valueOf(ClassUtils.getShortCanonicalName(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(ClassUtilsHuman.getShortCanonicalName(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        try {
//            stringJoiner.add(String.valueOf(ClassUtilsPatch.getShortCanonicalName(v)));
//        } catch (Exception e) {
//            stringJoiner.add(e.toString());
//        }
//        System.out.println(stringJoiner.toString());
//        System.out.println("================");
//    }
//
//    @Test
//    public void testClassUtils() {
//        compare4ClassUtils("[I");
//        compare4ClassUtils("[Ljava.lang.String;");
//        compare4ClassUtils("java.lang.String");
//        compare4ClassUtils("F");
//        compare4ClassUtils("B");
//    }

//    @Test
//    public void testFactory() {
//        CoreFactory factory = new Launcher().getFactory().Core();
//        CtIf IFS = factory.createIf();
//        CtAssignment a = factory.createAssignment();
//        IFS.setThenStatement(a);
//        System.out.println(a.getParent());
//        CtAssignment b = a.clone();
//        System.out.println(b.getParent()); // ParentNotInitializedException
//        assertEquals(Boolean.TRUE, true);
//    }

//    @Test
//    public void testChainSyntax() throws Exception {
//        AstComparator comparator = new AstComparator();
//        File oldFile = new File("src/test/resources/prophet4j/oldChainSyntax.java");
//        File newFile = new File("src/test/resources/prophet4j/newChainSyntax.java");
//        Diff diff = comparator.compare(oldFile, newFile);
//        List<Operation> operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println("================");
//        }
//    }
//
//    @Test
//    public void testMultiLinesString() throws Exception {
//        AstComparator comparator = new AstComparator();
//        File oldFile = new File("src/test/resources/prophet4j/oldMultiLinesString.java");
//        File newFile = new File("src/test/resources/prophet4j/newMultiLinesString.java");
//        Diff diff = comparator.compare(oldFile, newFile);
//        List<Operation> operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println("================");
//        }
//    }

    /* it may be helpful to create one PR for gumtree-spoon-ast-diff
    Diff diff = comparator.compare(oldFile, newFile);
    CtElement srcRoot = comparator.getCtType(oldFile).getParent();
    CtElement dstRoot = comparator.getCtType(newFile).getParent();
    CtElement ancestor = diff.commonAncestor();
    // we have to handle the CtPath here because evaluateOn() would be invalid when it meets #subPackage
    CtPath ancestorPath = ancestor.getPath();
    String ancestorPathString = ancestorPath.toString();
    ancestorPathString = ancestorPathString.substring(ancestorPathString.indexOf("#containedType"));
    ancestorPath = new CtPathStringBuilder().fromString(ancestorPathString);
    List<CtElement> srcStmtList = new ArrayList<>(ancestorPath.evaluateOn(srcRoot));
    assert srcStmtList.size() == 1;
    CtElement srcNode = srcStmtList.get(0);
    List<CtElement> dstStmtList = new ArrayList<>(ancestorPath.evaluateOn(dstRoot));
    assert dstStmtList.size() == 1;
    CtElement dstNode = dstStmtList.get(0);
     */
}
