#  ComRepair: Revision Classification 


We classify each revision into which program-repair-tool's search space it may lie. 
Our goal is to make a module in coming which takes a software repository or a list of revisions as an input 
and for each commits/revision it outputs a list of program repair tools. 
If commit `c` is classified as `x`, it means that `c` might have been produced by the program repair tool `x` 
or in order words `c` lies in the search space of `x` tool.


## Possible Coming Specification Improvement 
- Binary Operator Kind or other kind of equality
- Multiple pattern within a 

## Program Repair Tools
A list of program repair tools we are going to handle:- 
 
 ### jMutRepair 
 
 Paper: [Using Mutation to Automatically Suggest Fixes for Faulty Programs ](http://www.utdallas.edu/~ewong/SE6367/01-Project/08-SFL-papers/10-Automatically-Suggest-Fixes.pdf)

 #### Research Summary
 The paper experiments with the following settings:
 - It *replaces* the following operators with an operator of *the same class*.
     - Arithmetic
     - Relational
     - Logical
     - Increment/Decrement
     - Assignment (TODO: What could be changed in this?)
 - Or it can do *negation* in an `if` or `while` statement.
 
 A mutant(the program after one repair operation/change) is killed if any test case fails. 
 This means that the patch generated can contain only one change.  
 
 ##### Real-world patches note
 But these settings can change, for example negation an be considerd in `for` statement or `ternary` statements. 
 Or more operations could be considered. Like? TODO:  
 
 #### Dataset
 It's present [here](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/master/DRR/D_unassessed_init/jMutRepair/). It will be published later. 
 
 #### Implementation Details
 
 Actions:
 - Update (for binary)
 - Others (for unary)
 
 Entities:
 - BinaryOperator
 - UnaryOperator
 - Assignment
 - Conditionals
     - If
     - While
     - Do
     - For
     
 - TODO: Discuss what `action` are allowed? Just UPD or INS or DEL or ? in the case of unary?
 - TODO: Check if the operator(Updates) belong to the same class.
 - TODO: One line change? No - to many variations to consider?
     
 #### Pattern
 
 This pattern is able detect almost all commits in jMutRepair. 

 ```xml
<pattern name="binary">
	<entity id="1" type="BinaryOperator"/>
	<action entityId="1" type="UPD"/>
</pattern>
```
```xml
<pattern name="unary">
    <entity id="2" type="UnaryOperator"/>
    <action entityId="2" type="ANY"/>
</pattern>
```


 ### Nopol
 
 Paper: [Nopol: Automatic Repair of Conditional Statement Bugs in Java Programs](https://hal.archives-ouvertes.fr/hal-01285008/file/nopol.pdf)
 
 #### Research Summary
 It fixes two types of bug:
 - changing If condition
 - Inserting precondition/pre-check
 
 #### Real-world patches note
 None
 
 #### Dataset
 - [2017](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/master/DRR/D_unassessed_init/Nopol2017)
 - [2015_1](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/master/DRR/D_correct_init/Nopol2015)
 - [2015_2](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/master/DRR/D_incorrect_init/Nopol2015)
 
 #### Implementation Details
 Entities: 
 - If
 Actions
 - UPD
 - INS
 
 #### Pattern
 ```xml
<pattern name="if_condition_upd">
    <entity id="1" type = "If"/>
    <entity id="2" type = "*" role = "condition">
        <parent parentId="1" distance="1" /> <!-- TODO: distance -->
    </entity>
    
    <action entityId ="2" type = "UPD" />
</pattern>
```

```xml
<pattern name="if_condition_ins">
    <entity id="1" type="*"> <!-- TODO: type. should it be block? -->
        <parent parentId="2" distance="10" /> <!-- TODO: distance -->
    </entity>
    <entity id="2" type="If" />
    
    <action entityId="2" type="INS" />
    <action entityId="1" type="MOV" />
</pattern>

```
 
 ### GenProg
 
 Paper: [GenProg: A Generic Method for Automatic Software Repair](https://ieeexplore.ieee.org/document/6035728)
 
 #### Research Summary
 #### Real-world patches note
 #### Dataset
 #### Implementation Details
 #### Pattern
 
 ### NpeFix
 
  Paper:
  
  #### Research Summary
  #### Real-world patches note
  #### Dataset
  #### Implementation Details
  #### Pattern
 