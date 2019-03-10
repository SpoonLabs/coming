package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import fr.inria.prophet4j.defined.Feature;
import fr.inria.prophet4j.defined.Structure.FeatureManager;
import fr.inria.prophet4j.defined.Structure.FeatureOption;
import fr.inria.prophet4j.defined.extended.ExtendedFeature.AtomicFeature;
import fr.inria.prophet4j.defined.extended.ExtendedFeature.RepairFeature;
import fr.inria.prophet4j.defined.extended.ExtendedFeature.ValueFeature;
import fr.inria.prophet4j.utility.CodeDiffer;

// for ExtendedFeatureExtractor
public class ExtendedFeatureExtractorTest {
    private void test(Feature caseFeatureType, Feature checkFeatureType) {
        String str0, str1;
        CodeDiffer helper = new CodeDiffer(false, FeatureOption.EXTENDED);
        if (caseFeatureType instanceof AtomicFeature) {
            AtomicFeature atomicFeature = (AtomicFeature) caseFeatureType;
            switch (atomicFeature) {
                case OP_ADD_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=+1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1+1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na+=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_SUB_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=-1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1-1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na-=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_MUL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1*1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na*=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_DIV_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1/1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na/=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_MOD_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1%1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na%=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_LE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_LT_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_GE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_GT_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_EQ_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1==1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case OP_NE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1!=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case UOP_INC_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n++a;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na++;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case UOP_DEC_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n--a;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nint a=1;\na--;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case ASSIGN_LHS_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case ASSIGN_ZERO_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case ASSIGN_CONST_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case CHANGED_AF:
                    test(AtomicFeature.OP_ADD_AF, checkFeatureType);
                    test(AtomicFeature.OP_SUB_AF, checkFeatureType);
                    test(AtomicFeature.OP_MUL_AF, checkFeatureType);
                    test(AtomicFeature.OP_DIV_AF, checkFeatureType);
                    test(AtomicFeature.OP_MOD_AF, checkFeatureType);
                    test(AtomicFeature.UOP_INC_AF, checkFeatureType);
                    test(AtomicFeature.UOP_DEC_AF, checkFeatureType);
                    test(AtomicFeature.ASSIGN_LHS_AF, checkFeatureType);
                    test(AtomicFeature.ASSIGN_ZERO_AF, checkFeatureType);
                    test(AtomicFeature.ASSIGN_CONST_AF, checkFeatureType);
                    break;
                case DEREF_AF: // these is another uncompleted case of DEREF_AF
                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case INDEX_AF:
                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case MEMBER_ACCESS_AF: // fixme: if global variable a exists in str0 then fail, why?
                    str0 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=x.a;\n}\nint a;}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case CALLEE_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case CALL_ARGUMENT_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case ABST_V_AF: // i do not know the meaning of this util
                    test(AtomicFeature.OP_LE_AF, checkFeatureType);
                    test(AtomicFeature.OP_LT_AF, checkFeatureType);
                    test(AtomicFeature.OP_GE_AF, checkFeatureType);
                    test(AtomicFeature.OP_GT_AF, checkFeatureType);
                    test(AtomicFeature.OP_EQ_AF, checkFeatureType);
                    test(AtomicFeature.OP_NE_AF, checkFeatureType);
                    test(AtomicFeature.CHANGED_AF, checkFeatureType);
                    test(AtomicFeature.DEREF_AF, checkFeatureType);
                    test(AtomicFeature.INDEX_AF, checkFeatureType);
                    test(AtomicFeature.MEMBER_ACCESS_AF, checkFeatureType);
                    test(AtomicFeature.CALLEE_AF, checkFeatureType);
                    test(AtomicFeature.CALL_ARGUMENT_AF, checkFeatureType);
                    break;
                case STMT_LABEL_AF:
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){break;}}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){continue;}}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case STMT_LOOP_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\ndo{}while(a)\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nfor(;a;){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case STMT_ASSIGN_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case STMT_CALL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case STMT_COND_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case STMT_CONTROL_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case R_STMT_ASSIGN_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\na=1\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case R_STMT_CALL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case R_STMT_COND_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case R_STMT_CONTROL_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn 0;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nreturn 1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case ADDRESS_OF_AF: // Inapplicable to Java
                    break;
            }
        }
        if (caseFeatureType instanceof RepairFeature) {
            RepairFeature repairFeature = (RepairFeature) caseFeatureType;
            switch (repairFeature) {
                case INSERT_CONTROL_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case INSERT_GUARD_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a){return;}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){System.exit(0)}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case INSERT_STMT_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case REPLACE_COND_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case REPLACE_STMT_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
            }
        }
        if (caseFeatureType instanceof ValueFeature) {
            ValueFeature valueFeature = (ValueFeature) caseFeatureType;
            switch (valueFeature) {
                case MODIFIED_VF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case MODIFIED_SIMILAR_VF:
                    str0 = "class Foo{public void bar(){\nboolean a=10000;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=10000*1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case FUNC_ARGUMENT_VF:
                    str0 = "class Foo{public void bar(int x){\nint a=0;\n}}";
                    str1 = "class Foo{public void bar(int x){\nint a=x;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case MEMBER_VF:
                    test(AtomicFeature.MEMBER_ACCESS_AF, checkFeatureType);
                    break;
                case LOCAL_VARIABLE_VF:
                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case GLOBAL_VARIABLE_VF:
                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case ZERO_CONST_VF:
                    str0 = "class Foo{public void bar(){\nint a;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=0;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case NONZERO_CONST_VF:
                    str0 = "class Foo{public void bar(){\nint a;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case STRING_LITERAL_VF:
                    str0 = "class Foo{public void bar(){\nString a;\n}}";
                    str1 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case SIZE_LITERAL_VF:
                    str0 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
                    str1 = "class Foo{public void bar(){\nString a=\"a\";\nint b=a.length()\n}}";
                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeatureType));
                    break;
                case POINTER_VF: // Inapplicable to Java
                    break;
                case STRUCT_POINTER_VF: // Inapplicable to Java
                    break;
            }
        }
    }

    private boolean check(List<FeatureManager> featureManagers, Feature featureType) {
        for (FeatureManager featureManager : featureManagers) {
            if (featureManager.containFeature(featureType)) {
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
