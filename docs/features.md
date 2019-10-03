# Features

This document presents the diff features that we have in Coming.


S4R Features
======

This is the [sample file](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/coming/spoon/features/CodeFeatureDetectorTest.java) of detecting S4R features.


P4J features
=========
    
    Pos = 3 (POS_C -current line-, POS_F -former lines-, POS_L -latter lines-)
    
    AtomFeatureNum   = 30   => 33 (30 + 4 - 1)
    RepairFeatureNum = 5    => 5
    ValueFeatureNum  = 12   => 10 (12 - 2)
    
    RepairFeatureNum     = RepairFeatureNum                      = 5    => 5
    GlobalFeatureNum     = Pos * AtomFeatureNum * RepairFeatureNum = 450  => 495
    VarCrossFeatureNum   = Pos * AtomFeatureNum * AtomFeatureNum   = 2700 => 3267
    ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 360  => 330
    SumNum  = 4097

    There are some features mentioned in ProphetPaper but not implemented in ProphetCode, like VARIABLE_AF, EXCLUDE_ATOM_AF, OPERATE_LHS_AF, OPERATE_RHS_AF, ASSIGN_RHS_AF. As well as some features seem useful but not introduced in ProphetPaper and ProphetCode, such as DELETE_STMT_RF, UNKNOWN_STMT_RF. Prophet4J follows the way of ProphetCode, excluding these features.
    
    Some features are inapplicable to Java, namely ADDRESS_OF_AF, POINTER_VF, STRUCT_POINTER_VF, marked with :no_entry_sign:. In addition, there are some features are extended from original features, that is, OP_DIV_AF and OP_MOD_AF are from DivOpAF, OP_LE_AF and OP_LT_AF are from LessOpAF, OP_GE_AF and OP_GT_AF are from GreaterOpAF, as well as OP_EQ_AF and OP_NE_AF are from EqOpAF.

|FeatureCate|FeatureType4C|FeatureType4J|Test|
|:-:|:-:|:-:|:-:|
|`AtomFeature`|`AddOpAF`|`OP_ADD_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L25)|
|`AtomFeature`|`SubOpAF`|`OP_SUB_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L34)|
|`AtomFeature`|`MulOpAF`|`OP_MUL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L43)|
|`AtomFeature`|`DivOpAF`|`OP_DIV_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L50)|
|`AtomFeature`|`DivOpAF`|`OP_MOD_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L57)|
|`AtomFeature`|`LessOpAF`|`OP_LE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L64)|
|`AtomFeature`|`LessOpAF`|`OP_LT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L69)|
|`AtomFeature`|`GreaterOpAF`|`OP_GE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L74)|
|`AtomFeature`|`GreaterOpAF`|`OP_GT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L79)|
|`AtomFeature`|`EqOpAF`|`OP_EQ_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L84)|
|`AtomFeature`|`EqOpAF`|`OP_NE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L89)|
|`AtomFeature`|`IncUOpAF`|`UOP_INC_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L94)|
|`AtomFeature`|`DecUOpAF`|`UOP_DEC_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L101)|
|`AtomFeature`|`AssignLHSAF`|`ASSIGN_LHS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L108)|
|`AtomFeature`|`AssignZeroAF`|`ASSIGN_ZERO_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L113)|
|`AtomFeature`|`AssignConstantAF`|`ASSIGN_CONST_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L118)|
|`AtomFeature`|`ChangedAF`|`CHANGED_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L123)|
|`AtomFeature`|`DerefAF`|`DEREF_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L135)|
|`AtomFeature`|`IndexAF`|`INDEX_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L140)|
|`AtomFeature`|`MemberAccessAF`|`MEMBER_ACCESS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L145)|
|`AtomFeature`|`CalleeAF`|`CALLEE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L150)|
|`AtomFeature`|`CallArgumentAF`|`CALL_ARGUMENT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L155)|
|`AtomFeature`|`AbstVAF`|`ABST_V_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L160)|
|`AtomFeature`|`StmtLabelAF`|`STMT_LABEL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L174)|
|`AtomFeature`|`StmtLoopAF`|`STMT_LOOP_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L182)|
|`AtomFeature`|`StmtAssignAF`|`STMT_ASSIGN_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L193)|
|`AtomFeature`|`StmtCallAF`|`STMT_CALL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L198)|
|`AtomFeature`|`StmtIfAF`|`STMT_COND_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L203)|
|`AtomFeature`|`StmtControlAF`|`STMT_CONTROL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L208)|
|`AtomFeature`|`RStmtAssignAF`|`R_STMT_ASSIGN_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L213)|
|`AtomFeature`|`RStmtCallAF`|`R_STMT_CALL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L218)|
|`AtomFeature`|`RStmtCondAF`|`R_STMT_COND_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L223)|
|`AtomFeature`|`RStmtControlAF`|`R_STMT_CONTROL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L228)|
|`AtomFeature`|`AddrOfAF`|`-`|:no_entry_sign:|
|`RepairFeature`|`InsertControlRF`|`INSERT_CONTROL_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L240)|
|`RepairFeature`|`AddGuardCondRF`|`INSERT_GUARD_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L245)|
|`RepairFeature`|`InsertStmtRF`|`INSERT_STMT_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L250)|
|`RepairFeature`|`ReplaceCondRF`|`REPLACE_COND_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L255)|
|`RepairFeature`|`ReplaceStmtRF`|`REPLACE_STMT_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L260)|
|`ValueFeature`|`ModifiedVF`|`MODIFIED_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L270)|
|`ValueFeature`|`ModifiedSimilarVF`|`MODIFIED_SIMILAR_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L275)|
|`ValueFeature`|`FuncArgumentVF`|`FUNC_ARGUMENT_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L280)|
|`ValueFeature`|`MemberVF`|`MEMBER_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L285)|
|`ValueFeature`|`LocalVarVF`|`LOCAL_VARIABLE_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L288)|
|`ValueFeature`|`GlobalVarVF`|`GLOBAL_VARIABLE_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L293)|
|`ValueFeature`|`ZeroConstVF`|`ZERO_CONST_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L298)|
|`ValueFeature`|`NonZeroConstVF`|`NONZERO_CONST_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L303)|
|`ValueFeature`|`StringLiteralVF`|`STRING_LITERAL_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L308)|
|`ValueFeature`|`LenLiteralVF`|`SIZE_LITERAL_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/OriginalFeatureExtractorTest.java#L313)|
|`ValueFeature`|`PointerVF`|`-`|:no_entry_sign:|
|`ValueFeature`|`StructPointerVF`|`-`|:no_entry_sign:|


P4J-ext features
=========

    AtomFeatureNum   = 33   => 44 (33 + 13 - 2)
    RepairFeatureNum = 5    => 5
    ValueFeatureNum  = 10   => 28 (10 + 19 - 1)
    
    RepairFeatureNum     = RepairFeatureNum                      = 5
    GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum = 660
    VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum   = 5808
    ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum      = 1232
    SumNum  = 7705
    
|FeatureCate|ExtendedFeature|ExtendedFeature|Test|
|:-:|:-:|:-:|:-:|
|`AtomFeature`|`CHANGED_AF`|`CHANGED_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L123)|
|`AtomFeature`|`DEREF_AF`|`DEREF_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L135)|
|`AtomFeature`|`INDEX_AF`|`INDEX_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L140)|
|`AtomFeature`|`MEMBER_ACCESS_AF`|`MEMBER_ACCESS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L145)|
|`AtomFeature`|`CALLEE_AF`|`CALLEE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L150)|
|`AtomFeature`|`CALL_ARGUMENT_AF`|`CALL_ARGUMENT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L155)|
|`AtomFeature`|`ABST_V_AF`|`ABSTRACT_V_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L160)|
|`AtomFeature`|`ASSIGN_LHS_AF`|`AOP_ASSIGN_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L108)|
|`AtomFeature`|`OP_ADD_AF`|`AOP_PLUS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L25)|
|`AtomFeature`|`OP_SUB_AF`|`AOP_MINUS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L34)|
|`AtomFeature`|`OP_MUL_AF`|`AOP_MUL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L43)|
|`AtomFeature`|`OP_DIV_AF`|`AOP_DIV_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L50)|
|`AtomFeature`|`OP_MOD_AF`|`AOP_MOD_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L57)|
|`AtomFeature`|`OP_ADD_AF`|`BOP_PLUS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L25)|
|`AtomFeature`|`OP_SUB_AF`|`BOP_MINUS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L34)|
|`AtomFeature`|`OP_MUL_AF`|`BOP_MUL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L43)|
|`AtomFeature`|`OP_DIV_AF`|`BOP_DIV_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L50)|
|`AtomFeature`|`OP_MOD_AF`|`BOP_MOD_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L57)|
|`AtomFeature`|`OP_LE_AF`|`BOP_LE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L64)|
|`AtomFeature`|`OP_LT_AF`|`BOP_LT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L69)|
|`AtomFeature`|`OP_GE_AF`|`BOP_GE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L74)|
|`AtomFeature`|`OP_GT_AF`|`BOP_GT_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L79)|
|`AtomFeature`|`OP_EQ_AF`|`BOP_EQ_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L84)|
|`AtomFeature`|`OP_NE_AF`|`BOP_NE_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L89)|
|`AtomFeature`|`-`|`BOP_AND_AF`|WIP|
|`AtomFeature`|`-`|`BOP_OR_AF`|WIP|
|`AtomFeature`|`-`|`BOP_INSTANCEOF_AF`|WIP|
|`AtomFeature`|`OP_ADD_AF`|`UOP_POS_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L25)|
|`AtomFeature`|`OP_SUB_AF`|`UOP_NEG_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L34)|
|`AtomFeature`|`-`|`UOP_NOT_AF`|WIP|
|`AtomFeature`|`UOP_INC_AF`|`UOP_INC_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L94)|
|`AtomFeature`|`UOP_DEC_AF`|`UOP_DEC_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L101)|
|`AtomFeature`|`-`|`REF_SUPER_AF`|WIP|
|`AtomFeature`|`-`|`REF_THIS_AF`|WIP|
|`AtomFeature`|`STMT_LABEL_AF`|`STMT_LABEL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L174)|
|`AtomFeature`|`STMT_LOOP_AF`|`STMT_LOOP_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L182)|
|`AtomFeature`|`STMT_ASSIGN_AF`|`STMT_ASSIGN_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L193)|
|`AtomFeature`|`STMT_CALL_AF`|`STMT_CALL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L198)|
|`AtomFeature`|`STMT_COND_AF`|`STMT_COND_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L203)|
|`AtomFeature`|`STMT_CONTROL_AF`|`STMT_CONTROL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L208)|
|`AtomFeature`|`R_STMT_ASSIGN_AF`|`R_STMT_ASSIGN_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L213)|
|`AtomFeature`|`R_STMT_CALL_AF`|`R_STMT_CALL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L218)|
|`AtomFeature`|`R_STMT_COND_AF`|`R_STMT_COND_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L223)|
|`AtomFeature`|`R_STMT_CONTROL_AF`|`R_STMT_CONTROL_AF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L228)|
|`AtomFeature`|`ASSIGN_ZERO_AF`|`-`|:no_entry_sign:|
|`AtomFeature`|`ASSIGN_CONST_AF`|`-`|:no_entry_sign:|
|`RepairFeature`|`INSERT_CONTROL_RF`|`INSERT_CONTROL_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L240)|
|`RepairFeature`|`INSERT_GUARD_RF`|`INSERT_GUARD_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L245)|
|`RepairFeature`|`INSERT_STMT_RF`|`INSERT_STMT_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L250)|
|`RepairFeature`|`REPLACE_COND_RF`|`REPLACE_COND_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L255)|
|`RepairFeature`|`REPLACE_STMT_RF`|`REPLACE_STMT_RF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L260)|
|`ValueFeature`|`MODIFIED_VF`|`MODIFIED_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L270)|
|`ValueFeature`|`MODIFIED_SIMILAR_VF`|`MODIFIED_SIMILAR_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L275)|
|`ValueFeature`|`FUNC_ARGUMENT_VF`|`FUNC_ARGUMENT_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L280)|
|`ValueFeature`|`MEMBER_VF`|`MEMBER_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L285)|
|`ValueFeature`|`LOCAL_VARIABLE_VF`|`LOCAL_VARIABLE_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L288)|
|`ValueFeature`|`GLOBAL_VARIABLE_VF`|`GLOBAL_VARIABLE_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L293)|
|`ValueFeature`|`-`|`LV_NULL_VF`|WIP|
|`ValueFeature`|`-`|`LV_BLANK_VF`|WIP|
|`ValueFeature`|`ZERO_CONST_VF`|`LV_ZERO_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L298)|
|`ValueFeature`|`-`|`LV_EMPTY_VF`|WIP|
|`ValueFeature`|`-`|`LI_GET_VF`|WIP|
|`ValueFeature`|`SIZE_LITERAL_VF`|`LI_SIZE_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L313)|
|`ValueFeature`|`-`|`LI_LENGTH_VF`|WIP|
|`ValueFeature`|`-`|`LI_EQUALS_VF`|WIP|
|`ValueFeature`|`-`|`LT_BYTE_VF`|WIP|
|`ValueFeature`|`-`|`LT_CHAR_VF`|WIP|
|`ValueFeature`|`-`|`LT_SHORT_VF`|WIP|
|`ValueFeature`|`-`|`LT_INT_VF`|WIP|
|`ValueFeature`|`-`|`LT_LONG_VF`|WIP|
|`ValueFeature`|`-`|`LT_FLOAT_VF`|WIP|
|`ValueFeature`|`-`|`LT_DOUBLE_VF`|WIP|
|`ValueFeature`|`-`|`LT_BOOLEAN_VF`|WIP|
|`ValueFeature`|`-`|`LT_ENUM_VF`|WIP|
|`ValueFeature`|`STRING_LITERAL_VF`|`LT_STRING_VF`|[okay](https://github.com/SpoonLabs/coming/blob/master/src/test/java/fr/inria/prophet4j/ExtendedFeatureExtractorTest.java#L308)|
|`ValueFeature`|`-`|`LT_LIST_VF`|WIP|
|`ValueFeature`|`-`|`LT_MAP_VF`|WIP|
|`ValueFeature`|`-`|`LT_QUEUE_VF`|WIP|
|`ValueFeature`|`-`|`LT_SET_VF`|WIP|
|`ValueFeature`|`NONZERO_CONST_VF`|`-`|:no_entry_sign:|
