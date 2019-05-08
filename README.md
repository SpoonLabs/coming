[![Travis Build Status](https://travis-ci.org/SpoonLabs/coming.svg?branch=master)](https://travis-ci.org/SpoonLabs/coming)

Coming
=======
Coming is a tool for mining git repositories

If you use Coming, please cite one of: 

* [Coming: a Tool for Mining Change Pattern Instances from Git Commits](http://arxiv.org/pdf/1810.08532). M. Martinez, M. Monperrus, Proceedings of ICSE - Demo track, 2019. [bibtex](https://www.monperrus.net/martin/bibtexbrowser.php?key=arXiv-1810.08532&bib=monperrus.bib)
* [Accurate Extraction of Bug Fix Pattern Occurrences using Abstract Syntax Tree Analysis](https://hal.archives-ouvertes.fr/hal-01075938/file/bug-fix-pattern-identification.pdf) (Matias Martinez, Laurence Duchien and Martin Monperrus), Technical report hal-01075938, Inria, 2014 [bibtex](https://www.monperrus.net/martin/bibtexbrowser.php?key=martinez%3Ahal-01075938&bib=monperrus.bib)
* [Automatically Extracting Instances of Code Change Patterns with AST Analysis](https://hal.inria.fr/hal-00861883/file/paper-short.pdf) (Martinez, M.; Duchien, L.; Monperrus, M.) IEEE International Conference on Software Maintenance (ICSM), pp.388-391, 2013, doi: 10.1109/ICSM.2013.54 [bibtex](https://www.monperrus.net/martin/bibtexbrowser.php?key=martinez%3Ahal-00861883&bib=monperrus.bib)

Contact: 

[Matias Martinez](http://www.martinezmatias.com/), [Martin Monperrus](http://www.monperrus.net/martin/)

Compile
------

Please install a JDK 1.8 and configure Maven or your IDE to use it.

```
mvn install:install-file -Dfile=lib/gumtree-spoon-ast-diff-0.0.3-SNAPSHOT-jar-with-dependencies.jar -DgeneratePom=true -DgroupId=fr.inria.spirals -DartifactId=gumtree-spoon-ast-diff -Dversion=0.0.3-SNAPSHOT -Dpackaging=jar
mvn -Dskiptest compile
```

Test
------

```
unzip ./src/main/resources/repogit4testv0.zip
mvn test
```


`repogit4testv0` is a GIT repository included inside Coming which is used by the test cases.


# Run


The main class is: `fr.inria.coming.main.ComingMain`.


####  Parameters 


Most of the properties are configured in file "configuration.properties"

One can change any of those properties from the command line by using   `-parameters`

The value of those argument are the following format `<name_property_1>:<value_property_1>:<name_property_2>:<value_property_2> `

In the following command we change the value of two properties: `max_nb_hunks` and `max_files_per_commit` 

``` 
   -parameters max_nb_hunks:2:max_files_per_commit:1
```



# Mining Simple Changes (i.e., with exactly one change)



Extract all commits of `repogit4testv0` that insert a binary operator AST node


```
java -classpath ./coming.jar fr.inria.coming.main.ComingMain -location  ./repogit4testv0/ -mode mineinstance -action INS -entitytype BinaryOperator   -output ./out
```

The argument `-mode` indicates the analyzer that Coming will use.
The value `-mode mineinstance` means to detect instances of a change pattern (in the previous example, insert a binary operator AST node).

The argument `-location` indicates the location of the project to analyze. 
By default, Coming analyzes Git projects, so the `-location`  should be the path to the cloned project. Moreover, the argument `branch` allows to specify the Git branch to analyze (by default, it analyzes  the `master` branch).



The argument  `-output` is used to indicate the folder where Coming will write the results.


## Printing Types of Actions and Entities

To know the values accepted by the arguments `-action` and  `-entitytype`, 
please call ComingMain with the following arguments: `-showactions` and `-showentities`, resp.
You can also find those values on this [page](/docs/types.md).

# Mining Complex Changes (i.e., Two or more changes) 


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


### Change Pattern Specification: 

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


#### Roles of Entities

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


# Output

Coming writes the output in the folder indicated by the argument ` -output `. The kind of output depends of the analysis executed. We now present the output of two analysis: Mining Instances and Change frequency computation.

## Mined Instances

When running Coming in mode `-mode mineinstance` the output is a file name `instances_found.json` , which shows the different instances.

An example of the content of such file is:

```
"instances": [
    {
      "revision": "3849e21f3b749ce5f428d42e88ef3f6441546968 PR: http://nagoya.apache.org/bugzilla/show_bug.cgi?id\u003d20633\nSubmitted by:\tbrent@worden.org\n\n\ngit-svn-id: https://svn.apache.org/repos/asf/jakarta/commons/proper/math/trunk@140903 13f79535-47bb-0310-9956-ffa450edef68\n",
      "ops": [
        {
          "action": "INS",
          "entity": "PatternEntity [entityName\u003dMethod, parent\u003dnull]",
          "op": {
            "operator": "INS",
            "src": "CtMethodImpl",
            "dst": "null",
            "srcparent": "CtMethodImpl",
            "dstparent": "null"
          },
          "code": "public org.apache.commons.math.stat.distribution.ExponentialDistribution createExponentialDistribution(double mean) {\n    return new org.apache.commons.math.stat.distribution.ExponentialDistributionImpl(mean);\n}",
          "location": "#subPackage[name\u003dorg]#subPackage[name\u003dapache]#subPackage[name\u003dcommons]#subPackage[name\u003dmath]#subPackage[name\u003dstat]#subPackage[name\u003ddistribution]#containedType[name\u003dDistributionFactoryImpl]#typeMember[index\u003d5]"
        }
      ]
    },
    {
    ...
```

The JSon element for one instance shows: the revision information, the operators that match with the pattern, the pattern information, and the code matched with the pattern.


## Change frequency


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


## Code Features

Coming has an option to compute the features associated to the code changed by a commit.
This functionality can be used with the argument `-mode features`.
Coming writes in the folder specified in the `-output` a JSON file for each commit.

## Specifying new outputs

Coming allows to specify new output. For example, instead of saving the results in Json files as presented before, it's
 possible to write a plug-in to store the results in a relational database.
 You can find more detail about extending Coming in our [documentation](https://github.com/SpoonLabs/coming/blob/master/docs/extension_points.md#post-processors-and-outputs).

# Filtering Commits

## By commit message

Coming provides a filter to discard Commits which commit message does not include some keywords


### Bug fix keywords



For studying only commits which messages include words related to bug fixing (e.g., bug, fix, issue), add the following command.

```

-filter bugfix 
```

The bugfix keywords are predefined. If you want to use  other keywords, use the `Custom keywords`.


### Custom keywords

For studying only commits which messages include `[MATH-`, add the following two commands:

```

-filter keywords filtervalue [MATH- 

```


## By Number of Hunks

Coming applies line-based diff between two files (for more information, see http://en.wikipedia.org/wiki/Diff).

To filter a Commit according to the number of hunks:

```

-filter numberhunks -parameters:max_nb_hunks:2  

```
 

Here, in attribute `-filter` indicates that Commits are filtered according to max number of hunks (value `numberhunks`).
Then, using the argument`-parameters` we specify `max_nb_hunks:2` which means max number of hunks per modified file is 2.



## By number of modified files

The arguments:

`-filter maxfiles  -parameters max_files_per_commit:1`

consider commits with at least one file modified, added or deleted.


## Combining several filters

We can combine the two precedent filters:

``` 
-filter numberhunks:maxfiles  -parameters max_nb_hunks2:max_files_per_commit:1
```

## By presence of Tests

The argument `-filter withtest` indicates that only commits with at least one modification on test cases are considered.




# Extending Coming

To extend Coming, please read the document [Extension points of Coming](./docs/extension_points.md)

