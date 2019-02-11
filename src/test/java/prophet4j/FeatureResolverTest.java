package prophet4j;

import org.junit.Test;

import prophet4j.defined.FeatureType.*;
import prophet4j.feature.FeatureResolver;
//import spoon.reflect.declaration.CtType;
//import spoon.reflect.factory.Factory;
//import spoon.reflect.factory.FactoryImpl;
//import spoon.defined.DefaultCoreFactory;
//import spoon.defined.StandardEnvironment;

import static org.junit.Assert.*;

public class FeatureResolverTest {
    @Test
    public void testFeatureResolver() {
        String str0, str1;
        FeatureResolver featureResolver = new FeatureResolver();
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=+1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_ADD_AF));
            str1 = "class foo{public void bar(){\nint a=1+1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_ADD_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=-1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_SUB_AF));
            str1 = "class foo{public void bar(){\nint a=1-1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_SUB_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1*1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_MUL_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1/1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_DIV_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1%1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_MOD_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nboolean a=1<=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_LE_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nboolean a=1<1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_LT_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nboolean a=1>=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_GE_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nboolean a=1>1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_GT_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nboolean a=1==1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_EQ_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nboolean a=1!=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_NE_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na++;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.UOP_INC_AF));
            str1 = "class foo{public void bar(){\nint a=1;\n++a;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.UOP_INC_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na--;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.UOP_DEC_AF));
            str1 = "class foo{public void bar(){\nint a=1;\n--a;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.UOP_DEC_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na++;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CHANGED_AF));
            str1 = "class foo{public void bar(){\nint a=1;\n++a;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CHANGED_AF));
            str1 = "class foo{public void bar(){\nint a=1;\na--;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CHANGED_AF));
            str1 = "class foo{public void bar(){\nint a=1;\n--a;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CHANGED_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint[] a={0};\nint b=a[0];\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.INDEX_AF));
            str1 = "class foo{public void bar(){\nint[] a={0};\na[0]=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.INDEX_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nint a=x.y;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.MEMBER_ACCESS_AF));
            str1 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nx.y=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.MEMBER_ACCESS_AF));
        }
        {
            // ABST_V_AF
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CALLEE_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CALL_ARGUMENT_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.VARIABLE_AF));
            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.VARIABLE_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.VARIABLE_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.VARIABLE_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_ZERO_AF));
            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_ZERO_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_ZERO_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_ZERO_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=0;\na=0;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_NONZERO_AF));
            str1 = "class foo{public void bar(){\nint a=0;\na=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_NONZERO_AF));
            str1 = "class foo{public void bar(){\nint a=0;\nint b=0;\na=b;}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_NONZERO_AF));
            str1 = "class foo{public void bar(){\nint a=0;\nint b=0;\na*=b;}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.CONST_NONZERO_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\n}}";
            str1 = "class foo{public void bar(){\nint a=0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.EXCLUDE_ATOM_AF));
            str1 = "class foo{public void bar(){\nint b=1;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.EXCLUDE_ATOM_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=a+1;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b+1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OPERATE_LHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=1+b;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OPERATE_LHS_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=1+a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=1+b;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OPERATE_RHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b+1;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OPERATE_RHS_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_LHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_LHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_LHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_LHS_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na=0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_RHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_RHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_RHS_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.ASSIGN_RHS_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\ndo{}while(true)\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_LOOP_AF));
            str1 = "class foo{public void bar(){\nfor(;true;){}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_LOOP_AF));
            str1 = "class foo{public void bar(){\nwhile(true){}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_LOOP_AF));
        }
        {
            // STMT_LABEL_AF
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\na=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_ASSIGN_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_ASSIGN_AF));
            // //
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\na*=b;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_ASSIGN_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_CALL_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nif(true){}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_COND_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nswitch(a){case 1:}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_CONTROL_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nwhile(a==1){continue;}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_CONTROL_AF));
            str1 = "class foo{public void bar(){\nint a=1;\nwhile(a==1){break;}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_CONTROL_AF));
            str1 = "class foo{public int bar(){\nint a=1;\nreturn 0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.STMT_CONTROL_AF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\nb=a;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.R_STMT_ASSIGN_AF));
        }
        {
            str0 = "class foo{public void bar(){\nSystem.out.print(1);\n}}";
            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.R_STMT_CALL_AF));
        }
        {
            str0 = "class foo{public void bar(){\nif(true && false){}\n}}";
            str1 = "class foo{public void bar(){\nif(true || false){}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.R_STMT_COND_AF));
        }
        {
            str0 = "class foo{public int bar(){\nreturn 0;\n}}";
            str1 = "class foo{public int bar(){\nreturn 1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.R_STMT_CONTROL_AF));
        }
        {
            str0 = "class foo{public int bar(){\nint a=1;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nswitch(a){case 1:}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_CONTROL_RF));
            str1 = "class foo{public void bar(){\nint a=1;\nwhile(a==1){continue;}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_CONTROL_RF));
            str1 = "class foo{public void bar(){\nint a=1;\nwhile(a==1){break;}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_CONTROL_RF));
            str1 = "class foo{public int bar(){\nint a=1;\nreturn 0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_CONTROL_RF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nif(true){}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_GUARD_RF));
            str1 = "class foo{public void bar(){\ntry{}catch{}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_GUARD_RF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a;\nint b;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.INSERT_STMT_RF));
        }
        {
//            str0 = "class foo{public void bar(){\nif(true && false){}\n}}";
//            str1 = "class foo{public void bar(){\nif(true || false){}\n}}";
//            // todo: consider based on child feature kind
//            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairType.REPLACE_COND_RF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\na=b;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1;\nb=a;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.REPLACE_STMT_RF));
        }
        {
//            str0 = "class foo{public void bar(){\nint a;\nint b;\n}}";
//            str1 = "class foo{public void bar(){\nint a;\n}}";
//            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.DELETE_STMT_RF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nboolean b=true;\n}}";
            str1 = "class foo{public void bar(){\nboolean b=true;\nint a=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(RepairFeature.UNKNOWN_STMT_RF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=0;\na=1;\n}}";
            str1 = "class foo{public void bar(){\nint a=0;\na=1+1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.MODIFIED_VF));
            str1 = "class foo{public void bar(){\nint a=0;\na=2;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.MODIFIED_VF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=0;\na=1;\n}}";
            str1 = "class foo{public void bar(){\nint a=0;\na=1+1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.MODIFIED_SIMILAR_VF));
            str1 = "class foo{public void bar(){\nint a=0;\na=1+1+1;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.MODIFIED_SIMILAR_VF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\n}}";
            str1 = "class foo{public void bar(){\nSystem.out.println(1);\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.FUNC_ARGUMENT_VF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.LOCAL_VARIABLE_VF));
            str0 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.LOCAL_VARIABLE_VF));
        }
        {
            str0 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.GLOBAL_VARIABLE_VF));
            str0 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.GLOBAL_VARIABLE_VF));
        }
        {
            str0 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nint a;\n}}";
            str1 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nint a=x.y;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.MEMBER_VF));
            str1 = "class foo{public void bar(){\nclass X{int y;}\nX x=new X();\nx.y=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.MEMBER_VF));
        }
        {
            str0 = "class foo{public void bar(){\nList<String> a=new ArrayList<>();\nint b=0;\n}}";
            str1 = "class foo{public void bar(){\nList<String> a=new ArrayList<>();\nint b=a.size();\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.SIZE_LITERAL_VF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=0;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.ZERO_CONST_VF));
            str1 = "class foo{public void bar(){\nint a=1;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.ZERO_CONST_VF));
        }
        {
            str0 = "class foo{public void bar(){\nint a;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.NONZERO_CONST_VF));
            str1 = "class foo{public void bar(){\nint a=0;\n}}";
            assertEquals(Boolean.FALSE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.NONZERO_CONST_VF));
        }
        {
            str0 = "class foo{public void bar(){\nString a;\n}}";
            str1 = "class foo{public void bar(){\nString a=\"\";\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(ValueFeature.STRING_LITERAL_VF));
        }
        // todo: add more test cases (for more feature cases) (all features should be complete)
//        {
//            str0 = "public class foo{\npublic void bar(){\nint a=0;\nint b=a;\nint c=0;\na=1;\nb=a;\n}\n}";
//            str1 = "public class foo{\npublic void bar(){\nint a=0;\nint b=1;\nint c=0;\na=b;\n}\n}";
//            System.out.println("================\n" + featureResolver.easyExtractor(str0, str1));
//            assertEquals(Boolean.TRUE, containFeatureType(featureResolver.easyExtractor(str0, str1), (FeatureType.STMT_ASSIGN_AF)));
//        }

        // OperationKind: Insert, Update, Move, Delete;
        // NodeKind: context.getTypeLabel(operation.getAction().getNode())
        // NodeLabel: operation.getAction().getNode().getLabel()
        // just keep it, find usage from LabelFinder & spoon.reflect.code
//        assertEquals(Boolean.TRUE, diff.containsOperation(OperationKind.Update, "BinaryOperator"));
//        assertEquals(Boolean.TRUE, diff.containsOperation(OperationKind.Update, "BinaryOperator", "PLUS"));
    }
}
