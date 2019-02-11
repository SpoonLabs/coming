package prophet4j.defined;

public interface FeatureType {

    int POS_SIZE = Position.values().length;
    int AF_SIZE = AtomicFeature.values().length;
    int RF_SIZE = RepairFeature.values().length;
    int VF_SIZE = ValueFeature.values().length;

    int FEATURE_BASE_0 = 0;
    int FEATURE_BASE_1 = FEATURE_BASE_0 + RF_SIZE;
    int FEATURE_BASE_2 = FEATURE_BASE_1 + POS_SIZE * AF_SIZE * RF_SIZE;
    int FEATURE_BASE_3 = FEATURE_BASE_2 + POS_SIZE * AF_SIZE * AF_SIZE;
    // number of all possible features
    int FEATURE_SIZE = FEATURE_BASE_3 + AF_SIZE * VF_SIZE;

    enum JointType implements FeatureType {
        RF_JT, // RepairFeatureNum     = RepairFeatureNum
        POS_AF_RF_JT, // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum
        POS_AF_AF_JT, // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum
        AF_VF_JT, // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum
    }

    enum Position implements FeatureType {
        POS_C, // current line
        POS_P, // previous lines
        POS_N, // next lines
    }

    enum AtomicFeature implements FeatureType {
        // todo: consider OperatorAssignment cases, namely CommutativeOp += -= *= /= %=
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
        CHANGED_AF, // ++a --a a++ a--
        INDEX_AF, // []
        MEMBER_ACCESS_AF, // [] * & . -> (only .)
        ADDRESS_OF_AF,
        DE_REF_AF,
        ABST_V_AF,
        CALLEE_AF,
        CALL_ARGUMENT_AF,
        VARIABLE_AF, // variable
        CONST_ZERO_AF, // zero constant
        CONST_NONZERO_AF, // not zero constant
        EXCLUDE_ATOM_AF, // not include
        OPERATE_LHS_AF, // a+ a- a* a/ a% a&& a|| ...
        OPERATE_RHS_AF, // +a -a *a /a %a &&a ||a ...
        ASSIGN_LHS_AF, // a=
        ASSIGN_RHS_AF, // =a
        STMT_LOOP_AF, // do for while
        STMT_LABEL_AF,
        STMT_ASSIGN_AF, // =
        STMT_CALL_AF, // print()
        STMT_COND_AF, // if ...
        STMT_CONTROL_AF, // switch case break continue return
        R_STMT_ASSIGN_AF, // replace version of STMT_ASSIGN_AF
        R_STMT_CALL_AF, // replace version of STMT_CALL_AF
        R_STMT_COND_AF, // replace version of STMT_COND_AF
        R_STMT_CONTROL_AF, // replace version of STMT_CONTROL_AF
    }

    // todo: consider whether it is still apply to other repair tools similar to SPR, such as Nopol
    enum RepairFeature implements FeatureType {
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
         * case of delete one statement
         */
        DELETE_STMT_RF,
        /**
         * other unknown operations like move one statement or else
         */
        UNKNOWN_STMT_RF,
    }

    enum ValueFeature implements FeatureType {
        MODIFIED_VF,
        MODIFIED_SIMILAR_VF,
        FUNC_ARGUMENT_VF,
        LOCAL_VARIABLE_VF,
        GLOBAL_VARIABLE_VF,
        MEMBER_VF,
        SIZE_LITERAL_VF,
        ZERO_CONST_VF,
        NONZERO_CONST_VF,
        STRING_LITERAL_VF,
    }
}
