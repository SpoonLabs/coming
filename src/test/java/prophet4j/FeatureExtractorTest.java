package prophet4j;

import org.junit.Test;
import prophet4j.defined.FeatureStruct.*;
import prophet4j.defined.FeatureType;
import prophet4j.defined.FeatureType.*;
import prophet4j.repair.CodeDiffer;

import java.util.*;

import static org.junit.Assert.assertEquals;

// todo: the ideal test case is to supply rc and expression, so right now we only test atomic features
public class FeatureExtractorTest {

    private boolean check(List<FeatureVector> featureVectors, FeatureType featureType) {
        for (FeatureVector featureVector : featureVectors) {
            for (Feature feature : featureVector.getFeatures()) {
//                System.out.println(feature);
                if (feature.containFeatureType(featureType)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void testFeatureResolver() {
        String str0, str1;
        CodeDiffer helper = new CodeDiffer();
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=+1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_ADD_AF));
            str1 = "class foo{public void bar(){\nint a=1+1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_ADD_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=-1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_SUB_AF));
            str1 = "class foo{public void bar(){\nint a=1-1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_SUB_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1*1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_MUL_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1/1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_DIV_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1%1;\n}}";
            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_MOD_AF));
        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nboolean a=1<=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_LE_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nboolean a=1<1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_LT_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nboolean a=1>=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_GE_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nboolean a=1>1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_GT_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nboolean a=1==1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_EQ_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nboolean a=1!=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OP_NE_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na++;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.UOP_INC_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\n++a;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.UOP_INC_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na--;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.UOP_DEC_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\n--a;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.UOP_DEC_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na++;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CHANGED_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\n++a;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CHANGED_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\na--;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CHANGED_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\n--a;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CHANGED_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint[] a={0};\nint b=a[0];\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.INDEX_AF));
//            str1 = "class foo{public void bar(){\nint[] a={0};\na[0]=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.INDEX_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nint a=x.y;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.MEMBER_ACCESS_AF));
//            str1 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nx.y=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.MEMBER_ACCESS_AF));
//        }
//        {
//            // ABST_V_AF
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CALLEE_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CALL_ARGUMENT_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.VARIABLE_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.VARIABLE_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.VARIABLE_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.VARIABLE_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_ZERO_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_ZERO_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_ZERO_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_ZERO_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=0;\na=0;\n}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_NONZERO_AF));
//            str1 = "class foo{public void bar(){\nint a=0;\na=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_NONZERO_AF));
//            str1 = "class foo{public void bar(){\nint a=0;\nint b=0;\na=b;}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_NONZERO_AF));
//            str1 = "class foo{public void bar(){\nint a=0;\nint b=0;\na*=b;}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.CONST_NONZERO_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a=1;\n}}";
//            str1 = "class foo{public void bar(){\nint a=0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.EXCLUDE_ATOM_AF));
//            str1 = "class foo{public void bar(){\nint b=1;\n}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.EXCLUDE_ATOM_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=a+1;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b+1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OPERATE_LHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=1+b;\n}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.OPERATE_LHS_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=1+a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=1+b;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.OPERATE_RHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b+1;\n}}";
//            assertEquals(Boolean.FALSE, check(helper.func4Test(str0, str1), AtomicFeature.OPERATE_RHS_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_LHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_LHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_LHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_LHS_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_RHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_RHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_RHS_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.ASSIGN_RHS_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\ndo{}while(true)\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_LOOP_AF));
//            str1 = "class foo{public void bar(){\nfor(;true;){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_LOOP_AF));
//            str1 = "class foo{public void bar(){\nwhile(true){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_LOOP_AF));
//        }
//        {
//            // STMT_LABEL_AF
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_ASSIGN_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_ASSIGN_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_ASSIGN_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_CALL_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nif(true){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_COND_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\nswitch(a){case 1:}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_CONTROL_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nwhile(a==1){continue;}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_CONTROL_AF));
//            str1 = "class foo{public void bar(){\nint a=1;\nwhile(a==1){break;}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_CONTROL_AF));
//            str1 = "class foo{public int bar(){\nint a=1;\nreturn 0;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.STMT_CONTROL_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;\n}}";
//            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\nb=a;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.R_STMT_ASSIGN_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nSystem.out.print(1);\n}}";
//            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.R_STMT_CALL_AF));
//        }
//        {
//            str0 = "class foo{public void bar(){\nif(true && false){}\n}}";
//            str1 = "class foo{public void bar(){\nif(true || false){}\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.R_STMT_COND_AF));
//        }
//        {
//            str0 = "class foo{public int bar(){\nreturn 0;\n}}";
//            str1 = "class foo{public int bar(){\nreturn 1;\n}}";
//            assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), AtomicFeature.R_STMT_CONTROL_AF));
//        }
    }
}
