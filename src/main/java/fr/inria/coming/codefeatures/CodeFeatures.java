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
	S1_LOCAL_VAR_NOT_ASSIGNED, //
	S1_LOCAL_VAR_NOT_USED,
	S7_OBJECT_USED_IN_ASSIGNMENT,
	S8_PRIMITIVE_USED_IN_ASSIGNMENT,
	S9_METHOD_CALL_WITH_NORMAL_GUARD,
	S10_METHOD_CALL_WITH_NULL_GUARD,
	S11_FAULTY_CLASS_EXCEPTION_TYPE,
	S12_METHOD_CALL_WITH_TRY_CATCH,
	// If the faulty statement involves using object type variables (either local or
	// global), whether exist other statements in the faulty class that use some
	// same type object variables (with same of the object type variables used in
	// the faulty statement), but add guard check (wrap with if or if-else, and not
	// null or non- null related check) (for closure-111, the faulty statement uses
	// variable topType, whose type is JSType, and there are many other statements
	// in the faulty class which uses JSType variables, but have gurand checks, like
	// statement in 61, in 72. also see for example closure 60.)
	S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, //
	S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	// Spoon class of the fault statement.
	S3_TYPE_OF_FAULTY_STATEMENT,
	S14_TYPE_OF_FAULTY_STATEMENT_PARENT,
	
	S15_HAS_OBJECTIVE_METHOD_CALL,
	S16_HAS_Invocations_Prone_Exception,
	S18_In_Synchronized_Method,
	
	S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1,
	S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2,
	S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3,
	S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1,
	S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2,
	S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3,
	
	// If the faulty statement involves object reference to field (i.e., use object
	// type class field), do there exist certain field(s) that have never been
	// referenced in other methods of the faulty class.
	// S4_USED_FIELD,
	S4_Field_NOT_USED,
	S4_Field_NOT_ASSIGNED,
	// If the faulty statement involves using primitive type variables (either local
	// or global),
	// whether exist other statements in the faulty class that use some same
	// primitive type variables (with some of the primitive type variables used in
	// the faulty statement), but add guard check (for global variables
	S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	
	S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,

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
	LE5_COMPLEX_REFERENCE,
	// For the logical expression, whether there exists a boolean expression that
	// starts with the "not" operator! (an exclamation mark) (
	LE6_HAS_NEGATION,
	// For the logical expression, whether there exists a boolean expression which
	// is simply a boolean variable
	LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC,
	// If the logical expression only uses local variables,whether all of the local
	// variables have been used in other statements (exclude statements inside
	// control flow structure) since the introduction
	//LE8_LOGICAL_WITH_USED_LOCAL_VARS,
	LE8_SCOPE_VAR_USED_OTHER_BOOLEXPER,
	
	LE9_NORMAL_CHECK,
	
	LE9_NULL_CHECK,
	
	LE9_MIX_CHECK,
	
	LE9_EQUAL_NOTEQUAL_NULL_CHECK,
	
	LE10_ATOMIC_EXPRESSION_SAME_INVOCATION_TARGET,
	
	LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_LEFT,
	
	LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_RIGHT,
	
	LE10_ATOMIC_EXPRESSION_MULTIPLE_VAR_AS_BOOLEAN, 
	
	LE10_ATOMIC_EXPRESSION_USED_IN_INVOCATION_COMPARISION_VARIABLE, 
	
	LE10_CONTAINS_ALL_INVOCATION_COMPARISION_VARIABLE,
	
	// For each involved variable, whether has method definitions or method calls
	// (in the fault class) that take the type of the involved variable as one of
	// its parameters and the return type of the method is type compatible with the
	// type of the involved variable
	V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
	// has any other variables in scope that are similar in identifier name and type
	// compatible.
	V2_HAS_VAR_SIM_NAME_COMP_TYPE,
	V2_HAS_VAR_SIM_NAME,
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
	V16_IS_METHOD_PARAMETER_TYPE_VAR,
	// For each variable, is it primitive type?
	V8_VAR_PRIMITIVE,
	V8_VAR_OBJECT,
	// For each method invocation, whether the method has overloaded method
	V9_VAR_TYPE_Similar_Literal,	
	V10_VAR_TYPE_Similar_VAR,	
	V11_VAR_COMPATIBLE_TYPE_IN_CONDITION,	
	V12_VAR_Invocation_VAR_REPLACE_BY_VAR,	
	V13_VAR_Invocation_VAR_REPLACE_BY_INVOCATION,	
	V14_VAR_INSTANCE_OF_CLASS, 	
	V15_VAR_LAST_THREE_SAME_TYPE_LOC, 
	V17_VAR_IS_ENUMERATION,
	V18_Has_Method_Similar_In_Name,
	V19_With_Special_Name,
	
	
	V1_LOCAL_VAR_NOT_USED,
	V1_LOCAL_VAR_NOT_ASSIGNED,
	V2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
	V5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	V2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	V5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
	V4_Field_NOT_USED,
	V4_Field_NOT_ASSIGNED,
	V7_OBJECT_USED_IN_ASSIGNMENT,
	V8_PRIMITIVE_USED_IN_ASSIGNMENT,
	
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
	M6_INV_Invocation_INV_REPLACE_BY_VAR,
	M7_INV_Invocation_INV_REPLACE_BY_INV,
	// For each method invocation, whether the return value of it is primitive
	M8_RETURN_PRIMITIVE,
	M9_RETURN_OBJECTIVE,
	M10_WRAPTTED_IN_OTHER_CALLS,
	M11_Satrt_With_Get,
	M12_Has_Var_Similar_In_Name,
	M13_Argument_Has_Primitive,
	
	M1_LOCAL_VAR_NOT_USED,
	M1_LOCAL_VAR_NOT_ASSIGNED,
	M2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
	M5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
	M2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
	M5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
	M4_Field_NOT_USED,
	M4_Field_NOT_ASSIGNED,
	M7_OBJECT_USED_IN_ASSIGNMENT,
	M8_PRIMITIVE_USED_IN_ASSIGNMENT,
	M9_METHOD_CALL_WITH_NORMAL_GUARD,
	M10_METHOD_CALL_WITH_NULL_GUARD,
	M12_METHOD_CALL_WITH_TRY_CATCH,
	
	// C1: For each constantc, whether exist other constants used inthe faulty class
	// whose types are the same (or type compatible)withcbut values are different
	C1_SAME_TYPE_CONSTANT,
	C2_SAME_TYPE_CONSTANT_VAR,
	C2_SAME_TYPE_VAR,
	C3_TYPEACCESS_ACTUAL_VAR,
	C4_SIMILAR_TYPEACCESS_ACTUAL_VAR,
	// For each constant, is it an enum vlaue (But may be it ishard to detect it use
	// partial program analysis).
	C5_USES_ENUMERATION,
	
	CON1_OVERLOADED_CONSTRUCTOR,
	CON2_SIMILAR_CONSTRUCTOR,
	CON3_WRAPTTED_IN_OTHER_CALLS,
	CON4_Return_Primitive,
	CON5_Of_Class,
	CON6_Argument_Has_Primitive,
	CON9_METHOD_CALL_WITH_NORMAL_GUARD,
	CON10_METHOD_CALL_WITH_NULL_GUARD,
	CON12_METHOD_CALL_WITH_TRY_CATCH,
	
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
	
	O1_IS_LOGICAL,
	O1_IS_BIT,
	O1_IS_COMPARE, 
	O1_IS_SHIFT,
	O1_IS_MATH,
	O1_IS_OTHERS,
	O2_LOGICAL_CONTAIN_NOT,
	O3_CONTAIN_NULL,
	O3_CONTAIN_01,
	O4_COMPARE_IN_CONDITION,
	O5_IS_MATH_ROOT
	// For each arithmetic expression, whether has method definitions or method
	// calls (in the fault class) that take the return type of the arithmetic
	// expression as one of its parameters and the return type of the method is
	// type compatible with the return type of the arithmetic expression.
}
