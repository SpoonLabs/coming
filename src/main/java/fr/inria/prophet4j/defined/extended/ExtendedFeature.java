package fr.inria.prophet4j.defined.extended;

import fr.inria.prophet4j.defined.Feature;

public interface ExtendedFeature extends Feature {
    int AF_SIZE = AtomicFeature.values().length;
    int RF_SIZE = RepairFeature.values().length;
    int VF_SIZE = ValueFeature.values().length;

    int FEATURE_BASE_0 = 0;
    int FEATURE_BASE_1 = FEATURE_BASE_0 + RF_SIZE;
    int FEATURE_BASE_2 = FEATURE_BASE_1 + POS_SIZE * AF_SIZE * RF_SIZE;
    int FEATURE_BASE_3 = FEATURE_BASE_2 + POS_SIZE * AF_SIZE * AF_SIZE;
    // number of all possible features
    int FEATURE_SIZE = FEATURE_BASE_3 + AF_SIZE * VF_SIZE;

    enum CrossType implements ExtendedFeature {
        RF_CT, // RepairFeatureNum     = RepairFeatureNum
        POS_AF_RF_CT, // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum
        POS_AF_AF_CT, // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum
        AF_VF_CT, // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum
    }

    // https://zh.wikibooks.org/zh-hans/Java/关键字
    enum AtomicFeature implements ExtendedFeature {
//        OP_ADD_AF, // +a a+b +=
//        OP_SUB_AF, // -a a-b -=
//        OP_MUL_AF, // a*b *=
//        OP_DIV_AF, // a/b /=
//        OP_MOD_AF, // a%b %=
//        OP_LE_AF, // <=
//        OP_LT_AF, // <
//        OP_GE_AF, // >=
//        OP_GT_AF, // >
//        OP_EQ_AF, // ==
//        OP_NE_AF, // !=
//        UOP_INC_AF, // ++a a++
//        UOP_DEC_AF, // --a a--

        // Binary Operators 20
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
        BOP_BITAND_AF, // &
        BOP_BITOR_AF, // |
        BOP_BITXOR_AF, // ^
        BOP_SL_AF, // <<
        BOP_SR_AF, // >>
        BOP_USR_AF, // >>>
        BOP_INSTANCEOF_AF, // instanceof

        // Unary Operators 8
        UOP_NOT_AF, // !a
        UOP_COMPL_AF, // ~a
        UOP_POS_AF, // +a
        UOP_NEG_AF, // -a
        UOP_PREINC_AF, // ++a
        UOP_POSTINC_AF, // a++
        UOP_PREDEC_AF, // --a
        UOP_POSTDEC_AF, // a--

        // Assignment Operators 12
        AOP_ASSIGN_AF, // =
        AOP_PLUS_AF, // +=
        AOP_MINUS_AF, // -=
        AOP_MUL_AF, // *=
        AOP_DIV_AF, // /=
        AOP_MOD_AF, // %=
        AOP_BITAND_AF, // &=
        AOP_BITOR_AF, // |=
        AOP_BITXOR_AF, // ^=
        AOP_SL_AF, // <<=
        AOP_SR_AF, // >>=
        AOP_USR_AF, // >>>=

        // VARIABLE_AF, // variable
        ASSIGN_LHS_AF, // a=
        // ASSIGN_RHS_AF, // =a
        ASSIGN_ZERO_AF, // zero
        ASSIGN_CONST_AF, // constant
        // EXCLUDE_ATOM_AF, // not include
        // OPERATE_LHS_AF, // a+ a- a* a/ a% a&& a|| ...
        // OPERATE_RHS_AF, // +a -a *a /a %a &&a ||a ...
        CHANGED_AF, // ++a --a a++ a--
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

    enum RepairFeature implements ExtendedFeature {
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
        // UNKNOWN_STMT_RF, // other unknown operations like move one statement or else
    }

    enum ValueFeature implements ExtendedFeature {
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
    /*
    // we need one enhanced version
    // (AtomicFeature X ScopeFeature) X RepairFeature X (AtomicFeature X ScopeFeature)
    // some ideas from tech-srl/code2vec
    enum AtomicFeature implements ExtendedFeature {
        // Binary Operators 20
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
        BOP_BITAND_AF, // &
        BOP_BITOR_AF, // |
        BOP_BITXOR_AF, // ^
        BOP_SL_AF, // <<
        BOP_SR_AF, // >>
        BOP_USR_AF, // >>>
        BOP_INSTANCEOF_AF, // instanceof

        // Unary Operators 8
        UOP_NOT_AF, // !a
        UOP_COMPL_AF, // ~a
        UOP_POS_AF, // +a
        UOP_NEG_AF, // -a
        UOP_PREINC_AF, // ++a
        UOP_POSTINC_AF, // a++
        UOP_PREDEC_AF, // --a
        UOP_POSTDEC_AF, // a--

        // Assignment Operators 12
        AOP_ASSIGN_AF, // =
        AOP_PLUS_AF, // +=
        AOP_MINUS_AF, // -=
        AOP_MUL_AF, // *=
        AOP_DIV_AF, // /=
        AOP_MOD_AF, // %=
        AOP_BITAND_AF, // &=
        AOP_BITOR_AF, // |=
        AOP_BITXOR_AF, // ^=
        AOP_SL_AF, // <<=
        AOP_SR_AF, // >>=
        AOP_USR_AF, // >>>=
        // other Operators 1
        OOP_CONDITIONAL_AF, // ?: I guess it belongs to CtConditional todo check

        // Separator 6 http://www.cafeaulait.org/course/week2/10.html
        SEP_PARENTHESES_AF, // () for
        SEP_BRACKETS_AF, // [] for array
        SEP_BRACES_AF, // {}
        SEP_COMMA_AF, // ,
        SEP_PERIOD_AF, // .
        SEP_SEMICOLON_AF, // ;
        // whether should we consider <> and : or not ?

        // Keywords 0
        // we handle these as RepairFeatures

        // Identifier 3
        I_FUNC_AF,
        I_VAR_AF,
        I_CONST_AF,

        // Generalized Literal 7
        // Popular Values
        V_BLANK_AF, // ""
        V_ZERO_AF, // 0 0.0
        // General Values (including Popular Values)
        V_VALUE_AF,
        // Popular Properties
        V_LENGTH_AF,
        // General Properties (including Popular Properties)
        V_PROP_AF,
        // Popular Invocations
        V_SIZE_AF,
        // General Invocations (including Popular Invocations)
        V_INVOC_AF,

        // Types and Classes 10 for I_ and V_
        // Primitive Types (basic data types)
        T_BYTE_AF,
        T_CHAR_AF,
        T_SHORT_AF,
        T_INT_AF,
        T_LONG_AF,
        T_FLOAT_AF,
        T_DOUBLE_AF,
        T_BOOLEAN_AF,
        // we ignored other types (null void) todo: consider
        // Common Classes (Enum String ...)
        C_ENUM_AF,
        C_STRING_AF,
        // we ignored other classes (...) todo: consider
    }
    // structure scope path etc
    enum ScopeFeature implements ExtendedFeature {
        // other aspects of Operators
        OP_LHS_SF, // have nothing to do with UnaryOperators
        OP_RHS_SF, // have nothing to do with UnaryOperators

        STMT_LABEL_SF, // label
        STMT_LOOP_SF, // for foreach do while
        STMT_ASSIGN_SF, // =
        STMT_CALL_SF, // print()
        STMT_COND_SF, // if ...
        STMT_CONTROL_SF, // break continue return throw

        FUNC_ARGUMENT_SF,
        LOCAL_VARIABLE_SF,
        GLOBAL_VARIABLE_SF,

        CALL_ARGUMENT_SF,

        I_MODIFIED_RF,
        I_MODIFIED_SIMILAR_RF,
    }
     */
}
