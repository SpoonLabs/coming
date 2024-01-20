package fr.inria.prophet4j;

import static org.junit.Assert.assertEquals;

import java.util.List;

import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import org.junit.Test;

import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import fr.inria.prophet4j.feature.extended.ExtendedFeature.AtomicFeature;
import fr.inria.prophet4j.feature.extended.ExtendedFeature.RepairFeature;
import fr.inria.prophet4j.feature.extended.ExtendedFeature.ValueFeature;
import fr.inria.prophet4j.utility.CodeDiffer;

// for ExtendedFeatures
public class ExtendedFeatureExtractorTest {
	
    private void test(Feature checkFeature) {
        String str0, str1;
        if (checkFeature instanceof AtomicFeature) {
            AtomicFeature atomicFeature = (AtomicFeature) checkFeature;
            switch (atomicFeature) {
                case BOP_PLUS_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1+1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case AOP_PLUS_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na+=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case UOP_NEG_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=-1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case BOP_MINUS_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1-1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));     
                    break;
                    
               case AOP_MINUS_AF: 
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na-=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case BOP_MUL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1*1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case  AOP_MUL_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na*=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;    
                    
                case  BOP_DIV_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1/1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case  AOP_DIV_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na/=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
       
                case BOP_MOD_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=1%1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case AOP_MOD_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na%=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case BOP_LE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
          
                case BOP_LT_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1<1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;

                case BOP_GE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;      
                    
                case BOP_GT_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1>1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case BOP_EQ_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1==1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                    
                case BOP_NE_AF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=1!=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
          
                case UOP_INC_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n++a;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na++;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
          
                case UOP_DEC_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n--a;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    str1 = "class Foo{public void bar(){\nint a=1;\na--;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case AOP_ASSIGN_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;                
         
                case DEREF_AF: // these is another uncompleted case of DEREF_TF
                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
            
                case INDEX_AF:
                    str0 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\n}}";
                    str1 = "class Foo{public void bar(){\nint[] a={1};\nint b=0;\nb=a[0];\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case MEMBER_ACCESS_AF:
                    str0 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nFoo x=new Foo();\nint a=x.a;\n}\nint a;}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;        
                case CALLEE_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case CALL_ARGUMENT_AF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;          

                case STMT_LABEL_SF:
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){break;}}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){a=false;}\n}}";
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){continue;}}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
          
                case STMT_LOOP_SF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\ndo{}while(a)\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nfor(;a;){}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nwhile(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
         
                case STMT_ASSIGN_SF:
                    str0 = "class Foo{public void bar(){\nint a=1;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case STMT_CALL_SF:
                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case STMT_COND_SF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case STMT_CONTROL_SF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;           
                case R_STMT_ASSIGN_SF:
                    str0 = "class Foo{public void bar(){\nint a=1;\na=1\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\na=0;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case R_STMT_CALL_SF:
                    str0 = "class Foo{public void bar(){\nint a=1;\nMath.abs(a);\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\nMath.exp(a);\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case R_STMT_COND_SF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case R_STMT_CONTROL_SF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nreturn 0;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nreturn 1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
            }
        }
        if (checkFeature instanceof RepairFeature) {
            RepairFeature repairFeature = (RepairFeature) checkFeature;
            switch (repairFeature) {
                case INSERT_CONTROL_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case INSERT_GUARD_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a){return;}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a){System.exit(0)}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case INSERT_STMT_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\na=false;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case REPLACE_COND_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\nif(a&&true){}\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=true;\nif(a||true){}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case REPLACE_STMT_RF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
            }
        }
        if (checkFeature instanceof ValueFeature) {
            ValueFeature valueFeature = (ValueFeature) checkFeature;
            switch (valueFeature) {
                case MODIFIED_VF:
                    str0 = "class Foo{public void bar(){\nboolean a=true;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=false;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case MODIFIED_SIMILAR_VF:
                    str0 = "class Foo{public void bar(){\nboolean a=10000;\n}}";
                    str1 = "class Foo{public void bar(){\nboolean a=10000*1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case FUNC_ARGUMENT_VF:
                    str0 = "class Foo{public void bar(int x){\nint a=0;\n}}";
                    str1 = "class Foo{public void bar(int x){\nint a=x;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
            
                case LOCAL_VARIABLE_VF:
                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
         
                case GLOBAL_VARIABLE_VF:
                    str0 = "class foo{public void bar(){\nint a=1;\nif(true){int b=1;}\n}}";
                    str1 = "class foo{public void bar(){\nint a=1;\nif(true){a=0;}\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case LV_ZERO_VF:
                    str0 = "class Foo{public void bar(){\nint a;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=0;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case LT_INT_VF:
                    str0 = "class Foo{public void bar(){\nint a;\n}}";
                    str1 = "class Foo{public void bar(){\nint a=1;\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
           
                case LT_STRING_VF:
                    str0 = "class Foo{public void bar(){\nString a;\n}}";
                    str1 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
                case LI_LENGTH_VF:
                    str0 = "class Foo{public void bar(){\nString a=\"a\";\n}}";
                    str1 = "class Foo{public void bar(){\nString a=\"a\";\nint b=a.length()\n}}";
                    assertEquals(Boolean.TRUE, check(str0, str1, checkFeature));
                    break;
            }
        }
    }

    private boolean check(String str0, String str1, Feature feature) {
        Option option = new Option();
        option.featureOption = FeatureOption.EXTENDED;
        CodeDiffer codeDiffer = new CodeDiffer(false, option);
        List<FeatureMatrix> featureMatrices = codeDiffer.runByGenerator(str0, str1);
        for (FeatureMatrix featureMatrix : featureMatrices) {
            System.out.printf(featureMatrix.toString());
            if (featureMatrix.containFeature(feature)) {
                return true;
            }
        }
        //System.out.printf("feature %s not found\n", feature);
        return false;
    }

    @Test
    public void testFeatureExtractor() {
        for (AtomicFeature atomicFeature : AtomicFeature.values()) {
            test(atomicFeature);
        }
        for (RepairFeature repairFeature : RepairFeature.values()) {
            test(repairFeature);
        }
        for (ValueFeature valueFeature : ValueFeature.values()) {
            test(valueFeature);
        }
    }
}
