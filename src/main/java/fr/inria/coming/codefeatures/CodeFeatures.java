package fr.inria.coming.codefeatures;

/**
 * 
 * @author Matias Martinez
 *
 */

public enum CodeFeatures {

	// If the faulty statement involves object reference to local variables (i.e.,
	// use object type local variables), do there exist certain referenced local
	// variable(s) that have never been referenced in other statements
	// (exclude statements inside control flow structure) before the faulty
	// statement
	// since its introduction (declaration)(chart-4)
	/**
	 * Statement Feature. 
	 * There exist certain referenced local variable(s) that only be declared but not been assigned (initialized).
	 */
	S1_LOCAL_VAR_NOT_ASSIGNED, //
	/**
	 * Statement Feature. 
	 * There exist certain referenced local variable(s) that have never been referenced in other statements.
	 */
	S1_LOCAL_VAR_NOT_USED,
	
	// If the faulty statement involves using object type variables (either local or
		// global), whether exist other statements in the faulty class that use some
		// same type object variables (with same of the object type variables used in
		// the faulty statement), but add guard check (wrap with if or if-else, and not
		// null or non- null related check) (for closure-111, the faulty statement uses
		// variable topType, whose type is JSType, and there are many other statements
		// in the faulty class which uses JSType variables, but have gurand checks, like
		// statement in 61, in 72. also see for example closure 60.)
	/**
	 * Statement Feature. 
	 * There exist statements that use same type object variables but add guard check (wrap with if or if-else, but not null guard).
	 * NORMAL_GUARD indicates not null or non-null related check.
	 * This implies to add normal guard checks before return the object variables.
	 * E.g.: Defects4J Closure 111 and Closure 60.
	 */
	S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, 
	/**
	 * Statement Feature. There exist statements  that use same type object variables but add null check. 
	 * This implies to add null guard checks before return the object variables.
	 */
	S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	
	/**
	 * Statement Feature. The type of the fault statement.Extracted from Spoon class. 
	 * E.g. Invocation,Method,
	 */
	S3_TYPE_OF_FAULTY_STATEMENT,
	
	/**
	 * Statement Feature. If the faulty statement involves object reference to field (i.e., use object
	type class field), do there exist certain field(s) that have never been
	referenced in other methods of the faulty class.
	 */
	S4_Field_NOT_USED,
	
	/**
	 * Statement Feature. If the faulty statement involves object reference to field (i.e., use object
	type class field), do there exist certain field(s) that have never been
	assigned in other methods of the faulty class.
	 */
	
	S4_Field_NOT_ASSIGNED,
	
	/**
	 * Statement Feature.If the faulty statement involves using primitive type variables (either local
	 or global), whether exist other statements in the faulty class that use some same
	 primitive type variables (with some of the primitive type variables used in
	 the faulty statement), but add guard check.
	 */
	
	S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	
	/**
	 *Statement Feature. If the faulty statement involves using primitive type variables (either local
	 or global), whether exist other statements in the faulty class that use some same
	 primitive type variables (with some of the primitive type variables used in
	 the faulty statement), but add null check.
	 */	
	
	S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
	
	/**
	 * Statement Feature. Whether the associated method or class for the faulty line throws exception
	 */
	
	S6_METHOD_THROWS_EXCEPTION,
	
	/**
	 * Statement Feature. There exist faulty assignment statements that object type variable is used.
	 */
	S7_OBJECT_USED_IN_ASSIGNMENT,
	
	/**
	 * Statement Feature. There exist faulty assignment statements that primitive type variable is used.
	 */
	
	S8_PRIMITIVE_USED_IN_ASSIGNMENT,
	
	/**
	 * Statement Feature. 
	 * The method call used in the buggy statement, also existin other statements but with normal guard.
	 * NORMAL_GUARD indicates not null or non-null related check.
	 */
	
	S9_METHOD_CALL_WITH_NORMAL_GUARD,
	
	/**
	 * Statement Feature. 
	 * The method call used in the buggy statement, also exist in other statements but with null guard.
	 */	
	S10_METHOD_CALL_WITH_NULL_GUARD,
	
	/**
	 * Statement Feature. 
	 * faultyClassExceptionType: indicates whether the associated method for the faulty line throws exception..
	 */	
	S11_FAULTY_CLASS_EXCEPTION_TYPE,
	
	/**
	 * Statement Feature. 
	 * The method call used in the buggy statement, also exist in other statements but with a try catch block.
	 */	
	S12_METHOD_CALL_WITH_TRY_CATCH,
	
	/**
	 * Statement Feature.Type of 1 line before the faulty statement.
	 */	
	S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1,
	/**
	 * Statement Feature.Type of 2 lines before the faulty statement.
	 */	
	S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2,
	/**
	 * Statement Feature.Type of 3 lines before the faulty statement.
	 */	
	S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3,
	/**
	 * Statement Feature.Type of 1 line after the faulty statement.
	 */	
	S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1,
	/**
	 * Statement Feature.Type of 2 lines after the faulty statement.
	 */	
	S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2,
	/**
	 * Statement Feature.Type of 3 lines after the faulty statement.
	 */	
	S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3,
	
	/**
	 * Statement Feature.Type of the parent of the faulty statement.
	 */		
	S14_TYPE_OF_FAULTY_STATEMENT_PARENT,
	/**
	 * Statement Feature.TBD
	 */
	S15_HAS_OBJECTIVE_METHOD_CALL,
	/**
	 * Statement Feature.TBD
	 */
	S16_HAS_Invocations_Prone_Exception,
	/**
	 * Statement Feature. Whether the faulty statement in synchronized method.
	 */
	S18_In_Synchronized_Method,
	
	// For any variable v involved in a logical expression, whether exist other
	// boolean
	// expressions that involve using variable whose type is same with v —note it is
	// OK
	// for the boolean expression to also use some other variable types, we just
	// require variable of type v is involved (as we do not assume the availability
	// of
	// the whole program, we confine the search of boolean expression in the same
	// class)
	/**
	 * Logical expression feature. 
	 * Whether exist other boolean expressions that involve using variable whose type is same with variable in the buggy statement.
	 */
	LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION,
	 
	/**
	 *  Logical expression feature
	  * For any variable involved in a logical expression, whether exist methods (method declaration or method call) in scope 
	  * (that is in the same faulty class since we do not assume full program) that take variable whose type is same 
	  * with vas one of its parameters and return boolean
	  */
	
	LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
	
	/**
	 * Logical expression feature
	 * For a logical expression, if the logical expression involves comparison
	 over primitive type variables (that is, some boolean expressions are
	 comparing the primitive values), is there any other visible local primitive
	 type variables that are not included in the logical
	 */
	
	LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED,
	
	/**
	 * Logical expression feature
	 * Besides the variables involved in a logical expression, whether there exist
	 *  other local boolean variables that are not involved in the faulty statement
	 */

	LE4_EXISTS_LOCAL_UNUSED_VARIABLES,
	
	/** 
	 * Logical expression feature
	 *  Whether the number of boolean expressions in the logical expression is largerthan 1
	 */
	LE5_COMPLEX_REFERENCE,
	/**
	 * Logical expression feature.
	 * Whether there exists a boolean expression that starts with the "not" operator! (an exclamation mark) (
	 */

	LE6_HAS_NEGATION,
	
	/**
	 * Logical expression feature.
	 * whether there exists a boolean expression which is simply a boolean variable
	 */

	LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC,

	
	/**
	 * Logical expression feature.
	 * If the logical expression only uses local variables,whether all of the local 
	 * variables have been used in other statements (exclude statements inside control flow structure).
	 */
	LE8_SCOPE_VAR_USED_OTHER_BOOLEXPER,
	
	/**
	 * Logical expression feature.
	 * whether there exists a boolean expression is a normal check
	 */
	 
	LE9_NORMAL_CHECK,
	
	/**
	 * Logical expression feature.
	 * whether there exists a boolean expression is a null check
	 */
	
	LE9_NULL_CHECK,
	
	/**
	 * Logical expression feature.
	 * whether there exists a boolean expression mixs with normal check and null check
	 */
	
	LE9_MIX_CHECK,
	
	/**
	 * Logical expression feature.
	 * whether there exists a boolean expression is a null check with equal == or not_equal != operators.
	 */
	
	LE9_EQUAL_NOTEQUAL_NULL_CHECK,
	
	/**
	 * Logical expression feature.
	 * whether there exists atomic expression with the same invocation target.
	 */
	
	LE10_ATOMIC_EXPRESSION_SAME_INVOCATION_TARGET,
	
	/**
	 * Logical expression feature.
	 * whether there exists atomic expression compare the same left part the target expression.
	 */
	
	LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_LEFT,	

	/**
	 * Logical expression feature.
	 * whether there exists atomic expression compare the same right part the target expression.
	 */
	
	LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_RIGHT,
	

	/**
	 * Logical expression feature.
	 * whether there exists atomic expression with multiple variables.
	 */
	
	LE10_ATOMIC_EXPRESSION_MULTIPLE_VAR_AS_BOOLEAN, 
	
	/**
	 * Logical expression feature.
	 * 
	 */
	
	LE10_ATOMIC_EXPRESSION_USED_IN_INVOCATION_COMPARISION_VARIABLE, 
	
	/**
	 * Logical expression feature.
	 * 
	 */
	LE10_CONTAINS_ALL_INVOCATION_COMPARISION_VARIABLE,
	
	

	
	
	/**
	 * Variable features.
	 *  For each involved variable, whether has method definitions or method calls
	 *  (in the fault class) that take the type of the involved variable as one of
	 *  its parameters and the return type of the method is type compatible with the type of the involved variable.
	 */
	 
	V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,	
	
	/**
	 * Variable features.
	 *  For each involved variable, whether there exist any other variables in scope that are similar in identifier name and type compatible.
	 */
	V2_HAS_VAR_SIM_NAME_COMP_TYPE,
	
	/**
	 * Variable features.
	 * For each involved variable, whether there exist any other variables in scope that are similar in identifier name compatible.
	 */
	V2_HAS_VAR_SIM_NAME,
	/**
	 * Variable features.
	 * For each involved variable, whether it is constant –can assume variables whose
	 * identifier names are majorly capital letters are constant variables
	 * 
	 */
	V3_HAS_CONSTANT,
	
	/**
	 * Variable features.
	 * For each involved variable, if it is used as parameter in method call in multiple times.
	 * 
	 */
	V4B_USED_MULTIPLE_AS_PARAMETER,
	
	/**
	 * Variable features.
	 * For each involved variable, if it is used as parameter in method call, for this method call, is it the first time that it is used as parameter
	 */
	
	V4_FIRST_TIME_USED_AS_PARAMETER,
	
	/**
	 * Variable features.
	 * For an involved variable, is there any other variable in scope that is 
	 * assigned to a certain function transformation of the involved variable
	 *
	 */
	
	V5_HAS_VAR_IN_TRANSFORMATION,
	
	/**
	 * Variable features.
	 * For each involved variable, whether has methods in scope (method definitions or method calls in the faulty class) that 
	 * return a type which is the same or compatible with the type of the involved variable. 
	 */
		
	V6_IS_METHOD_RETURN_TYPE_VAR,	
	
	/**
	 * Variable features.
	 * For each involved variable, whether has parameters of methods in scope (method definitions or method calls in the faulty class) that 
	 * return a type which is the same or compatible with the type of the involved variable. 
	 */
	
	
	V16_IS_METHOD_PARAMETER_TYPE_VAR,

	/**
	 * Variable features.
	 * For each involved variable, whether is it primitive type.
	 */

	V8_VAR_PRIMITIVE,
	
	/**
	 * Variable features.
	 * For each involved variable, whether is it object type.
	 */
	
	V8_VAR_OBJECT,

	/**
	 * Variable features.
	 * For each involved variable, whether there exist variables that have similar type in literal.
	 */
	V9_VAR_TYPE_Similar_Literal,	
	
	/**
	 * Variable features.
	 * For each involved variable, whether there exist similar type variables.
	 */
	
	V10_VAR_TYPE_Similar_VAR,	
	
	/**
	 * Variable features.
	 * For each involved variable, whether there exist compatible type variables in used in condition.
	 */
	
	V11_VAR_COMPATIBLE_TYPE_IN_CONDITION,	
	
	/**
	 * Variable features.
	 * For each involved variable, whether there exist invocation of the variables replaced by other variables.
	 */
	
	V12_VAR_Invocation_VAR_REPLACE_BY_VAR,	
	/**
	 * Variable features.
	 * For each involved variable, whether there exist invocation of the variables replaced by other invocations.
	 */
	V13_VAR_Invocation_VAR_REPLACE_BY_INVOCATION,	
	
	/**
	 * Variable features.
	 * For each involved variable, whether it is an instance of a class.
	 */
	V14_VAR_INSTANCE_OF_CLASS, 	
	
	/**
	 * Variable features.
	 * For each involved variable,TBD
	 */
	
	V15_VAR_LAST_THREE_SAME_TYPE_LOC, 
	
	/**
	 * Variable features.
	 * For each involved variable,whether it is an enumeration type.
	 */
	V17_VAR_IS_ENUMERATION,
	
	/**
	 * Variable features.
	 * For each involved variable, whether it is a method name similar with it in literature.
	 */
	V18_Has_Method_Similar_In_Name,
	
	/**
	 * Variable features.
	 * For each involved variable, whether it is with a special name.TBD
	 */
	V19_With_Special_Name,
	
	/** 
	 * Variable Feature. 
	 * For each involved variable,  whether it is not been used in other statements.
	 */
	V1_LOCAL_VAR_NOT_USED,
	
	/** 
	 * Variable Feature. 
	 * For each involved variable, whether it is not been assigned (initialized).
	 */
	
	V1_LOCAL_VAR_NOT_ASSIGNED,
	
	/** 
	 * Variable Feature. 
	 * For each involved variable, whether there exist a similar variable with the object type with normal guard.
	 */

	V2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
	
	/** 
	 * Variable Feature. 
	 * For each involved variable, whether there exist a similar variable with the primitive type with normal guard.
	 */
	
	V5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	
	/** 
	 * Variable Feature. 
	 * For each involved variable, whether there exist a similar variable with the object type with null guard.
	 */
	
	V2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	
	/** 
	 * Variable Feature. 
	 * For each involved variable, whether there exist a similar variable with the primitive type with null guard.
	 */
	
	V5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
	
	/**
	 * Variable Feature.
	 * For each involved variable, whether it is not referenced in other methods of the faulty class.	
	 */
	
	V4_Field_NOT_USED,
	
	/**
	 * Variable Feature.
	 * For each involved variable, whether it is not assigned in other methods of the faulty class.	
	 */
	V4_Field_NOT_ASSIGNED,
	/**
	 * Variable Feature.
	 * For each involved variable, whether it is object type used in an assignment.	
	 */
	V7_OBJECT_USED_IN_ASSIGNMENT,
	
	/**
	 * Variable Feature.
	 * For each involved variable, whether it is primitive type used in an assignment.	
	 */
	V8_PRIMITIVE_USED_IN_ASSIGNMENT,
	
	
	
	/**
	 * Method features.
	 * For each method invocation,whether there exist overloaded method.
	 */
	
	M1_OVERLOADED_METHOD,
	
	/**
	 * Method features.
	 * Method features.For each method invocation, whether there exist methods that return the same 
	 * type (or type compatible) and are similar in identifier name with the called
	 * method (again, we limit the search to the faulty class, search both method 
	 * definition and method invocations in the faulty class
	 */
	
	M2_SIMILAR_METHOD_WITH_SAME_RETURN,
	
	/**
	 * Method features.
	 * For each method invocation, whether has method definitions or method calls
	 * (in the fault class) that take the return type of the method invocation as
	 * one of its parameters and the return type of the method is type compatible
	 * with the return type of the method invocation.
	 */
	
	M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
	
	/**
	 * Method features.
	 * For each method invocation, whether the types of some of its parameters are
	 * same or compatible with the return type of the method.
	 */

	M4_PARAMETER_RETURN_COMPABILITY,
	
	/**
	 * Method features.
	 * For each method invocation, whether has variables in scope whose types are 
	 * the same or compatible with the return types of the method invocation. 
	 */
	
	M5_MI_WITH_COMPATIBLE_VAR_TYPE,
	
	
	
	M6_INV_Invocation_INV_REPLACE_BY_VAR,
	M7_INV_Invocation_INV_REPLACE_BY_INV,
	
	/**
	 * Method features.
	 * For each method invocation, whether the return value of it is primitive.
	 */

	M8_RETURN_PRIMITIVE,
	
	/**
	 * Method features.
	 * For each method invocation, whether the return value of it is the objective type.
	 */
	
	M9_RETURN_OBJECTIVE,
	
	/**
	 * Method features.
	 * For each method invocation, whether it is wrapped in other calls.
	 */
	
	M10_WRAPTTED_IN_OTHER_CALLS,
	
	/**
	 * Method features.
	 * For each method invocation, whether it is start with Get.
	 */
	
	M11_Satrt_With_Get,
	
	/**
	 * Method features.
	 * For variables in each method invocation, whether there exist other variables similar in name.
	 */
	
	M12_Has_Var_Similar_In_Name,
	
	/**
	 * Method features.
	 * For variables in each method invocation, whether the argument has primitive type.
	 */
	M13_Argument_Has_Primitive,
	
	
	/**
	 * Method features.
	 * For variables in each method invocation, whether there exist local variable not used.
	 */
	
	M1_LOCAL_VAR_NOT_USED,

	/**
	 * Method features.
	 * For variables in each method invocation, whether there exist local variable not assigned.
	 */
	
	
	M1_LOCAL_VAR_NOT_ASSIGNED,
	
	/**
	 * Method features.
	 * For each method invocation, whether there exist similar method invocation with normal guard.
	 */
	
	M2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
	
	/**
	 * Method features.
	 * For variables in each method invocation, whether there normal guard check for the primitive type.
	 */
	
	M5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	
	/**
	 * Method features.
	 * For variables in each method invocation, whether there exist similar object type variables with null guard.
	 */
	
	M2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	
	/**
	 * Method features.
	 * For variables in each method invocation, whether there exist similar PRIMITIVE type variables with null guard.
	 */
	
	M5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
	
	/**
	 * Method features.
	 * whether there exist field(s) not used in the faulty class.
	 */
	
	M4_Field_NOT_USED,
	/**
	 * Method features.
	 * whether there exist field(s) not assigned in the faulty class.
	 */
	M4_Field_NOT_ASSIGNED,
	/**
	 * Method features.
	 * In each method invocation, whether there exist object type used in assignment.
	 */
	M7_OBJECT_USED_IN_ASSIGNMENT,
	/**
	 * Method features.
	 * In each method invocation, whether there exist primitive type used in assignment.
	 */
	M8_PRIMITIVE_USED_IN_ASSIGNMENT,
	/**
	 * Method features.
	 * In each method invocation, whether the method call with normal guard.
	 */
	
	M9_METHOD_CALL_WITH_NORMAL_GUARD,
	/**
	 * Method features.
	 * In each method invocation, whether the method call with null guard.
	 */
	M10_METHOD_CALL_WITH_NULL_GUARD,
	
	/**
	 * Method features.
	 * In each method invocation, whether the method call with try catch block.
	 */
	M12_METHOD_CALL_WITH_TRY_CATCH,
	
	/**
	 * Constant features.
	 * For each constant c, whether exist other constants used in the faulty class
	 *  whose types are the same (or type compatible) with c but values are different
	 */
	
	
	C1_SAME_TYPE_CONSTANT,
	
	/**
	 * Constant features.
	 * For each constant c, whether exist other constants assigned by constants used in the faulty class
	 * whose types are the same (or type compatible) with c.
	 */
	
	C2_SAME_TYPE_CONSTANT_VAR,
	
	/**
	 * Constant features.
	 * For each constant c, whether exist other variables used in the faulty class
	 * whose types are the same (or type compatible) with c.
	 */
	
	C2_SAME_TYPE_VAR,
	

	/**
	 * Constant features.
	 * TBD
	 */
	
	C3_TYPEACCESS_ACTUAL_VAR,
	
	/**
	 * Constant features.
	 * TBD
	 */
	
	C4_SIMILAR_TYPEACCESS_ACTUAL_VAR,
	
	/**
	 * Constant features.
	 * For each constant c, whether it is an enum value.
	 */
	
	C5_USES_ENUMERATION,
	

	/**
	 * Constructor features.
	 * For the involved constructor, whether it has an overloaded constructor method.
	 */
	
	CON1_OVERLOADED_CONSTRUCTOR,
	/**
	 * Constructor features.
	 * For the involved constructor, whether it has a similar constructor method.
	 */
	
	CON2_SIMILAR_CONSTRUCTOR,
	
	/**
	 * Constructor features.
	 * For the involved constructor, it has been used by wrapped with other calls.
	 */
	
	CON3_WRAPTTED_IN_OTHER_CALLS,
	
	/**
	 * Constructor features.
	 * For the involved constructor, the return type is primitive.
	 */
	
	CON4_Return_Primitive,
	
	/**
	 * Constructor features.
	 * For the involved constructor, it is the faulty class constructor.
	 */
	
	CON5_Of_Class,
	
	/**
	 * Constructor features.
	 * For the involved constructor, it has arguments whose types are primitive.
	 */
	
	CON6_Argument_Has_Primitive,
	/**
	 * Constructor features.
	 *  For the involved constructor, it is called with a normal guard.
	 */
	
	CON9_METHOD_CALL_WITH_NORMAL_GUARD,
	/**
	 * Constructor features.
	 * For the involved constructor, it is called with a null guard.
	 */
	CON10_METHOD_CALL_WITH_NULL_GUARD,
	
	/**
	 * Constructor features.
	 * For the involved constructor, it is called with a try catch block.
	 */
	CON12_METHOD_CALL_WITH_TRY_CATCH,
	
	
	/**
	 * Expression features.
	 * Not used.
	 */
	
	E1_LOCAL_VAR_NOT_USED,
	E1_LOCAL_VAR_NOT_ASSIGNED,
	E2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
	E5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	E2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	E5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
	E4_Field_NOT_USED,
	E4_Field_NOT_ASSIGNED,
	E7_OBJECT_USED_IN_ASSIGNMENT,
	E8_PRIMITIVE_USED_IN_ASSIGNMENT,
	E9_METHOD_CALL_WITH_NORMAL_GUARD,
	E10_METHOD_CALL_WITH_NULL_GUARD,
	
	E1_RETURN_PRIMITIVE, 
	E2_COMPATIBLE_INVOCATION_PAREMETER_RETURN,
	E3_COMPATIBLE_INVOCATION_PAREMETER,
	
	/**
	 * Operator features.
	 * Whether it is a logical operator.
	 */
	O1_IS_LOGICAL,
	/**
	 * Operator features.
	 * Whether it is a bit operator.
	 */
	O1_IS_BIT,
	/**
	 * Operator features.
	 * Whether it is a compare operator.
	 */
	O1_IS_COMPARE, 
	/**
	 * Operator features.
	 * Whether it is a shift operator.
	 */
	O1_IS_SHIFT,
	/**
	 * Operator features.
	 * Whether it is a math operator.
	 */
	O1_IS_MATH,
	
	/**
	 * Operator features.
	 * Whether it is a other operator.
	 */
	O1_IS_OTHERS,

	/**
	 * Operator features.
	 * Whether it contains ! operator.
	 */
	O2_LOGICAL_CONTAIN_NOT,
	/**
	 * Operator features.
	 * Whether it contains null.
	 */
	O3_CONTAIN_NULL,
	/**
	 * Operator features.
	 * Whether it contains zero or one.
	 */
	O3_CONTAIN_01,
	/**
	 * Operator features.
	 * Whether it is used in comparison in condition.
	 */
	O4_COMPARE_IN_CONDITION,
	/**
	 * Operator features.
	 * Whether it is the root of math operation.
	 */
	O5_IS_MATH_ROOT,	
	
	/**
	 * P4J repair features.
	 * Whether repair action removes only the condition (guard).
	 */

	P4J_SRC_REMOVE_PARTIAL_IF,
	/**
	 * P4J repair features.
	 * Whether repair action removes part of statements in the condition.
	 */
	
	P4J_SRC_REMOVE_STMT,
	/**
	 * P4J repair features.
	 * Whether repair action removes the whole block (not condition).
	 */
	P4J_SRC_REMOVE_WHOLE_BLOCK,
	/**
	 * P4J repair features.
	 * Whether repair action  removes the whole if block (condition).
	 */
	P4J_SRC_REMOVE_WHOLE_IF,
	/**
	 * P4J repair features.
	 * Whether repair action  replaces the condition (condition).
	 */
	P4J_SRC_REPLACE_COND_RF,
	/**
	 * P4J repair features.
	 * Whether repair action replaces some statements.
	 */
	P4J_SRC_REPLACE_STMT_RF,
	/**
	 * P4J repair features.
	 * Whether repair action inserts some control loops.
	 */
	P4J_SRC_INSERT_CONTROL_RF,
	/**
	 * P4J repair features.
	 * Whether repair action inserts a guard.
	 */
    P4J_SRC_INSERT_GUARD_RF,
    /**
	 * P4J repair features.
	 * Whether repair action inserts statement.
	 */
    P4J_SRC_INSERT_STMT_RF,
	
}
