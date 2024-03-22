Coming
=======
Coming is a tool for commit analysis in git repositories.

If you use Coming, please cite: 

* [Coming: a Tool for Mining Change Pattern Instances from Git Commits](http://arxiv.org/pdf/1810.08532). M. Martinez, M. Monperrus, Proceedings of ICSE, 2019 ([doi:10.1109/ICSE-Companion.2019.00043](https://doi.org/10.1109/ICSE-Companion.2019.00043)). [bibtex](https://www.monperrus.net/martin/bibtexbrowser.php?key=1810.08532&bib=monperrus.bib)

Contact: 

[Matias Martinez](http://www.martinezmatias.com/), [Martin Monperrus](http://www.monperrus.net/martin/)

## Install

Coming is deployed on Maven Central, see [past versions](https://repo1.maven.org/maven2/com/github/spoonlabs/coming/).

To build yourself, the procedure is as follows.

Add a github token in `.m2/settings.xml`.

```xml
<settings>
  <servers>
    <server>
      <id>brufulascam</id>
      <username>yourlogin</username>
      <!-- your github token with scope read:packages -->
      <password>FOOBAR</password>
    </server>
  </servers>
</settings>
```


Install a JDK 17 and configure Maven or your IDE to use it.


```
$ export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
$ mvn -version
Apache Maven 3.6.3
Maven home: /usr/share/maven
Java version: 17.0.9, vendor: Private Build, runtime: /usr/lib/jvm/java-17-openjdk-amd64

# now installing
$ mvn install -DskipTests
```

Tests:

```
git clone https://github.com/SpoonLabs/repogit4testv0
mvn test
```


`repogit4testv0` is a GIT repository included inside Coming which is used by the test cases.


## Run with main class


The main class is: `fr.inria.coming.main.ComingMain`.

```
mvn exec:java -Dexec.mainClass=fr.inria.coming.main.ComingMain

 -action <INS | DEL | UPD | MOV | PER | ANY>                          tye of action to be mined
 -branch <branch name>                                                In case of -input='git', use this branch name. Default is master.
 -entitytype <arg>                                                    entity type to be mine
 -entityvalue <arg>                                                   the value of the entity  mentioned in -entitytype
 -filter <arg>                                                        name of the filter
 -filtervalue <arg>                                                   values of the filter  mentioned in -filter
 -hunkanalysis <arg>                                                  include analysis of hunks
 -input <git(default) | files | filespair | repairability>            format of the content present in the given -path. git implies that the path is a git repository. files implies the path contains .patch files
 -location <path>                                                     analyse the content in 'path'
 -message <arg>                                                       comming message
 -mode <mineinstance | diff | features>                               the mode of execution of the analysis
 -output <path>                                                       dump the output of the analysis in the given path
 -outputprocessor <classname>                                         output processors for result
 -parameters <arg>                                                    Parameters, divided by :
 -parentlevel <arg>                                                   numbers of AST node where the parent is located. 1 implies immediate parent
 -parenttype <arg>                                                    parent type of the nodes to be considered
 -pattern <path>                                                      path of the pattern file to be used when the -mode is 'mineinstance'
 -patternparser <classname>                                           parser to be used for parsing the file specified -pattern. Default is XML
 -repairtool <ALL | JMutRepair | Nopol | JKali | NPEfix | JGenProg>   If -mode=repairability, this option specifies which repair tools should we consider in our analysis. Can be a list separated by :
 -showactions                                                         show all actions
 -showentities                                                        show all entities
```

**Parameters** Most of the properties are configured in file `config-coming.properties`

One can change any of those properties from the command line by using   `-parameters`

The value of those argument are the following format `<name_property_1>:<value_property_1>:<name_property_2>:<value_property_2> `

In the following command we change the value of two properties: `max_nb_hunks` and `max_files_per_commit` 

``` 
   -parameters max_nb_hunks:2:max_files_per_commit:1
```

## Modes

### Mode Instance Mining

When running Coming in mode `-mode mineinstance` the output is a file name `instances_found.json` , which shows the different instances of the pattern passed as parameter.

* [Automatically Extracting Instances of Code Change Patterns with AST Analysis](https://hal.inria.fr/hal-00861883/file/paper-short.pdf) (Martinez, M.; Duchien, L.; Monperrus, M.) IEEE International Conference on Software Maintenance (ICSM), pp.388-391, 2013, doi: 10.1109/ICSM.2013.54 [bibtex](https://www.monperrus.net/martin/bibtexbrowser.php?key=martinez%3Ahal-00861883&bib=monperrus.bib)


#### Mining Simple Changes (i.e., with exactly one change)



Extract all commits of `repogit4testv0` that insert a binary operator AST node


```
java -classpath ./coming.jar fr.inria.coming.main.ComingMain -location  ./repogit4testv0/ -mode mineinstance -action INS -entitytype BinaryOperator   -output ./out
```

The argument `-mode` indicates the analyzer that Coming will use.
The value `-mode mineinstance` means to detect instances of a change pattern (in the previous example, insert a binary operator AST node).

The argument `-location` indicates the location of the project to analyze. 
By default, Coming analyzes Git projects(as per `-input`), so the `-location`  should be the path to the cloned project. Moreover, the argument `branch` allows to specify the Git branch to analyze (by default, it analyzes  the `master` branch).


The argument  `-output` is used to indicate the folder where Coming will write the results.

To know the values accepted by the arguments `-action` and  `-entitytype`, 
please call ComingMain with the following arguments: `-showactions` and `-showentities`, resp.
You can also find those values on this [page](/docs/types.md).

#### Mining Complex Changes (i.e., Two or more changes) 


Instead of passing the action type and entity type per command line (which defines simple pattern),
we can pass to Coming complex change pattern specified in a XML file.


```
-mode mineinstance -pattern ./pattern_INS_IF_MOVE_ASSIG.xml
```


Here, `-pattern` must receive the location to an XML with the pattern specification.

This pattern is specified as follows: 


```
<pattern>
	<entity id="1" type="Assignment">
		<parent parentId="2" distance="10" />
	</entity>
	<entity id="2" type="If" />

	<action entityId="2" type="INS" />
	<action entityId="1" type="MOV" />
</pattern>

```

##### Change Pattern Specification

Coming accepts Change Patterns specified in a XML files.
As example the pattern `Add If-Return`:

```
<pattern>
	<entity id=``1" type=``Return">
		<parent parentId=``2" distance=``2" />
	</entity>
	<entity id=``2" type=``If" />
	<action entityId=``1" type=``INS" />
	<action entityId=``2" type=``INS" />
</pattern>

```
Specifies:

a) two  entities  (id  1  and  2),  one  representing  a `Return`,  the second  one  an `If`;

b) a  parent  relation  between  theifandtheReturnentities  (with  a  max  distance  of  2  nodes);  and

c) two actions of type INS (insert), one affecting the entity id 1 (i.e., the `Return`), the other one the entity id 2 (i.e., the `if`)


This pattern is able to match a changes such:

```
+  if ((n1 * n2) < MathUtils.SAFE_MIN) {
+           return ZERO;
+    }

```

That change is an *instance*  of the pattern `Add If-Return`.


##### Roles of Entities

The pattern specification also allows to specify the *role* of an entity in its parent entity.
Given the code:

```
   if (exception == null) {
-      l.connectionClosed(event);
+      l.connectionErrorOccurred(event);

...
+  if (realConnection != null)
-  if (realConnection == null)

```

The following pattern, that matches any changes inside an entity which parent is an IF, is able to detect two instances:

```
<pattern>
<entity id="1" type = "If"/>
<entity id="2" type = "*">
	<parent parentId="1" distance="10" />
</entity>
<action entityId ="2" type = "*" />
</pattern>
```

One of the instances is over the method invocation (which was an updated parameter), and the second one the operator inside the IF.

The role feature allows to specify a pattern that matches an element according to the role of the element in its parent.


For example,  the following pattern matches an element (with ID 2) which role in parent is **condition**: 

```
<pattern>

<entity id="1" type = "If"/>
<entity id="2" type = "*" role = "condition">
	<parent parentId="1" distance="10" />
</entity>

<action entityId ="2" type = "*" />

</pattern>
```

Thus, this patches will find one instance: the change inside the IF condition (update of binary operator) and it does not match with the other change (update of parameter).

However, the next pattern will uniquely match the second change: changes on an entity which parent has a role of **Then** block.

```
<pattern>
<entity id="1" type = "If"/>
<entity id="3" type = "Block" role = "Then">
	<parent parentId="1" distance="10" />
</entity>
<entity id="2" type = "*">
	<parent parentId="3" distance="10" />
</entity>
<action entityId ="2" type = "*" />
</pattern>
```

This pattern matches with the update of the method invocation's parameter (and not with the binary operator update)

The list of available Roles is presented on this [page](/docs/types.md).


### Mode Change Frequency


When running Coming in mode `-mode diff` the output is a file name `change_frequency.json` , which shows the frequency and probability of each type of change (i.e., frequency of actions applied  to each type of entities).

An example of the content of such file is:

```

{
  "frequency": [
    {
      "c": "BinaryOperator",
      "f": "6"
    },
    {
      "c": "Invocation",
      "f": "2"
    },
    {
      "c": "If",
      "f": "2"
    },
   ....
  ],
  "frequencyParent": [
    {
      "c": "INS_Invocation_Block",
      "f": "2"
    },
    {
      "c": "UPD_BinaryOperator_If",
      "f": "2"
    },
    {
      "c": "INS_If_Block",
      "f": "2"
    },
    ...
  ],


```

The file shows:

a) the frequency of affected entities within json attribute  `frequency` ([see types available](docs/types.md)).
Example, the previous json file shows

```
  "c": "BinaryOperator",
  "f": "6"
```

which means that there are 6 actions (code changes) that affect Binary Operators.


b) the frequency of Actions over affected entities and their entity parents.
Example, the previous json file shows

```
{...
      "c": "UPD_BinaryOperator_If",
      "f": "2"
    },
```
which means that there are 2 changes that update binary operators inside an if condition (i.e., the parent).

### Mode Repairability

This is a mode to find commits which look like automated program repair commits, see paper ["Estimating the Potential of Program Repair Search Spaces with Commit Analysis"](http://arxiv.org/pdf/2007.06986) (Khashayar Etemadi, Niloofar Tarighat, Siddharth Yadav, Matias Martinez and Martin Monperrus, Journal of Systems and Software, 2022).

Note that the results are sensitive to the underlying diff algorithm. If you run repairibility analysis today, you'll get results that are different from the paper. For exact reproduction, use commit [1cad74323bacad65f06ddf80ab53971d38957507](https://github.com/SpoonLabs/coming/commit/1cad74323bacad65f06ddf80ab53971d38957507) and Java 8.

When running Coming in mode `-mode repairibility`, the output is a file named `all_instances_found.json` , which shows the possible tool creating the commits. You can choose tools of interest by including the option:  `-repairtool All,Jkali,..`

An example of the content of such file is:

```
{
    {
    "instances": [
      "revision": "8c0e7110c9ebc3ba5158e8de0f73c80ec69e1001",
      "repairability": [
        {
          "tool-name": "JMutRepair",
          "pattern-name": "JMutRepair:binary_1",
          "instance_detail": [
            {
              "pattern_action": "UPD",
              "pattern_entity": {
                "entity_type": "BinaryOperator",
                "entity_new value": "*",
                "entity_role": "*",
                "entity_parent": "null"
              },
              "concrete_change": {
                "operator": "UPD",
                "src_type": "BinaryOperator",
                "dst_type": "BinaryOperator",
                "src": "sz - 1",
                "dst": "sz + 1",
                "src_parent_type": "Assignment",
                "dst_parent_type": "Assignment",
                "src_parent": "start \u003d sz - 1",
                "dst_parent": "start \u003d sz + 1"
              },
              "line": 127,
              "file": "/Users/macbook/Documents/university/internship/coming/coming/src/CharSequenceUtils.java"
            }
          ]
        }
      ]
    }
}
```

In order to perform an analysis of possible repair tools that may have generated commits use the python script at https://github.com/kth-tcs/defects4j-repair-reloaded/tree/comrepair-coming/.

create the output json file by running the script with option `-mode repairibility ` and then:

``` 
python analyse_repairability_output.py <path to the json>

``` 

or

``` 
python analyse_repairability_output.py <path to the json> <path to patches>
``` 
This script produces an output showing how many commits are corresponding to each repair tool and also (in the second choice) the number of commits it was unable to find.

Last 100 commits of the repository are analyzed by default, you can change this default with -parameters nb_commits:<your value>

### Mode Code Features

Coming can be used to compute features associated to the code changed by a commit.
This functionality can be used with the argument `-mode features`.
Coming writes in the folder specified in the `-output` a JSON file for each commit.

See [Automated Classification of Overfitting Patches with Statically Extracted Code Features](http://arxiv.org/pdf/1910.12057) (He Ye, Jian Gu, Matias Martinez, Thomas Durieux and Martin Monperrus), In IEEE Transactions on Software Engineering, 2021.

## Input Types

Coming read the input from the folder indicated by the argument `-location`. The kind of input depends on the argument `-input`. 

### git  
If `-input` is not specified, it is `git` by default. In the previous case or in the case of `-input git`, the path represented by `-location` should be a git repo.

### filespair
This input format is used to do analysis on one revision mentioned by the diff between specified the source and tha target file.
If `-input filespair`, the location argument is supposed to specified in the following format: `-location <source_file_path>:<target_file_path>`

### files 
If `-input files`, the location path should follow the following hierarchy. Note here `-location <location_arg>`.
```
<location_arg>
├── <diff_folder>
│   └── <modif_file>
│       ├── <diff_folder>_<modif_file>_s.java
│       └── <diff_folder>_<modif_file>_t.java
```
In the above case, the analysis are performed on the revision form `<diff_folder>_<modif_file>_s.java` to `<diff_folder>_<modif_file>_t.java`, where `s` stands for source and `t` stands for target.

**Example Input Specification**
```
java ... -location ./pairsD4j -input files ...

$ tree ./pairsD4j/
pairsD4j
├── Math_70
│   └── BisectionSolver
│       ├── Math_70_BisectionSolver_s.java
│       └── Math_70_BisectionSolver_t.java
└── Math_73
    └── BrentSolver
        ├── Math_73_BrentSolver_s.java
        └── Math_73_BrentSolver_t.java

4 directories, 4 files

```

## Filtering Commits

### By commit message

Coming provides a filter to discard Commits which commit message does not include some keywords


#### Bug fix keywords



For studying only commits which messages include words related to bug fixing (e.g., bug, fix, issue), add the following command.

```

-filter bugfix 
```

The bugfix keywords are predefined. If you want to use  other keywords, use the `Custom keywords`.


#### Custom keywords

For studying only commits which messages include `[MATH-`, add the following two commands:

```

-filter keywords filtervalue [MATH- 

```


#### By Number of Hunks

Coming applies line-based diff between two files (for more information, see http://en.wikipedia.org/wiki/Diff).

To filter a Commit according to the number of hunks:

```

-filter numberhunks -parameters:max_nb_hunks:2  

```
 

Here, in attribute `-filter` indicates that Commits are filtered according to max number of hunks (value `numberhunks`).
Then, using the argument`-parameters` we specify `max_nb_hunks:2` which means max number of hunks per modified file is 2.



#### By number of modified files

The arguments:

`-filter maxfiles  -parameters max_files_per_commit:1`

consider commits with at least one file modified, added or deleted.


#### Combining several filters

We can combine the two precedent filters:

``` 
-filter numberhunks:maxfiles  -parameters max_nb_hunks2:max_files_per_commit:1
```

#### By presence of Tests

The argument `-filter withtest` indicates that only commits with at least one modification on test cases are considered.

### By number of AST changes

Coming filters a commit according to the number of AST changes involved in that commit.
If a commit modified a file `f` by introducing more changes than `MAX_AST_CHANGES_PER_FILE` or less than `MIN_AST_CHANGES_PER_FILE`, then those changes are not further considered by Coming. This means that his filter has a direct impact on the Analyzers based on AST changes such as pattern mining or change frequency: Coming will not apply those analyzers over `f`.

To use this filter, add to the command line:
```
-parameters MIN_AST_CHANGES_PER_FILE:0:MAX_AST_CHANGES_PER_FILE:50
```



## Extending Coming

To extend Coming, please read the document [Extension points of Coming](./docs/extension_points.md)
Moreover, you can also read [code_walk-through](./docs/code_walkthrough.md).

