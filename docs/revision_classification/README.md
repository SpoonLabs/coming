#  ComRepair: Revision Classification 


We classify each revision into which program-repair-tool's search space it may lie. 
Our goal is to make a module in coming which takes a software repository or a list of revisions as an input 
and for each commits/revision it outputs a list of program repair tools. 
If commit `c` is classified as `x`, it means that `c` might have been produced by the program repair tool `x` 
or in order words `c` lies in the search space of `x` tool.


## Possible Coming Specification Improvement 
- Binary Operator Kind or other kind of equality
- Multiple pattern within a single .xml

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
 Or more operations could be considered. Like? 
 
 #### Dataset
 It's present [here](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/comrepair-coming/coming_data/jMutRepair). It will be published later. 
 
 #### Implementation Details
 
 This pattern is able detect almost all commits in jMutRepair. 

 ```xml
<pattern name="binary">

    <entity id="1" type="BinaryOperator"/>
    <action entityId="1" type="UPD"/>

    <!-- For making sure that operands remains the same -->
    <entity id="2" type="*" role="LEFTOPERAND">
        <parent parentId="1" distance="1"/>
    </entity>
    <action entityId="2" type="UNCHANGED_HIGH_PRIORITY"/>

    <entity id="3" type="*" role="RIGHTOPERAND">
        <parent parentId="1" distance="1"/>
    </entity>
    <action entityId="3" type="UNCHANGED_HIGH_PRIORITY"/>

</pattern>

```
We don't require to add check of the src and dst node having BinaryOperator of the same class.
because changing the class of an operator will result in an uncompilable program.
**Problems:**
 - How to make sure that the entities involved are not changed? Example: (a == b) -> (b != c) doesnt happen.


```xml
<pattern name="unary">
    <entity id="1" type="UnaryOperator"/>
    <action entityId="1" type="ANY"/>


    <!-- For making sure that operands remains the same -->
    <entity id="2" type="*" role="OPERAND">
        <parent parentId="1" distance="1"/>
    </entity>
    <action entityId="2" type="UNCHANGED_HIGH_PRIORITY"/>
</pattern>
```
**Problems:**
 - increments and decrements can only to be changed/upd not inserted
 - `!` can be inserted or deleted. 
 - Are `+`,`-`,`~` also considered here?
 



 ### Nopol
 
 Paper: [Nopol: Automatic Repair of Conditional Statement Bugs in Java Programs](https://hal.archives-ouvertes.fr/hal-01285008/file/nopol.pdf)
 
 #### Research Summary
 It fixes two types of bug:
 - changing If condition
 - Inserting precondition/pre-check
 
 #### Real-world patches note
 None
 
 #### Dataset
 - [here](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/comrepair-coming/coming_data/Nopol)
 - 103 total patches 
    - 81 INS patches
    - 22 UPD patches

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
 
