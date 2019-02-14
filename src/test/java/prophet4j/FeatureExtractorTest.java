package prophet4j;

import org.junit.Test;
import prophet4j.defined.FeatureStruct.*;
import prophet4j.defined.FeatureType;
import prophet4j.defined.FeatureType.*;
import prophet4j.support.CodeDiffer;

import java.util.*;

import static org.junit.Assert.assertEquals;

// todo: test features at different position
public class FeatureExtractorTest {
    @Test
    public void testFeatureResolver() {
        String str0, str1;
        CodeDiffer helper = new CodeDiffer();
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=+1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_ADD_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na=1+1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_ADD_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na+=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_ADD_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=-1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_SUB_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na=1-1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_SUB_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na-=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_SUB_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\nn=1*1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_MUL_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\nn*=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_MUL_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=1/1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_DIV_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na/=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_DIV_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=1%1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_MOD_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na%=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_MOD_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
            str1 = "class Foo{public void bar(){\nboolean a=1<=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_LE_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
            str1 = "class Foo{public void bar(){\nboolean a=1<1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_LT_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
            str1 = "class Foo{public void bar(){\nboolean a=1>=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_GE_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
            str1 = "class Foo{public void bar(){\nboolean a=1>1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_GT_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
            str1 = "class Foo{public void bar(){\nboolean a=1==1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_EQ_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
            str1 = "class Foo{public void bar(){\nboolean a=1!=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.OP_NE_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\n++a;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.UOP_INC_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na++;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.UOP_INC_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\n--a;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.UOP_DEC_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na--;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.UOP_DEC_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na+=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na-=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na*=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na/=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na%=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\n++a;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na++;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\n--a;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na--;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CHANGED_AF));
        }
//        {
//            str0 = "class Foo{public void bar(){\nint[] a={0};\n}}";
//            str1 = "class Foo{public void bar(){\nint[] a={0};\na[0]=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.INDEX_AF));
//            str1 = "class Foo{public void bar(){\nint[] a={0};\nint b=a[0];\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.INDEX_AF));
//        }
        {// fixme: if global variable a exists in str0 then fail, why?
            str0 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=x.a;\n}\nint a;}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.MEMBER_ACCESS_AF));
        }
//        {
//            // ABST_V_AF
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a;\n}}";
//            str1 = "class Foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CALLEE_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a;\n}}";
//            str1 = "class Foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CALL_ARGUMENT_AF));
//        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CONST_ZERO_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1, null), AtomicFeature.CONST_ZERO_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1, null), AtomicFeature.CONST_NONZERO_AF));
            str1 = "class Foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.CONST_NONZERO_AF));
        }
        {
            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
            str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.ASSIGN_LHS_AF));
        }
//        {
//            str0 = "class Foo{public void bar(){\nint a;\n}}";
//            str1 = "class Foo{public void bar(){\ndo{}while(true)\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_LOOP_AF));
//            str1 = "class Foo{public void bar(){\nfor(;true;){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_LOOP_AF));
//            str1 = "class Foo{public void bar(){\nwhile(true){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_LOOP_AF));
//        }
//        {
//            // STMT_LABEL_AF
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//            str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_ASSIGN_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a;\n}}";
//            str1 = "class Foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_CALL_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a;\n}}";
//            str1 = "class Foo{public void bar(){\nif(true){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_COND_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a;\n}}";
//            str1 = "class Foo{public void bar(){\nint a=1;\nswitch(a){case 1:}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_CONTROL_AF));
//            str1 = "class Foo{public void bar(){\nint a=1;\nwhile(a==1){continue;}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_CONTROL_AF));
//            str1 = "class Foo{public void bar(){\nint a=1;\nwhile(a==1){break;}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_CONTROL_AF));
//            str1 = "class Foo{public int bar(){\nint a=1;\nreturn 0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.STMT_CONTROL_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nint a=1;\nint b=1;\na=b;\n}}";
//            str1 = "class Foo{public void bar(){\nint a=1;\nint b=1;\nb=a;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.R_STMT_ASSIGN_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nSystem.out.print(1);\n}}";
//            str1 = "class Foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.R_STMT_CALL_AF));
//        }
//        {
//            str0 = "class Foo{public void bar(){\nif(true && false){}\n}}";
//            str1 = "class Foo{public void bar(){\nif(true || false){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.R_STMT_COND_AF));
//        }
//        {
//            str0 = "class Foo{public int bar(){\nreturn 0;\n}}";
//            str1 = "class Foo{public int bar(){\nreturn 1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1, null), AtomicFeature.R_STMT_CONTROL_AF));
//        }
    }

    /*
    CandidateKind.IfExitKind
    CandidateKind.GuardKind
    CandidateKind.SpecialGuardKind
    CandidateKind.AddInitKind
    CandidateKind.AddAndReplaceKind
    CandidateKind.TightenConditionKind
    CandidateKind.LoosenConditionKind
    CandidateKind.ReplaceStringKind
    CandidateKind.ReplaceKind
     */
    private boolean check(List<FeatureVector> featureVectors, FeatureType featureType) {
        for (FeatureVector featureVector : featureVectors) {
            for (Feature feature: featureVector.getFeatures()) {
                if (feature.containFeatureType(featureType)) {
                    return true;
                }
            }
        }
        return false;
    }
}
