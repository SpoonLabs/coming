package fr.inria.coming.spoon.patterns;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.Diff;
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
    public void testInsParamPattern() throws Exception {
        testExist("/pattern_specification/pattern_INS_Parameter.xml",
                "patterns_examples/case7/INS_Param_s.java",
                "patterns_examples/case7/INS_Param_t.java");
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
    public void testNoInsX2() throws Exception {
        testNoExist("/pattern_specification/pattern_INS_X2.xml",
                "patterns_examples/case8/INS_X2_s.java",
                "patterns_examples/case8/INS_X2_false_t.java");
    }
}
