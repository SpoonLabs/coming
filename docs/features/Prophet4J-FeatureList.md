    AtomFeatureNum   = 30
    RepairFeatureNum = 5
    ValueFeatureNum  = 12
    
    RepairFeatureNum     = RepairFeatureNum                      = 5
    GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 450
    VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
    ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360
    
    There are some features mentioned in ProphetPaper but not implemented in ProphetCode, like VARIABLE_AF, EXCLUDE_ATOM_AF, OPERATE_LHS_AF, OPERATE_RHS_AF, ASSIGN_RHS_AF. As well as some features seem useful but not introduced in ProphetPaper and ProphetCode, such as DELETE_STMT_RF, UNKNOWN_STMT_RF. Prophet4J follows the way of ProphetCode, excluding these features.
    
    Some features are inapplicable to Java, namely ADDRESS_OF_AF, POINTER_VF, STRUCT_POINTER_VF, marked with :no_entry_sign:. In addition, there are some features are extended from original features, that is, OP_DIV_AF and OP_MOD_AF are from DivOpAF, OP_LE_AF and OP_LT_AF are from LessOpAF, OP_GE_AF and OP_GT_AF are from GreaterOpAF, as well as OP_EQ_AF and OP_NE_AF are from EqOpAF.

    There are two versions of implementation for extracting features, `FeatureResolver.java` follows the way described by ProphetPaper while `FeatureExtractor.java` follows the way implemented by ProphetCode. In order to power the `FeatureLearner.java`, we have to utilize FeatureExtractor.java right now.

|FeatureCate|FeatureType4C|FeatureType4J|Test|
|:-:|:-:|:-:|:-:|
|`AtomFeature`|`AddOpAF`|`OP_ADD_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L20)|
|`AtomFeature`|`SubOpAF`|`OP_SUB_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L29)|
|`AtomFeature`|`MulOpAF`|`OP_MUL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L38)|
|`AtomFeature`|`DivOpAF`|`OP_DIV_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L45)|
|`AtomFeature`|`DivOpAF`|`OP_MOD_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L52)|
|`AtomFeature`|`LessOpAF`|`OP_LE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L59)|
|`AtomFeature`|`LessOpAF`|`OP_LT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L64)|
|`AtomFeature`|`GreaterOpAF`|`OP_GE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L69)|
|`AtomFeature`|`GreaterOpAF`|`OP_GT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L74)|
|`AtomFeature`|`EqOpAF`|`OP_EQ_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L79)|
|`AtomFeature`|`EqOpAF`|`OP_NE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L84)|
|`AtomFeature`|`IncUOpAF`|`UOP_INC_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L89)|
|`AtomFeature`|`DecUOpAF`|`UOP_DEC_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L96)|
|`AtomFeature`|`ChangedAF`|`CHANGED_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L103)|
|`AtomFeature`|`DerefAF`|`DE_REF_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L126)|
|`AtomFeature`|`IndexAF`|`INDEX_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L131)|
|`AtomFeature`|`MemberAccessAF`|`MEMBER_ACCESS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L136)|
|`AtomFeature`|`AssignZeroAF`|`ASSIGN_ZERO_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L141)|
|`AtomFeature`|`AssignConstantAF`|`ASSIGN_CONST_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L148)|
|`AtomFeature`|`AssignLHSAF`|`ASSIGN_LHS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L155)|
|`AtomFeature`|`CalleeAF`|`CALLEE_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L160)|
|`AtomFeature`|`CallArgumentAF`|`CALL_ARGUMENT_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L165)|
|`AtomFeature`|`AbstVAF`|`ABST_V_AF`|`WIP`|
|`AtomFeature`|`StmtLabelAF`|`STMT_LABEL_AF`|`WIP`|
|`AtomFeature`|`StmtLoopAF`|`STMT_LOOP_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L176)|
|`AtomFeature`|`StmtAssignAF`|`STMT_ASSIGN_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L185)|
|`AtomFeature`|`StmtCallAF`|`STMT_CALL_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L190)|
|`AtomFeature`|`StmtIfAF`|`STMT_COND_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L195)|
|`AtomFeature`|`StmtControlAF`|`STMT_CONTROL_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L200)|
|`AtomFeature`|`RStmtAssignAF`|`R_STMT_ASSIGN_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L211)|
|`AtomFeature`|`RStmtCallAF`|`R_STMT_CALL_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L216)|
|`AtomFeature`|`RStmtCondAF`|`R_STMT_COND_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L221)|
|`AtomFeature`|`RStmtControlAF`|`R_STMT_CONTROL_AF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L226)|
|`AtomFeature`|`AddrOfAF`|`ADDRESS_OF_AF`|:no_entry_sign:|
|`RepairFeature`|`InsertControlRF`|`INSERT_CONTROL_RF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L270)|
|`RepairFeature`|`AddGuardCondRF`|`INSERT_GUARD_RF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L281)|
|`RepairFeature`|`InsertStmtRF`|`INSERT_STMT_RF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L288)|
|`RepairFeature`|`ReplaceCondRF`|`REPLACE_COND_RF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L293)|
|`RepairFeature`|`ReplaceStmtRF`|`REPLACE_STMT_RF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L299)|
|`ValueFeature`|`ModifiedVF`|`MODIFIED_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L314)|
|`ValueFeature`|`ModifiedSimilarVF`|`MODIFIED_SIMILAR_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L321)|
|`ValueFeature`|`FuncArgumentVF`|`FUNC_ARGUMENT_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L328)|
|`ValueFeature`|`LocalVarVF`|`LOCAL_VARIABLE_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L333)|
|`ValueFeature`|`GlobalVarVF`|`GLOBAL_VARIABLE_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L341)|
|`ValueFeature`|`MemberVF`|`MEMBER_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L349)|
|`ValueFeature`|`LenLiteralVF`|`SIZE_LITERAL_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L356)|
|`ValueFeature`|`ZeroConstVF`|`ZERO_CONST_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L361)|
|`ValueFeature`|`NonZeroConstVF`|`NONZERO_CONST_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L368)|
|`ValueFeature`|`StringLiteralVF`|`STRING_LITERAL_VF`|[debug](https://github.com/SpoonLabs/coming/blob/master/src/test/java/prophet4j/FeatureExtractorTest.java#L375)|
|`ValueFeature`|`PointerVF`|`POINTER_VF`|:no_entry_sign:|
|`ValueFeature`|`StructPointerVF`|`STRUCT_POINTER_VF`|:no_entry_sign:|
