package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

// for ExtendedFeatureExtractor
public class ExtendedFeatureExtractorTest {
//    private void test(Feature caseFeature, Feature checkFeature) {
//        String str0, str1;
//        CodeDiffer helper = new CodeDiffer(false, FeatureOption.EXTENDED);
//        if (caseFeature instanceof AtomicFeature) {
//            AtomicFeature atomicFeature = (AtomicFeature) caseFeature;
//            switch (atomicFeature) {
//                case OP_ADD_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=+1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=1+1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na+=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case OP_SUB_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=-1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=1-1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na-=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_MUL_TF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=1*1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na*=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_DIV_TF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=1/1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na/=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_MOD_TF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=1%1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na%=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_LE_TF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_LT_TF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_GE_TF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_GT_TF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_EQ_TF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1==1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case BOP_NE_TF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1!=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case UOP_INC_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\n++a;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na++;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case UOP_DEC_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\n--a;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nint a=1;\na--;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case ASSIGN_LHS_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case ASSIGN_ZERO_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case ASSIGN_CONST_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case CHANGED_AF:
//                    test(AtomicFeature.OP_ADD_AF, checkFeature);
//                    test(AtomicFeature.OP_SUB_AF, checkFeature);
//                    test(AtomicFeature.BOP_MUL_TF, checkFeature);
//                    test(AtomicFeature.BOP_DIV_TF, checkFeature);
//                    test(AtomicFeature.BOP_MOD_TF, checkFeature);
//                    test(AtomicFeature.UOP_INC_AF, checkFeature);
//                    test(AtomicFeature.UOP_DEC_AF, checkFeature);
//                    test(AtomicFeature.ASSIGN_LHS_AF, checkFeature);
//                    test(AtomicFeature.ASSIGN_ZERO_AF, checkFeature);
//                    test(AtomicFeature.ASSIGN_CONST_AF, checkFeature);
//                    break;
//                case DEREF_TF: // these is another uncompleted case of DEREF_TF
//                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
//                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case INDEX_TF:
//                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
//                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case MEMBER_ACCESS_TF: // fixme: if global variable a exists in str0 then fail, why?
//                    str0 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=x.a;\n}\nint a;}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case CALLEE_TF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case CALL_ARGUMENT_SF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case ABST_V_TF: // i do not know the meaning of this util
//                    test(AtomicFeature.BOP_LE_TF, checkFeature);
//                    test(AtomicFeature.BOP_LT_TF, checkFeature);
//                    test(AtomicFeature.BOP_GE_TF, checkFeature);
//                    test(AtomicFeature.BOP_GT_TF, checkFeature);
//                    test(AtomicFeature.BOP_EQ_TF, checkFeature);
//                    test(AtomicFeature.BOP_NE_TF, checkFeature);
//                    test(AtomicFeature.CHANGED_AF, checkFeature);
//                    test(AtomicFeature.DEREF_TF, checkFeature);
//                    test(AtomicFeature.INDEX_TF, checkFeature);
//                    test(AtomicFeature.MEMBER_ACCESS_TF, checkFeature);
//                    test(AtomicFeature.CALLEE_TF, checkFeature);
//                    test(AtomicFeature.CALL_ARGUMENT_SF, checkFeature);
//                    break;
//                case STMT_LABEL_SF:
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){break;}}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){continue;}}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case STMT_LOOP_SF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\ndo{}while(a)\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nfor(;a;){}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case STMT_ASSIGN_SF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case STMT_CALL_SF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case STMT_COND_SF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case STMT_CONTROL_SF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case R_STMT_ASSIGN_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\na=1\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case R_STMT_CALL_AF:
//                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case R_STMT_COND_AF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case R_STMT_CONTROL_AF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn 0;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nreturn 1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case ADDRESS_OF_AF: // Inapplicable to Java
//                    break;
//            }
//        }
//        if (caseFeature instanceof RepairFeature) {
//            RepairFeature repairFeature = (RepairFeature) caseFeature;
//            switch (repairFeature) {
//                case INSERT_CONTROL_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case INSERT_GUARD_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a){return;}\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){System.exit(0)}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case INSERT_STMT_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case REPLACE_COND_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case REPLACE_STMT_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//            }
//        }
//        if (caseFeature instanceof ScopeFeature) {
//            ScopeFeature valueFeature = (ScopeFeature) caseFeature;
//            switch (valueFeature) {
//                case I_MODIFIED_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case I_MODIFIED_SIMILAR_RF:
//                    str0 = "class Foo{public void bar(){\nboolean a=10000;\n}}";
//                    str1 = "class Foo{public void bar(){\nboolean a=10000*1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case FUNC_ARGUMENT_SF:
//                    str0 = "class Foo{public void bar(int x){\nint a=0;\n}}";
//                    str1 = "class Foo{public void bar(int x){\nint a=x;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case MEMBER_TF:
//                    test(AtomicFeature.MEMBER_ACCESS_TF, checkFeature);
//                    break;
//                case LOCAL_VARIABLE_SF:
//                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
//                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case GLOBAL_VARIABLE_SF:
//                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
//                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case V_ZERO_TF:
//                    str0 = "class Foo{public void bar(){\nint a;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=0;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case NONZERO_CONST_TF:
//                    str0 = "class Foo{public void bar(){\nint a;\n}}";
//                    str1 = "class Foo{public void bar(){\nint a=1;\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case C_STRING_TF:
//                    str0 = "class Foo{public void bar(){\nString a;\n}}";
//                    str1 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case V_PROP_TF:
//                    str0 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
//                    str1 = "class Foo{public void bar(){\nString a=\"a\";\nint b=a.length()\n}}";
//                    assertEquals(Boolean.TRUE, check(helper.func4Test(str0, str1), checkFeature));
//                    break;
//                case POINTER_VF: // Inapplicable to Java
//                    break;
//                case STRUCT_POINTER_VF: // Inapplicable to Java
//                    break;
//            }
//        }
//    }
//
//    private boolean check(List<FeatureVector> featureVectors, Feature feature) {
//        for (FeatureVector featureVector : featureVectors) {
//            if (featureVector.containFeature(feature)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Test
//    public void testFeatureExtractor() {
//        for (AtomicFeature atomicFeature : AtomicFeature.values()) {
//            test(atomicFeature, atomicFeature);
//        }
//        for (RepairFeature repairFeature : RepairFeature.values()) {
//            test(repairFeature, repairFeature);
//        }
//        for (ScopeFeature valueFeature : ScopeFeature.values()) {
//            test(valueFeature, valueFeature);
//        }
//    }
}
