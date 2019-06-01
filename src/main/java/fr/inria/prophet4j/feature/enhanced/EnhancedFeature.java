package fr.inria.prophet4j.feature.enhanced;

import fr.inria.prophet4j.feature.Feature;

public interface EnhancedFeature extends Feature {
    int AF_SIZE = AtomicFeature.values().length; // 44
    int RF_SIZE = RepairFeature.values().length; // 5
    int VF_SIZE = ValueFeature.values().length; // 28

    int FEATURE_BASE_0 = 0;
    int FEATURE_BASE_1 = FEATURE_BASE_0 + RF_SIZE;
    // srcAF * RF
    int FEATURE_BASE_2 = FEATURE_BASE_1 + POS_SIZE * AF_SIZE * RF_SIZE;
    // srcVF * RF
    int FEATURE_BASE_3 = FEATURE_BASE_2 + POS_SIZE * VF_SIZE * RF_SIZE; // newly added
    // dstAF * srcAF
    int FEATURE_BASE_4 = FEATURE_BASE_3 + POS_SIZE * AF_SIZE * AF_SIZE;
    // srcVF * srcAF
    int FEATURE_BASE_5 = FEATURE_BASE_4 + POS_SIZE * VF_SIZE * AF_SIZE; // newly added
    // dstAF * dstRF
    int FEATURE_BASE_6 = FEATURE_BASE_5 + AF_SIZE * RF_SIZE; // newly added
    // dstVF * dstRF
    int FEATURE_BASE_7 = FEATURE_BASE_6 + VF_SIZE * RF_SIZE; // newly added
    // dstAF * dstVF
    int FEATURE_SIZE = FEATURE_BASE_7 + AF_SIZE * VF_SIZE;
//    int FEATURE_BASE_0 = 0;
//    int FEATURE_BASE_1 = FEATURE_BASE_0 + POS_SIZE * AF_SIZE; // for src
//    int FEATURE_BASE_2 = FEATURE_BASE_1 + POS_SIZE * AF_SIZE; // for dst
//    int FEATURE_SIZE = FEATURE_BASE_2 + RF_SIZE * SF_SIZE;

    enum CrossType implements EnhancedFeature {
        RF_CT, // RepairFeatureNum     = RepairFeatureNum
        POS_AF_RF_CT, // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum
        POS_VF_RF_CT, // newly added
        POS_AF_AF_CT, // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum
        POS_VF_AF_CT, // newly added
        AF_RF_CT, // newly added
        VF_RF_CT, // newly added
        AF_VF_CT, // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum
//        POS_AF_CT4SRC, // AtomicCrossFeatureNum = 3 * AtomFeatureNum
//        POS_AF_CT4DST, // AtomicCrossFeatureNum = 3 * AtomFeatureNum
//        RF_SF_CT, // ValueCrossFeatureNum  = RepairFeatureNum * ValueFeatureNum
    }

    // enumerate atomic features as more as possible
    enum AtomicFeature implements EnhancedFeature {
        /**
         * temporary features 7
         */
        CHANGED_AF, // ++a --a a++ a-- += -= *= /= =
        DEREF_AF, // []
        INDEX_AF, // []
        CALLEE_AF,
        CALL_ARGUMENT_AF,
        ABSTRACT_V_AF,
        MEMBER_ACCESS_AF,

        /**
         * STMT 10
         */
        STMT_LABEL_SF, // label
        STMT_LOOP_SF, // for foreach do while
        STMT_ASSIGN_SF, // =
        STMT_CALL_SF, // print()
        STMT_COND_SF, // if ...
        STMT_CONTROL_SF, // break continue return throw
        R_STMT_ASSIGN_SF, // replace version
        R_STMT_CALL_SF, // replace version
        R_STMT_COND_SF, // replace version
        R_STMT_CONTROL_SF, // replace version

        /**
         * Assignment Operators 6
         */
        AOP_ASSIGN_AF, // =
        AOP_PLUS_AF, // +=
        AOP_MINUS_AF, // -=
        AOP_MUL_AF, // *=
        AOP_DIV_AF, // /=
        AOP_MOD_AF, // %=
        // bitOP should be rare
//        AOP_BITAND_AF, // &=
//        AOP_BITOR_AF, // |=
//        AOP_BITXOR_AF, // ^=
//        AOP_SL_AF, // <<=
//        AOP_SR_AF, // >>=
//        AOP_USR_AF, // >>>=

        /**
         * Binary Operators 14
         */
        BOP_PLUS_AF, // a+b
        BOP_MINUS_AF, // a-b
        BOP_MUL_AF, // a*b
        BOP_DIV_AF, // a/b
        BOP_MOD_AF, // a%b
        BOP_LE_AF, // <=
        BOP_LT_AF, // <
        BOP_GE_AF, // >=
        BOP_GT_AF, // >
        BOP_EQ_AF, // ==
        BOP_NE_AF, // !=
        BOP_AND_AF, // &&
        BOP_OR_AF, // ||
        BOP_INSTANCEOF_AF, // instanceof
        // bitOP should be rare
//        BOP_BITAND_AF, // &
//        BOP_BITOR_AF, // |
//        BOP_BITXOR_AF, // ^
//        BOP_SL_AF, // <<
//        BOP_SR_AF, // >>
//        BOP_USR_AF, // >>>

        /**
         * Unary Operators 5
         */
        UOP_NOT_AF, // !a
        UOP_POS_AF, // +a
        UOP_NEG_AF, // -a
        UOP_INC_AF, // ++a a++
        UOP_DEC_AF, // --a a--
        // bitOP should be rare
//        UOP_COMPL_AF, // ~a

        /**
         * Reference 2
         */
        REF_SUPER_AF,
        REF_THIS_AF,

//        /**
//         * Flow 16
//         */
        // BRANCH 5
//        FB_IF_AF,
//        FB_ELSE_AF,
//        FB_SWITCH_AF,
//        FB_CASE_AF,
//        FB_DEFAULT_AF,
        // CONTROL 3
//        FC_BREAK_AF,
//        FC_CONTINUE_AF,
//        FC_RETURN_AF,
        // Exception 5
//        FE_ASSERT_AF,
//        FE_TRY_AF,
//        FE_CATCH_AF,
//        FE_FINALLY_AF,
//        FE_THROW_AF,
        // Loop 3
//        FL_DO_AF,
//        FL_FOR_AF,
//        FL_WHILE_AF,

        /*
        // whether should we consider <> and : or not ?
        // other Operators 1
//        OOP_CONDITIONAL_AF, // ?: I guess it belongs to CtConditional
        // Separator 6 http://www.cafeaulait.org/course/week2/10.html
//        SEP_PARENTHESES_AF, // ()
//        SEP_BRACKETS_AF, // []
//        SEP_BRACES_AF, // {}
//        SEP_COMMA_AF, // ,
//        SEP_PERIOD_AF, // .
//        SEP_SEMICOLON_AF, // ;
         */
    }

    enum RepairFeature implements EnhancedFeature {
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
        // DELETE_STMT_RF, // case of delete one statement
        // UNKNOWN_STMT_RF, // other unknown operations like moving one statement
    }

    // we should learn some ideas from tech-srl/code2vec someday
    enum ValueFeature implements EnhancedFeature {
        /**
         * temporary features 6
         */
//        CONSTANT_VF,
//        VARIABLE_VF,
        LOCAL_VARIABLE_VF,
        GLOBAL_VARIABLE_VF,
        FUNC_ARGUMENT_VF,
        MEMBER_VF, // it should be similar to
        MODIFIED_VF, // I guess it is not so important
        MODIFIED_SIMILAR_VF, // I guess it is not so important

        /**
         * Literal 8
         */
        // Common Values
        LV_NULL_VF, // null
        LV_BLANK_VF, // ""
        LV_ZERO_VF, // 0 0.0
        LV_EMPTY_VF, // empty collection
        // Common Invocations
        LI_GET_VF, // .get()
        LI_SIZE_VF, // .size()
        LI_LENGTH_VF, // .length()
        LI_EQUALS_VF, // .equals()
//        LV_OTHERS_VF, // Other Values (excluding Common Values)
//        LI_OTHERS_VF, // Other Invocations (excluding Common Invocations)
//        LP_OTHERS_VF, // Other Properties (excluding Common Properties)

        /**
         * Literal Types 14
         */
        // Primitive Types (basic data types) 8
        LT_BYTE_VF,
        LT_CHAR_VF,
        LT_SHORT_VF,
        LT_INT_VF,
        LT_LONG_VF,
        LT_FLOAT_VF,
        LT_DOUBLE_VF,
        LT_BOOLEAN_VF,
        // Common Object Types (Enum String ...) 2
        LT_ENUM_VF,
        LT_STRING_VF,
        // Common Collection Types (List Map Queue Set ...) 4
        LT_LIST_VF,
        LT_MAP_VF,
        LT_QUEUE_VF,
        LT_SET_VF,
    }
}
