package fr.inria.prophet4j.feature.original;

import fr.inria.prophet4j.feature.Feature;

public interface OriginalFeature extends Feature {
    int AF_SIZE = AtomicFeature.values().length; // 33
    int RF_SIZE = RepairFeature.values().length; // 5
    int VF_SIZE = ValueFeature.values().length; // 10

    int FEATURE_BASE_0 = 0;
    int FEATURE_BASE_1 = FEATURE_BASE_0 + RF_SIZE;
    int FEATURE_BASE_2 = FEATURE_BASE_1 + POS_SIZE * AF_SIZE * RF_SIZE;
    int FEATURE_BASE_3 = FEATURE_BASE_2 + POS_SIZE * AF_SIZE * AF_SIZE;
    int FEATURE_SIZE = FEATURE_BASE_3 + AF_SIZE * VF_SIZE;

    enum CrossType implements OriginalFeature {
        RF_CT, // RepairFeatureNum     = RepairFeatureNum
        POS_AF_RF_CT, // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum
        POS_AF_AF_CT, // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum
        AF_VF_CT, // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum
        SRC,
        FORMER,
        LATER
    }

    enum AtomicFeature implements OriginalFeature {
        OP_ADD_AF, // +a a+b +=
        OP_SUB_AF, // -a a-b -=
        OP_MUL_AF, // a*b *=
        OP_DIV_AF, // a/b /=
        OP_MOD_AF, // a%b %=
        OP_LE_AF, // <=
        OP_LT_AF, // <
        OP_GE_AF, // >=
        OP_GT_AF, // >
        OP_EQ_AF, // ==
        OP_NE_AF, // !=
        UOP_INC_AF, // ++a a++
        UOP_DEC_AF, // --a a--
        // VARIABLE_AF, // variable
        ASSIGN_LHS_AF, // a=
        // ASSIGN_RHS_AF, // =a
        ASSIGN_ZERO_AF, // zero
        ASSIGN_CONST_AF, // constant
        // EXCLUDE_ATOM_AF, // not include
        // OPERATE_LHS_AF, // a+ a- a* a/ a% a&& a|| ...
        // OPERATE_RHS_AF, // +a -a *a /a %a &&a ||a ...
        CHANGED_AF, // ++a --a a++ a-- += -= *= /= =
        DEREF_AF, // []
        INDEX_AF, // []
        MEMBER_ACCESS_AF, // [] * & . -> (only .)
        CALLEE_AF,
        CALL_ARGUMENT_AF,
        ABST_V_AF,
        STMT_LABEL_AF, // label
        STMT_LOOP_AF, // for foreach do while
        STMT_ASSIGN_AF, // =
        STMT_CALL_AF, // print()
        STMT_COND_AF, // if ...
        STMT_CONTROL_AF, // break continue return throw
        R_STMT_ASSIGN_AF, // replace version
        R_STMT_CALL_AF, // replace version
        R_STMT_COND_AF, // replace version
        R_STMT_CONTROL_AF, // replace version
//        ADDRESS_OF_AF, // Inapplicable to Java
    }

    enum RepairFeature implements OriginalFeature {
        /**
         * inserting a potentially guarded control statement before a program point (AddControlRepair in Prophet4C)
         * IfExitKind
         */
        INSERT_CONTROL_RF,
        /**
         * adding a guard condition to an existing statement (GuardRepair in Prophet4C)
         * GuardKind
         * SpecialGuardKind
         */
        INSERT_GUARD_RF,
        /**
         * inserting a non-control statement before a program point (AddStmtRepair in Prophet4C)
         * AddInitKind
         * AddAndReplaceKind
         */
        INSERT_STMT_RF,
        /**
         * replacing a branch condition (CondRepair in Prophet4C)
         * TightenConditionKind
         * LoosenConditionKind
         */
        REPLACE_COND_RF,
        /**
         * replacing an existing statement (ReplaceStmtRepair in Prophet4C)
         * ReplaceKind
         * ReplaceStringKind
         */
        REPLACE_STMT_RF,
        /**
         * deleting a guard condition from an existing statement (GuardRepair in Prophet4C)
         * RemoveGuardKind
         */
        REMOVE_PARTIAL_IF,
        /**
         * replacing an existing statement 
         * RemoveSTMTKind
         */
        REMOVE_STMT,
        /**
         * deleting a if condition and the block inside it from an existing statement (GuardRepair in Prophet4C)
         * RemoveGuardBlockKind
         */
        REMOVE_WHOLE_IF,  
        /**
         * remove a whole block
         */
        REMOVE_WHOLE_BLOCK,
        
        
    }

    enum ValueFeature implements OriginalFeature {
        MODIFIED_VF,
        MODIFIED_SIMILAR_VF,
        FUNC_ARGUMENT_VF,
        MEMBER_VF,
        LOCAL_VARIABLE_VF,
        GLOBAL_VARIABLE_VF,
        ZERO_CONST_VF,
        NONZERO_CONST_VF,
        STRING_LITERAL_VF,
        SIZE_LITERAL_VF,
//        POINTER_VF, // Inapplicable to Java
//        STRUCT_POINTER_VF, // Inapplicable to Java
    }
}
