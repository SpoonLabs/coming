# ODS Overfitting detection system

For an overview of ODS, see [
Automated Classification of Overfitting Patches with Statically Extracted Code Features](https://arxiv.org/pdf/1910.12057.pdf)

If you use ODS or ODS' features, please cite:

```
Automated Classification of Overfitting Patches with Statically Extracted Code Features. H. Ye, J. Gu, M. Martinez, T. Durieux, M. Monperrus. arxiv:1910.12057

```


## Features

ODs has 3 sets of features.

1. [Contextual  Syntactic  Features](#contextual-syntactic-features)

2. [Repair Pattern Features](#repair-pattern-features)

3. [Code Description Features](#code-description-features)


### Contextual  Syntactic  Features

Given a target statement, which can be the buggy statement, ODS computes the following features. 

####  Contextual  Syntactic  Features: Type Features

ODS has 8 features, of type `String`, that indicates the type (e.g., `If`, `While`, `assignment`) of a particular  statement. For example, `typeOfFaultyStatementAfter2` indicates the type of the statements that is two (`2`) statement below (`after`) the buggy statement.

Example:

```
int a = 0; --> before 3
call(a); --> before 2
a++; --> before 1
call2(a); --> target statement
int b = 10; --> after 1
call2(b); --> after 2
return sum(a,b); --> after 3
```


* typeOfFaultyStatementAfter1:  type of the statement located just below the target statement
* typeOfFaultyStatementAfter2: type of the statement located two statements after the target statement
* typeOfFaultyStatementAfter3: type of the statement located three statements after the target statement 
* typeOfFaultyStatementBefore1:  type of the statement located just before the target statement
* typeOfFaultyStatementBefore2:  type of the statement located two statements before the target statement
* typeOfFaultyStatementBefore3: type of the statement located three statements before the target statement
* typeOfFaultyStatement: type of the target statement
* typeOfFaultyStatementParent: type of the parent statement (e.g., an `if`, `while` in case the target element is inside the `if`, `while` block)

A last boolean feature related to Type is: 

* faultyClassExceptionType: indicates whether the associated method or class for the faulty line throws exception.



####  Contextual  Syntactic  Features: Method features

ODS has 7  binary features related to the context of *Methods*.
 
 * methodCallWithNullGuard: indicates if the method has a null guard as parent
 * methodCallWithTryCatch: indicates if the method has try catch as parent
 * inSynchronizedMethod:  indicates if the called method is synchronized
 * hasObjectiveMethodCall: 
 * methodThrowsException: indicates if the called method throws an exception
 * methodCallWithNormalGuard:  indicates if the method has a guard as parent (not necessary a null check guard)
 * hasInvocationsProneException:

#### Contextual  Syntactic  Features: Similarity features

ODS has 4 binary features related to the similarity.

 * similarObjectTypeWithNormalGuard:  indicates if given a Object (no primitive) variable from the target statement is used somewhere else inside a normal guard (i.e., one that does not check `null`)
 * similarObjectTypeWithNullGuard: indicates if given a Object (no primitive) variable from the target statement is used somewhere else inside a Null guard
 * similarPrimitiveTypeWithNormalGuard: indicates if given a  primitive variable (e.g., `int`, `bool`, `float`) from the target stateament is used somewhere else inside a normal guard (i.e., one that does not check `null`)
 * similarPrimitiveTypeWithNullGuard: indicates if given a  primitive variable (e.g., `int`, `bool`, `float`) from the target statement is used somewhere else inside a Null guard
 
 
#### Contextual  Syntactic  Features: Usage 

ODS has 6 binary  features related to the usage of code elements (fields, variables, etc).

 * fieldNotAssigned: indicates if there is a  field that has not been assigned anywhere inside the class that contains the target statement.
 * fieldNotUsed: indicates if there is a field  not used  inside the method that contains the target statement.
 * localVarNotAssigned: indicates if there is a local variable not assigned anywhere  inside the method that contains the target statement.
 * localVarNotUsed:  indicates if there is a local variable not used inside the method that contains the target statement.
 * objectUsedInAssignment: indicates if there is a object variable used in the right side of an  assignment  located in the method that contains the target statement.
 * primitiveUsedInAssignment: indicates if there is a primitive variable used in the right side of an assignment  located in the method that contains the target statement.
 


### Repair Pattern Features 

There are 26 binary features related to repair patterns. 
They indicates whether a patch applied to a buggy locations  contains particular transformation such as those indicated by the pattern.

 * wrapsLoop: adds a loop that wraps the buggy statement.
 * wrapsTryCatch: adds a try-catch that wraps the buggy statement.
 * wrapsIfElse: : adds an if-else that wraps the buggy statement.
 * wrapsIf: adds an if that wraps the buggy statement.
 * wrongMethodRef: replaces a method call from the buggy statement by another
 * constChange: changes a constant from the buggy statement.
 * unwrapIfElse: removes a loop that wraps the buggy statement.
 * unwrapTryCatch: removes a loop that wraps the buggy statement.
 * expArithMod: changes an arithmetic expression from the buggy statement.
 * codeMove: the patch moves code.
 * expLogicExpand: a logic expression from the buggy statement is expanded
 * condBlockOthersAdd: 
 * wrapsElse: adds a else that wraps the buggy statement.
 * wrapsMethod: adds a method invocation where the parameter is the buggy statement (it must be an expression)
 * wrongVarRef: replaces a variable reference by another
 * condBlockRem: removes a conditional block
 * unwrapMethod: removes a method invocation but it keeps one of the parameters in its place.
 * singleLine: add a single line
 * missNullCheckP: adds a missing null check
 * missNullCheckN: adds a missing null check
 * condBlockExcAdd: TO BE DEFINED
 * copyPaste: inserts a code already existing in the buggy class.
 * condBlockRetAdd: adds a return statement in a block
 * expLogicReduce: removes one one term from a logic expression
 * expLogicMod: modifies a logic expression
 * notClassified: another change 


### Code Description Features

There are 50 binary features related to the code from a patch: 


* ABST_V_AF: TO BE DEFINED
* ASSIGN_CONST_AF: the patch has an assignment that has a constant in the right part
* ASSIGN_LHS_AF the patch was an assignment
* ASSIGN_ZERO_AF: the patch has an assignment that has a zero constant (0) in the right part
* CALLEE_AF: the patch has a called element (an invocation)
* CALL_ARGUMENT_AF:  the patch has an argument from a method invocation.
* CHANGED_AF: the patch has an operator that change the value of one var (++a --a a++ a-- += -= *= /= =)
* DEREF_AF: TO BE DEFINED
* FUNC_ARGUMENT_VF: 
* GLOBAL_VARIABLE_VF : the patched code access to a global variable
* INDEX_AF: the patch has an index [] operator
* INSERT_CONTROL_RF: inserting a potentially guarded control statement before a program point 
* INSERT_GUARD_RF:  adding a guard condition to an existing statement
* INSERT_STMT_RF: inserting a non-control statement before a program point 
* LOCAL_VARIABLE_VF: the patched code access to a local variable
* MEMBER_ACCESS_AF: the patched code access to a member (field)
* MEMBER_VF: the patch includes the declaration of a member (filed)
* MODIFIED_SIMILAR_VF: if an expression was modified and the modified code is similar to the unmodified code.
* MODIFIED_VF: if an expression was modified
* NONZERO_CONST_VF: the patch has a literal different to zero (!= 0)
* OP_ADD_AF: has a + operator
* OP_DIV_AF: has a - operator
* OP_EQ_AF: : has a == operator
* OP_GE_AF: has a >= operator
* OP_GT_AF: has a > operator
* OP_LE_AF: has a <= operator
* OP_LT_AF: has a < operator
* OP_MOD_AF: has a % operator
* OP_MUL_AF: has a * operator
* OP_NE_AF: has a != operator
* OP_SUB_AF: has a - operator
* REMOVE_PARTIAL_IF: deleting a guard condition from an existing statement
* REMOVE_STMT: the patch removes an statement
* REMOVE_WHOLE_BLOCK: the patch removes a complete block
* REMOVE_WHOLE_IF:  the patch removes a complete if condition
* REPLACE_COND_RF: replaces a condition
* REPLACE_STMT_RF: replaces an existing statement 
* R_STMT_ASSIGN_AF:  the patch, which replaces code,  has an assignment 
* R_STMT_CALL_AF:  the patch, which replaces code,  has method call
* R_STMT_COND_AF: the patch, which replaces code,  has an If
* R_STMT_CONTROL_AF:  the patch, which replaces code,  has a break, continue, return, throw,
* SIZE_LITERAL_VF: the patch has a literal
* STMT_ASSIGN_AF the patch has an assignment
* STMT_CALL_AF: the patch has method call
* STMT_COND_AF: the patch has an If
* STMT_CONTROL_AF: the patch has a break, continue, return, throw,
* STMT_LABEL_AF: the patch has a labeled element (e.g., break)
* STMT_LOOP_AF: the patch has a loop
* STRING_LITERAL_VF: the patch has a literal
* UOP_DEC_AF: the patch has an increment operator ++a a++
* UOP_INC_AF:  the patch has a decrement  operator --a a--
* ZERO_CONST_VF: the patch has a literal equals to zero (0)



