package fr.inria.prophet4j.feature.S4RO;

import fr.inria.prophet4j.feature.Feature;

public interface S4ROFeature extends Feature {
    int CF_SIZE = CodeFeature.values().length; // how many features?
    int AF_SIZE = AtomicFeature.values().length; // 33
    int RF_SIZE = RepairFeature.values().length; // 5
    int VF_SIZE = ValueFeature.values().length; // 10

    int FEATURE_SIZE_S4R = CF_SIZE;
    int FEATURE_BASE_0 = FEATURE_SIZE_S4R;
    int FEATURE_BASE_1 = FEATURE_BASE_0 + RF_SIZE;
    int FEATURE_BASE_2 = FEATURE_BASE_1 + POS_SIZE * AF_SIZE * RF_SIZE;
    int FEATURE_BASE_3 = FEATURE_BASE_2 + POS_SIZE * AF_SIZE * AF_SIZE;
    int FEATURE_SIZE = FEATURE_BASE_3 + AF_SIZE * VF_SIZE;

    enum CrossType implements S4ROFeature {
        CF_CT,
        RF_CT, // RepairFeatureNum     = RepairFeatureNum
        POS_AF_RF_CT, // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum
        POS_AF_AF_CT, // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum
        AF_VF_CT, // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum
    }

    // base on fr.inria.prophet4j.feature.Feature
    enum CodeFeature implements S4ROFeature {
        // All vars in scope
        VARS_IN_SCOPE,
        // Return type of the parent method
        METHOD_RETURN_TYPE,
        // Type of the parents
        PARENTS_TYPE,
        //
        METHOD_PARAMETERS,
        //
        METHOD_MODIFIERS,
        //
        METHOD_COMMENTS,
        //
        CODE,
        //
        BUGGY_STATEMENT,
        //
        CODE_TREE,
        //
        FILE_LOCATION,
        //
        LINE_LOCATION,
        //
        SPOON_PATH,
        //
        PATH_ELEMENTS,
        //
        PARENT_CLASS,
        //
        VAR_NAME,
        //
        VARS,
        //
        VAR_TYPE,
        //
        VAR_VISIB,
        //
        VAR_MODIF,
        // Statement type:
        TYPE,
        // Involved relational/arithmetic operato
        involved_relation_bin_operators,
        //
        BIN_PROPERTIES,
        // whether involves
        involve_GE_relation_operators, involve_AND_relation_operators, involve_OR_relation_operators,
        involve_BITOR_relation_operators, involve_BITXOR_relation_operators, involve_BITAND_relation_operators,
        involve_EQ_relation_operators, involve_LT_relation_operators, involve_NE_relation_operators,
        involve_GT_relation_operators, involve_LE_relation_operators, involve_SL_relation_operators,
        involve_SR_relation_operators, involve_USR_relation_operators, involve_PLUS_relation_operators,
        involve_MINUS_relation_operators, involve_MUL_relation_operators, involve_DIV_relation_operators,
        involve_MOD_relation_operators, involve_INSTANCEOF_relation_operators,
        // involved unary
        involved_relation_unary_operators, UNARY_PROPERTIES,
        //// whether involves
        involve_POS_relation_operators, involve_NEG_relation_operators, involve_NOT_relation_operators,
        involve_COMPL_relation_operators, involve_PREINC_relation_operators, involve_PREDEC_relation_operators,
        involve_POSTINC_relation_operators, involve_POSTDEC_relation_operators,
        // Involves primitive type
        NUMBER_PRIMITIVE_VARS_IN_STMT,
        // Involves object reference,
        NUMBER_OBJECT_REFERENCE_VARS_IN_STMT,

//

        NUMBER_TOTAL_VARS_IN_STMT,

        // is there any other variable in scope that is similar in name We can have
        // based on Levenstein distance
        HAS_VAR_SIM_NAME,

        // Whether uses constants we can have
        USES_CONSTANT,
        // Whether uses enum we can have
        USES_ENUM,
        // If involves object reference, whether the variable has been assigned in other
        // statements after its initial introduction
        NR_VARIABLE_ASSIGNED,

        NR_VARIABLE_NOT_ASSIGNED,

        NR_OBJECT_ASSIGNED_LOCAL, NR_OBJECT_NOT_ASSIGNED_LOCAL,

        // If involves object reference, whether the variable has been used in other
        // statements after its initial introduction.
        NR_OBJECT_USED, NR_OBJECT_NOT_USED,

        // If involves object reference (which is a local variable), whether the
        // variable has been used in other
        // statements after its initial introduction.
        NR_OBJECT_USED_LOCAL_VAR, NR_OBJECT_NOT_USED_LOCAL_VAR,

        NR_PRIMITIVE_USED_LOCAL_VAR, NR_PRIMITIVE_NOT_USED_LOCAL_VAR,

        // Is field (of an object type) initialization statement? If so, whether the
        // object type has other fields which are not initialized since the definition
        // of the object
        NR_FIELD_INCOMPLETE_INIT,
        // whether has other variables in scope that are type compatible
        HAS_VAR_SIM_TYPE,
        //
        PSPACE,
        //
        BUG_INFO,
        //
        PATCH_INFO,

        // The element corresponding to the patch
        PATCH_CODE_ELEMENT, PATCH_CODE_STATEMENT, POSITION, AFFECTED_PARENT, AFFECTED, OPERATION, AST_PARENT, AST,
        // If the faulty statement involves object reference to local variables (i.e.,
        // use object type local variables), do there exist certain referenced local
        // variable(s) that have never been referenced in other statements
        // (exclude statements inside control flow structure) before the faulty
        // statement
        // since its introduction (declaration)(chart-4)
        S1_LOCAL_VAR_NOT_ASSIGNED, //
        S1_LOCAL_VAR_NOT_USED,

        // If the faulty statement involves using object type variables (either local or
        // global), whether exist other statements in the faulty class that use some
        // same type object variables (with same of the object type variables used in
        // the faulty statement), but add guard check (wrap with if or if-else, and not
        // null or non- null related check) (for closure-111, the faulty statement uses
        // variable topType, whose type is JSType, and there are many other statements
        // in the faulty class which uses JSType variables, but have gurand checks, like
        // statement in 61, in 72. also see for example closure 60.)
        S2_SIMILAR_OBJECT_TYPE_WITH_GUARD, //
        // Spoon class of the fault statement.
        S3_TYPE_OF_FAULTY_STATEMENT,

        // If the faulty statement involves object reference to field (i.e., use object
        // type class field), do there exist certain field(s) that have never been
        // referenced in other methods of the faulty class.
        S4_USED_FIELD,

        // If the faulty statement involves using primitive type variables (either local
        // or global),
        // whether exist other statements in the faulty class that use some same
        // primitive type variables (with some of the primitive type variables used in
        // the faulty statement), but add guard check (for global variables
        S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD,

        // For any variable involved in a logical expression,
        // whether exist other boolean expressions in the faulty class
        // that involve using variable whose type is same with v
        // whether the associated method or class for the faulty line throws exception
        S6_METHOD_THROWS_EXCEPTION,
        // For any variable v involved in a logical expression, whether exist other
        // boolean
        // expressions that involve using variable whose type is same with v —note it is
        // OK
        // for the boolean expression to also use some other variable types, we just
        // require variable of type v is involved (as we do not assume the availability
        // of
        // the whole program, we confine the search of boolean expression in the same
        // class)
        LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION,
        // For any variable involved in a logical expression,whether exist methods
        // (method declaration or method call) in scope (that is in the same faulty
        // class
        // since we do not assume full program) that take variable whose type is same
        // with vas one of its parameters and return boolean
        LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
        // LE3: For a logical expression, if the logical expression involves comparison
        // over primitive type variables (that is, some boolean expressions are
        // comparing the primitive values), is there any other visible local primitive
        // type variables that are not included in the logical
        LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED,
        // Besides the variables involved in a logical expression, whether there exist
        // other local boolean variables that are not involved in the faulty statement
        LE4_EXISTS_LOCAL_UNUSED_VARIABLES,
        // Whether the number of boolean expressions in the logical expression is larger
        // than 1
        LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY,
        // For the logical expression, whether there exists a boolean expression that
        // starts with the "not" operator! (an exclamation mark) (
        LE6_HAS_NEGATION,
        // For the logical expression, whether there exists a boolean expression which
        // is simply a boolean variable
        LE7_SIMPLE_VAR_IN_LOGIC,
        // If the logical expression only uses local variables,whether all of the local
        // variables have been used in other statements (exclude statements inside
        // control flow structure) since the introduction
        LE_8_LOGICAL_WITH_USED_LOCAL_VARS,
        // For each involved variable, whether has method definitions or method calls
        // (in the fault class) that take the type of the involved variable as one of
        // its parameters and the return type of the method is type compatible with the
        // type of the involved variable
        V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
        // has any other variables in scope that are similar in identifier name and type
        // compatible.
        V2_HAS_VAR_SIM_NAME_COMP_TYPE,
        // For each involved variable, is it constant? –can assume variables whose
        // identifier names are majorly capital letters are constant variables
        V3_HAS_CONSTANT,

        V4B_USED_MULTIPLE_AS_PARAMETER,

        // V4: For each involved variable, if it is used as parameter inmethod call, for
        // this method call, is it the first time that it isused as parameter
        V4_FIRST_TIME_USED_AS_PARAMETER,

        // For an involved variable, is there any other variable in scope that is
        // assigned to a certain function transformation of the involved variable
        V5_HAS_VAR_IN_TRANSFORMATION,

        //For each involved variable, whether has methods in scope(method definitions or method calls in the faulty class) thatreturn a type which is the same or compatible with the typeof the involved variable.
        V6_IS_METHOD_RETURN_TYPE_VAR,
        // For each variable, is it primitive type?
        V8_VAR_PRIMITIVE,
        // For each method invocation, whether the method has overloaded method
        M1_OVERLOADED_METHOD,
        // For each method invocation, whether there exist methods that return the same
        // type (or type compatible) and are similar in identifier name with the called
        // method (again, we limit the search to the faulty class, search both method
        // definition and method invocations in the faulty class
        M2_SIMILAR_METHOD_WITH_SAME_RETURN,
        // For each method invocation, whether has method definitions or method calls
        // (in the fault class) that take the return type of the method invocation as
        // one of its parameters and the return type of the method is type compatible
        // with
        // the
        // return type of the method invocation.
        M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,

        // For each method invocation, whether the types of some of its parameters are
        // same or compatible with the return type of the method.
        M4_PARAMETER_RETURN_COMPABILITY,
        //	 For each method invocation, whether has variables in scope whose types are the same or compatible with the return types of the method invocation. I am not sure whether it is easy to add this feature
        M5_MI_WITH_COMPATIBLE_VAR_TYPE,
        // For each method invocation, whether the return value of it is primitive
        M6_RETURN_PRIMITIVE,
        // C1: For each constantc, whether exist other constants used inthe faulty class
        // whose types are the same (or type compatible)withcbut values are different
        C1_SAME_TYPE_CONSTANT,
        // For each constant, is it an enum vlaue (But may be it ishard to detect it use
        // partial program analysis).
        C2_USES_ENUMERATION,
        // For each arithmetic expression, whether has method definitions or method
        // calls (in the fault class) that take the return type of the arithmetic
        // expression as one of its parameters and the return type of the method is
        // type compatible with the return type of the arithmetic expression.
        AE1_COMPATIBLE_RETURN_TYPE;
    }

    enum AtomicFeature implements S4ROFeature {
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

    enum RepairFeature implements S4ROFeature {
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

    enum ValueFeature implements S4ROFeature {
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
