#  ComRepair: Revision Classification 


We classify each revision into which program-repair-tool's search space it may lie. 
Our goal is to make a module in coming which takes a software repository or a list of revisions as an input 
and for each commits/revision it outputs a list of program repair tools. 
If commit `c` is classified as `x`, it means that `c` might have been produced by the program repair tool `x` 
or in order words `c` lies in the search space of `x` tool.


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
 But these settings can change, for example negation an be considered in `for` statement or `ternary` statements. 
 Or more operations could be considered. Depends on the implementation. 
 
 #### Dataset
 It's present [here](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/comrepair-coming/coming_data/jMutRepair). It will be published later. 
 
 #### Implementation Details

We use two patterns files:
- [binary.xml](https://github.com/SpoonLabs/coming/blob/master/src/main/resources/repairability/JMutRepair/binary.xml):  
    This file is responsible for getting any sort of `UPDATE` in the Binary Operator. For example: `+` -> `-` or left operand is changed
    We don't require to add check of the source and target node of the BinaryOperator having the same class of Operator because changing the class of an operator will result in an uncompilable program.  
    jMutRepair tool can't change the operands of the Binary Operator. We discard instances returned by this pattern if the operand has been changed. This check is applied in [the filter function ofJMutRepair.](https://github.com/SpoonLabs/coming/blob/master/src/main/java/fr/inria/coming/repairability/repiartools/JMutRepair.java)
    
- [unary.xml](https://github.com/SpoonLabs/coming/blob/master/src/main/resources/repairability/JMutRepair/unary.xml)
    This pattern checks for insertion or deletion of any unary operator.
    Possible problems with the current pattern are:
        - increments and decrements can only to be changed/upd but right now insert and delete are also accepted
        - `!` can be inserted or deleted but right now update is also accepted
        - `+`,`-`,`~`(from the perspective of unary operator) are accepted here but they shouldn't be
    
    TODO: To make it better(above points + )


 ### Nopol
 
 Paper: [Nopol: Automatic Repair of Conditional Statement Bugs in Java Programs](https://hal.archives-ouvertes.fr/hal-01285008/file/nopol.pdf)
 
 #### Research Summary
 It fixes two types of bug:
 - Changing if-condition (IF-UPD)
 - Inserting precondition or if-statement (IF-INS)
 
 #### Real-world patches note
 None
 
 #### Dataset
 - [here](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/comrepair-coming/coming_data/Nopol)
 - 103 total patches 
    - 81 patches for insertion of if
    - 22 patches or update in if-condition

 #### Implementation Details
 
 We have four patterns files. Two for IF-INS and two for IF-UPD.
 
**Finding multiple instances for a single pattern in a single diff**
 
The xml below is supposed to match a if-condition update. 
Simply checking entity 2 for a change doesnt't always work. Case: Only one thing deep(wrt AST) within the condition is changed and the entity 2 remains unchanged.
So we search for any sort of change within the condition at any level of children. 
 ```xml
<pattern name="if_condition_upd_deep">

    <entity id="1" type="If"/>
    <entity id="2" type="*" role="condition">
        <parent parentId="1" distance="1"/>
    </entity>
    <entity id="4" type="*">
        <parent parentId="2" distance="1000000"/>
    </entity>
    <action entityId="4" type="*"/>

    <entity id="3" type="*" role="Then">
        <parent parentId="1" distance="1"/>
    </entity>
    <action entityId="3" type="UNCHANGED_HIGH_PRIORITY"/>
</pattern>

```
If the if-condition update is very complicated, the above pattern can produce something like below for even a single if-condition update:
```
[ChangePatternInstance [actions=[Insert VariableRead at com.google.javascript.jscomp.LightweightMessageFormatter:97                                                                              
        error                                                                                                                                                                                    
]], ChangePatternInstance [actions=[Update Literal at com.google.javascript.jscomp.LightweightMessageFormatter:98                                                                                
         to null                                                                                                                                                                                 
]], ChangePatternInstance [actions=[Move Literal from com.google.javascript.jscomp.LightweightMessageFormatter:98 to com.google.javascript.jscomp.LightweightMessageFormatter:97                 
        0                                                                                                                                                                                        
]], ChangePatternInstance [actions=[Delete TypeAccess at com.google.javascript.jscomp.LightweightMessageFormatter                                                                                
        com.google.javascript.jscomp.LightweightMessageFormatter                                                                                                                                 
]], ChangePatternInstance [actions=[Delete FieldRead at com.google.javascript.jscomp.LightweightMessageFormatter:97                                                                              
        excerpt                                                                                                                                                                                  
]], ChangePatternInstance [actions=[Delete TypeAccess at com.google.javascript.jscomp.LightweightMessageFormatter:97                                                                             
        SourceExcerpt.LINE...
```
 
 
 
 ### GenProg
 
 Paper: [GenProg: A Generic Method for Automatic Software Repair](https://ieeexplore.ieee.org/document/6035728)
 
 #### Research Summary
  - It can insert a statement, that is already present in the source code
  - It can delete any statement
  - It can do both of the above.
  - It can swap statements.
  
 #### Real-world patches note
  - We are only considering patches in which the inserted/updated/swapped statement is already preset in the same file
 
 #### Dataset
 - It's present [here](https://github.com/kth-tcs/defects4j-repair-reloaded/tree/comrepair-coming/coming_data/JGenProg)
 - ~23 patches in which the inserted statement is not present in the source file
 
 #### Implementation Details
 In this module our approach is detect all the actions(i.e. INS, DEL, UPD, or MOV) at statement level.
 If the new statement was present in the previous version of the file, then the instance most probably was generated by JGenProg.
 
 MOV action can belong to insert + delete action but it can also generate false-positives were statements were moved because of any kind of insertion in other places. [TODO]
 DEL action is same as in the paper.
 INS action is same as in the paper.
 
 
 ### NpeFix

  Paper: 
  
  #### Research Summary
  #### Real-world patches note
  #### Dataset
  #### Implementation Details
  #### Pattern
 
