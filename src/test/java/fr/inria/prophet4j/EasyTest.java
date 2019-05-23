package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtIf;
import spoon.reflect.factory.CoreFactory;

import java.util.StringJoiner;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasyTest {
    @Test
    public void testFeatureLearner() {
        assertEquals(Boolean.TRUE, true);
    }

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

//    @Test
//    public void testExplicitConversion() throws Exception {
//        AstComparator comparator = new AstComparator();
//        String a = "class Foo{public void bar(){\ndouble b = 1;\n}}";
//        String b = "class Foo{public void bar(){\ndouble b = (double) 1;\n}}";
//        Diff diff = comparator.compare(a, b);
//        List<Operation> operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println("================");
//        }
//        a = "class Foo{public void bar(){\nint a = 0;\n}}";
//        b = "class Foo{public void bar(){\nint a = 1;\n}}";
//        diff = comparator.compare(a, b);
//        operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println(operation.getSrcNode());
//            System.out.println(operation.getDstNode());
//            System.out.println("================");
//
//            Pattern pattern = Pattern.compile(":(\\d+)");
//            Matcher matcher = pattern.matcher(operation.toString());
//
//            if(matcher.find()) {
//                System.out.println(Integer.valueOf(matcher.group(1)));
//                System.out.println(Integer.valueOf(matcher.group(1)));
//            }
//        }
//        a = "class Foo{public void bar(){\nint a = 1;\ndouble b = a;\n}}";
//        b = "class Foo{public void bar(){\nint a = 1;\ndouble b = (double) a;\n}}";
//        diff = comparator.compare(a, b);
//        operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println("================");
//        }
//        File oldFile = new File("src/test/resources/prophet4j/buggyBaseSecantSolver.java");
//        File newFile = new File("src/test/resources/prophet4j/patchedBaseSecantSolver.java");
//        diff = comparator.compare(oldFile, newFile);
//        operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println("================");
//        }
//        oldFile = new File("src/test/resources/prophet4j/buggyMannWhitneyUTest.java");
//        newFile = new File("src/test/resources/prophet4j/patchedMannWhitneyUTest.java");
//        diff = comparator.compare(oldFile, newFile);
//        operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//            System.out.println("================");
//        }
//        oldFile = new File("src/test/resources/prophet4j/buggyMockTable.java");
//        newFile = new File("src/test/resources/prophet4j/humanMockTable.java");
//        diff = comparator.compare(oldFile, newFile);
//        operations = diff.getRootOperations();
//        for (Operation operation : operations) {
//            System.out.println(operation);
//
//            Pattern pattern = Pattern.compile(":(\\d+)");
//            Matcher matcher = pattern.matcher(operation.toString());
//
//            Integer lineNum = null;
//            if (operation instanceof MoveOperation) {
//                if(matcher.find()) {
//                    lineNum = Integer.valueOf(matcher.group(1));
//                }
//                System.out.println(lineNum);
//                if(matcher.find()) {
//                    lineNum = Integer.valueOf(matcher.group(1));
//                }
//                System.out.println(lineNum);
//            }
//
//            System.out.println(operation.getSrcNode());
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
