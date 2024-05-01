package fr.inria.coming.spoon.patterns;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.Diff;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class UnchangedActionTest {
    private static ExperimentMiningInstancesD4JTest exp =
            new ExperimentMiningInstancesD4JTest();

    private void testExist(String pathToPatternFile, String pathToSourceFile, String pathToTargetFile) throws Exception{
        File fl = new File(getClass().getResource(pathToPatternFile).getFile());

        ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

        File s = exp.getFile(pathToSourceFile);
        File t = exp.getFile(pathToTargetFile);
        FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
        Diff diff = r.getDiff(s, t);
        System.out.println("Output: " + diff);
        exp.assertPattern(diff, pattern);
    }

    private void testNoExist(String pathToPatternFile, String pathToSourceFile, String pathToTargetFile) throws Exception{
        File fl = new File(getClass().getResource(pathToPatternFile).getFile());

        ChangePatternSpecification pattern = PatternXMLParser.parseFile(fl.getAbsolutePath());

        File s = exp.getFile(pathToSourceFile);
        File t = exp.getFile(pathToTargetFile);
        FineGrainDifftAnalyzer r = new FineGrainDifftAnalyzer();
        Diff diff = r.getDiff(s, t);
        System.out.println("Output: " + diff);
        exp.assertNoPattern(diff, pattern);
    }

    @Test
    public void testIfccPattern() throws Exception {
        testExist("/pattern_specification/pattern_IF_CC.xml",
                "patterns_examples/case6/Ifcc_s.java",
                "patterns_examples/case6/Ifcc_t.java");
    }

    @Test
    public void testNoIfccPattern() throws Exception {
        testNoExist("/pattern_specification/pattern_IF_CC.xml",
                "patterns_examples/case6/Ifcc_s.java",
                "patterns_examples/case6/Ifcc_false_t.java");
    }

    @Test
    public void testNoIfccPattern1() throws Exception {
        testNoExist("/pattern_specification/pattern_IF_CC.xml",
                "patterns_examples/case6/Ifcc_s.java",
                "patterns_examples/case6/Ifcc_false_t1.java");
    }

    @Test
    public void testInsParamPattern() throws Exception {
        testExist("/pattern_specification/pattern_INS_Parameter.xml",
                "patterns_examples/case7/INS_Param_s.java",
                "patterns_examples/case7/INS_Param_t.java");
    }

    @Test
    public void testInsParamPattern1() throws Exception {
        testExist("/pattern_specification/pattern_INS_Parameter.xml",
                "patterns_examples/case7/INS_Param1_s.java",
                "patterns_examples/case7/INS_Param1_t.java");
    }

    @Test
    public void testNoInsParamPattern() throws Exception {
        testNoExist("/pattern_specification/pattern_INS_Parameter.xml",
                "patterns_examples/case7/INS_Param_s.java",
                "patterns_examples/case7/INS_Param_false_t.java");
    }

    @Test
    public void testNoInsParamPattern1() throws Exception {
        testNoExist("/pattern_specification/pattern_INS_Parameter.xml",
                "patterns_examples/case7/INS_Param_s.java",
                "patterns_examples/case7/INS_Param_false_t1.java");
    }

    @Test
    public void testNoInsParamPattern2() throws Exception {
        testNoExist("/pattern_specification/pattern_INS_Parameter.xml",
                "patterns_examples/case7/INS_Param_s.java",
                "patterns_examples/case7/INS_Param_false_t2.java");
    }

    @Test
    public void testDelParamPattern() throws Exception {
        testExist("/pattern_specification/pattern_DEL_Parameter.xml",
                "patterns_examples/case7/DEL_Param_s.java",
                "patterns_examples/case7/DEL_Param_t.java");
    }

    @Test
    public void testDelParamPattern1() throws Exception {
        testExist("/pattern_specification/pattern_DEL_Parameter.xml",
                "patterns_examples/case7/DEL_Param_s.java",
                "patterns_examples/case7/DEL_Param_t1.java");
    }

    @Test
    public void testNoDelParamPattern1() throws Exception {
        // meld ./src/main/resources/patterns_examples/case7/DEL_Param_s.java ./src/main/resources/patterns_examples/case7/DEL_Param_false_t.java

        // Update BinaryOperator at Main:6
        //	 to a == 5
        //Delete BinaryOperator at Main:6
        //	 ==
        //Move BinaryOperator from Main:6 to Main:6
        //	(a + b)

        // we delete a BinaryOperator whoe parent is a method invocation

        testExist("/pattern_specification/pattern_DEL_Parameter.xml",
                "patterns_examples/case7/DEL_Param_s.java",
                "patterns_examples/case7/DEL_Param_false_t.java");
    }

    @Test
    public void testNoInsX2() throws Exception {
        testNoExist("/pattern_specification/pattern_INS_X2.xml",
                "patterns_examples/case8/INS_X2_s.java",
                "patterns_examples/case8/INS_X2_false_t.java");
    }

    @Ignore
    @Test
    public void testIfApc() throws Exception {
        testExist("/pattern_specification/pattern_IF_APC.xml",
                "patterns_examples/case9/Ifapc_s.java",
                "patterns_examples/case9/Ifapc_t.java");
    }

    @Ignore
    @Test
    public void testNoIfApc() throws Exception {
        testNoExist("/pattern_specification/pattern_IF_APC.xml",
                "patterns_examples/case9/Ifapc_s.java",
                "patterns_examples/case9/Ifapc_false_t.java");
    }
}
