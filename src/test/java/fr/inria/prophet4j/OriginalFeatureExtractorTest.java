package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

import java.util.List;

import fr.inria.prophet4j.utility.Option;
import org.junit.Test;

import fr.inria.prophet4j.defined.Feature;
import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.original.OriginalFeature.AtomicFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature.RepairFeature;
import fr.inria.prophet4j.defined.original.OriginalFeature.ValueFeature;
import fr.inria.prophet4j.defined.CodeDiffer;

// for OriginalFeatureExtractor
public class OriginalFeatureExtractorTest {
    private void test(Feature caseFeature, Feature checkFeature) {
        String str0, str1;
        CodeDiffer helper = new CodeDiffer(false, new Option());
        if (caseFeature instanceof AtomicFeature) {
            AtomicFeature atomicFeature = (AtomicFeature) caseFeature;
            switch (atomicFeature) {
                case OP_ADD_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=+1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1+1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na+=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_SUB_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=-1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1-1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na-=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_MUL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1*1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na*=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_DIV_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1/1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na/=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_MOD_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1%1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na%=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_LE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_LT_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_GE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_GT_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_EQ_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1==1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case OP_NE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1!=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case UOP_INC_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n++a;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na++;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case UOP_DEC_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n--a;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na--;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case ASSIGN_LHS_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case ASSIGN_ZERO_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case ASSIGN_CONST_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case CHANGED_AF:
                    test(AtomicFeature.OP_ADD_AF, checkFeature);
                    test(AtomicFeature.OP_SUB_AF, checkFeature);
                    test(AtomicFeature.OP_MUL_AF, checkFeature);
                    test(AtomicFeature.OP_DIV_AF, checkFeature);
                    test(AtomicFeature.OP_MOD_AF, checkFeature);
                    test(AtomicFeature.UOP_INC_AF, checkFeature);
                    test(AtomicFeature.UOP_DEC_AF, checkFeature);
                    test(AtomicFeature.ASSIGN_LHS_AF, checkFeature);
                    test(AtomicFeature.ASSIGN_ZERO_AF, checkFeature);
                    test(AtomicFeature.ASSIGN_CONST_AF, checkFeature);
                    break;
                case DEREF_AF: // these is another uncompleted case of DEREF_TF
                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case INDEX_AF:
                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case MEMBER_ACCESS_AF: // fixme: if global variable a exists in str0 then fail, why?
                    str0 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=x.a;\n}\nint a;}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case CALLEE_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case CALL_ARGUMENT_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case ABST_V_AF: // i do not know the meaning of this util
                    test(AtomicFeature.OP_LE_AF, checkFeature);
                    test(AtomicFeature.OP_LT_AF, checkFeature);
                    test(AtomicFeature.OP_GE_AF, checkFeature);
                    test(AtomicFeature.OP_GT_AF, checkFeature);
                    test(AtomicFeature.OP_EQ_AF, checkFeature);
                    test(AtomicFeature.OP_NE_AF, checkFeature);
                    test(AtomicFeature.CHANGED_AF, checkFeature);
                    test(AtomicFeature.DEREF_AF, checkFeature);
                    test(AtomicFeature.INDEX_AF, checkFeature);
                    test(AtomicFeature.MEMBER_ACCESS_AF, checkFeature);
                    test(AtomicFeature.CALLEE_AF, checkFeature);
                    test(AtomicFeature.CALL_ARGUMENT_AF, checkFeature);
                    break;
                case STMT_LABEL_AF:
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){break;}}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){continue;}}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case STMT_LOOP_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\ndo{}while(a)\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nfor(;a;){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case STMT_ASSIGN_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case STMT_CALL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case STMT_COND_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case STMT_CONTROL_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case R_STMT_ASSIGN_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\na=1\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case R_STMT_CALL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case R_STMT_COND_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case R_STMT_CONTROL_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn 0;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nreturn 1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
//                case ADDRESS_OF_AF: // Inapplicable to Java
//                    break;
            }
        }
        if (caseFeature instanceof RepairFeature) {
            RepairFeature repairFeature = (RepairFeature) caseFeature;
            switch (repairFeature) {
                case INSERT_CONTROL_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case INSERT_GUARD_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a){return;}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){System.exit(0)}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case INSERT_STMT_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case REPLACE_COND_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case REPLACE_STMT_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
            }
        }
        if (caseFeature instanceof ValueFeature) {
            ValueFeature valueFeature = (ValueFeature) caseFeature;
            switch (valueFeature) {
                case MODIFIED_VF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case MODIFIED_SIMILAR_VF:
                    str0 = "class Foo{public void bar(){\nboolean a=10000;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=10000*1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case FUNC_ARGUMENT_VF:
                    str0 = "class Foo{public void bar(int x){\nint a=0;\n}}";
                    str1 = "class Foo{public void bar(int x){\nint a=x;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case MEMBER_VF:
                    test(AtomicFeature.MEMBER_ACCESS_AF, checkFeature);
                    break;
                case LOCAL_VARIABLE_VF:
                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case GLOBAL_VARIABLE_VF:
                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case ZERO_CONST_VF:
                    str0 = "class Foo{public void bar(){\nint a;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case NONZERO_CONST_VF:
                    str0 = "class Foo{public void bar(){\nint a;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case STRING_LITERAL_VF:
                    str0 = "class Foo{public void bar(){\nString a;\n}}";
                    str1 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
                case SIZE_LITERAL_VF:
                    str0 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
                    str1 = "class Foo{public void bar(){\nString a=\"a\";\nint b=a.length()\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
                    break;
//                case POINTER_VF: // Inapplicable to Java
//                    break;
//                case STRUCT_POINTER_VF: // Inapplicable to Java
//                    break;
            }
        }
    }

    private boolean check(List<FeatureVector> featureVectors, Feature feature) {
        for (FeatureVector featureVector : featureVectors) {
            if (featureVector.containFeature(feature)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testFeatureExtractor() {
        for (AtomicFeature atomicFeature : AtomicFeature.values()) {
            test(atomicFeature, atomicFeature);
        }
        for (RepairFeature repairFeature : RepairFeature.values()) {
            test(repairFeature, repairFeature);
        }
        for (ValueFeature valueFeature : ValueFeature.values()) {
            test(valueFeature, valueFeature);
        }
    }
}
