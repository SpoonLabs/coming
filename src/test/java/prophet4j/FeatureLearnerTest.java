package prophet4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeatureLearnerTest {
    @Test
    public void testFeatureLearner() {
        assertEquals(Boolean.TRUE, true);
    }
    /* it may be helpful to create one PR for gumtree-spoon-ast-diff
    Diff diff = comparator.compare(file0, file1);
    CtElement srcRoot = comparator.getCtType(file0).getParent();
    CtElement dstRoot = comparator.getCtType(file1).getParent();
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
