    AtomFeatureNum   = 30
    RepairFeatureNum = 5
    ValueFeatureNum  = 12
    
    RepairFeatureNum     = RepairFeatureNum                      = 5
    GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 450
    VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 2700
    ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360
    
    The column <FeatureKind4J> is to make prophet4j more specific to Java, also help evaluate the plan of enumerating features.
    
    Implementation for some features will turn to follow the way taken by Prophet4C (February 2019).

|FeatureCate|FeatureKind|FeatureKind4J|Test|
|:-:|:-:|:-:|:-:|
|`AtomFeature`|`AddOpAF`|`OP_ADD_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L20)|
|`AtomFeature`|`SubOpAF`|`OP_SUB_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L27)|
|`AtomFeature`|`MulOpAF`|`OP_MUL_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L34)|
|`AtomFeature`|`DivOpAF`|`OP_DIV_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L39)|
|`AtomFeature`|`DivOpAF`|`OP_MOD_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L44)|
|`AtomFeature`|`LessOpAF`|`OP_LE_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L49)|
|`AtomFeature`|`LessOpAF`|`OP_LT_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L54)|
|`AtomFeature`|`GreaterOpAF`|`OP_GE_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L59)|
|`AtomFeature`|`GreaterOpAF`|`OP_GT_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L64)|
|`AtomFeature`|`EqOpAF`|`OP_EQ_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L69)|
|`AtomFeature`|`EqOpAF`|`OP_NE_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L74)|
|`AtomFeature`|`IncUOpAF`|`UOP_INC_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L79)|
|`AtomFeature`|`DecUOpAF`|`UOP_DEC_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L86)|
|`AtomFeature`|`ChangedAF`|`CHANGED_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L93)|
|`AtomFeature`|`IndexAF`|`INDEX_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L104)|
|`AtomFeature`|`MemberAccessAF`|`MEMBER_ACCESS_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L111)|
|`AtomFeature`|`AddrOfAF`|:no_entry_sign:|:no_entry_sign:|
|`AtomFeature`|`DerefAF`|:no_entry_sign:|:no_entry_sign:|
|`AtomFeature`|`AbstVAF`|`ABST_V_AF`|`--`|
|`AtomFeature`|`CalleeAF`|`CALLEE_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L121)|
|`AtomFeature`|`CallArgumentAF`|`CALL_ARGUMENT_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L126)|
|`AtomFeature`|`??`|`VARIABLE_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L131)|
|`AtomFeature`|`AssignZeroAF`|`CONST_ZERO_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L142)|
|`AtomFeature`|`AssignConstantAF`|`CONST_NONZERO_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L153)|
|`AtomFeature`|`??`|`EXCLUDE_ATOM_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L164)|
|`AtomFeature`|`??`|`OPERATE_LHS_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L171)|
|`AtomFeature`|`??`|`OPERATE_RHS_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L178)|
|`AtomFeature`|`AssignLHSAF`|`ASSIGN_LHS_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L185)|
|`AtomFeature`|`??`|`ASSIGN_RHS_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L196)|
|`AtomFeature`|`StmtLoopAF`|`STMT_LOOP_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L207)|
|`AtomFeature`|`StmtLabelAF`|`STMT_LABEL_AF`|`--`|
|`AtomFeature`|`StmtAssignAF`|`STMT_ASSIGN_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L219)|
|`AtomFeature`|`StmtCallAF`|`STMT_CALL_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L229)|
|`AtomFeature`|`StmtIfAF`|`STMT_COND_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L234)|
|`AtomFeature`|`StmtControlAF`|`STMT_CONTROL_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L239)|
|`AtomFeature`|`RStmtAssignAF`|`R_STMT_ASSIGN_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L250)|
|`AtomFeature`|`RStmtCallAF`|`R_STMT_CALL_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L255)|
|`AtomFeature`|`RStmtCondAF`|`R_STMT_COND_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L260)|
|`AtomFeature`|`RStmtControlAF`|`R_STMT_CONTROL_AF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L265)|
|`RepairFeature`|`InsertControlRF`|`INSERT_CONTROL_RF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L270)|
|`RepairFeature`|`AddGuardCondRF`|`INSERT_GUARD_RF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L281)|
|`RepairFeature`|`InsertStmtRF`|`INSERT_STMT_RF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L288)|
|`RepairFeature`|`ReplaceCondRF`|`REPLACE_COND_RF`|[failed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L293)|
|`RepairFeature`|`ReplaceStmtRF`|`REPLACE_STMT_RF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L299)|
|`RepairFeature`|`??`|`DELETE_STMT_RF`|`--`|
|`RepairFeature`|`??`|`UNKNOWN_STMT_RF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L309)|
|`ValueFeature`|`ModifiedVF`|`MODIFIED_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L314)|
|`ValueFeature`|`ModifiedSimilarVF`|`MODIFIED_SIMILAR_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L321)|
|`ValueFeature`|`FuncArgumentVF`|`FUNC_ARGUMENT_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L328)|
|`ValueFeature`|`LocalVarVF`|`LOCAL_VARIABLE_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L333)|
|`ValueFeature`|`GlobalVarVF`|`GLOBAL_VARIABLE_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L341)|
|`ValueFeature`|`MemberVF`|`MEMBER_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L349)|
|`ValueFeature`|`LenLiteralVF`|`SIZE_LITERAL_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L356)|
|`ValueFeature`|`ZeroConstVF`|`ZERO_CONST_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L361)|
|`ValueFeature`|`NonZeroConstVF`|`NONZERO_CONST_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L368)|
|`ValueFeature`|`StringLiteralVF`|`STRING_LITERAL_VF`|[passed](https://github.com/kth-tcs/prophet4J/blob/master/src/test/java/prophet4j/FeatureResolverTest.java#L375)|
|`ValueFeature`|`PointerVF`|:no_entry_sign:|:no_entry_sign:|
|`ValueFeature`|`StructPointerVF`|:no_entry_sign:|:no_entry_sign:|
