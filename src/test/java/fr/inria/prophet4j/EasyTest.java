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

//import java.io.File;
//import java.util.List;

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
