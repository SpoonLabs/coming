[![Travis Build Status](https://travis-ci.org/Spirals-Team/coming.svg?branch=master)](https://travis-ci.org/Spirals-Team/coming)

Coming
=======
Coming is a tool for mining git repositories

If you use Coming, please cite: 

**[short]** [Automatically Extracting Instances of Code Change Patterns with AST Analysis](https://hal.inria.fr/hal-00861883/file/paper-short.pdf) (Martinez, M.; Duchien, L.; Monperrus, M.) IEEE International Conference on Software Maintenance (ICSM), pp.388-391, 2013, doi: 10.1109/ICSM.2013.54

    @INPROCEEDINGS{coming2015, 
    author={Martinez, M. and Duchien, L. and Monperrus, M.}, 
    booktitle={IEEE International Conference on Software Maintenance (ICSM)}, 
    title={Automatically Extracting Instances of Code Change Patterns with AST Analysis}, 
    year={2013}, 
    pages={388-391},
    doi={10.1109/ICSM.2013.54}, 
    ISSN={1063-6773},  
    month={Sept},}

**[long version]** [Accurate Extraction of Bug Fix Pattern Occurrences using Abstract Syntax Tree Analysis](https://hal.archives-ouvertes.fr/hal-01075938/file/bug-fix-pattern-identification.pdf) (Matias Martinez, Laurence Duchien and Martin Monperrus), Technical report hal-01075938, Inria, 2014

    @techreport{martinez:hal-01075938,
    title = {{Accurate Extraction of Bug Fix Pattern Occurrences using Abstract Syntax Tree Analysis}},
    author = {Martinez, Matias and Duchien, Laurence and Monperrus, Martin},
    url = {https://hal.archives-ouvertes.fr/hal-01075938/file/bug-fix-pattern-identification.pdf},
    type = {Technical Report},
    number = {hal-01075938},
    institution = {{Inria}},
    year = {2014},
    }
    
Contact: Matias Martinez http://www.martinezmatias.com.ar/, Martin Monperrus http://www.monperrus.net/martin/

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


If the compilation fails, please try to Clone and install (using mvn) two dependencies: [Astor](https://github.com/SpoonLabs/astor) and [GTSpoon](https://github.com/SpoonLabs/gumtree-spoon-ast-diff)
 (We are working to solve this dependency problem)
Run
------

The main class is: `fr.inria.coming.main.ComingMain`.

# Mining simple Patterns



Extract all commits of repogit4testv0 that insert a binary operator AST node


```
java -classpath ./coming.jar fr.inria.coming.main.ComingMain -location  ./repogit4testv0/ -mode mineinstance -action INS -entitytype BinaryOperator   -output ./out
```

The argument `-mode` indicates the analyzer that Coming will use.
The value `-mode mineinstance` means to detect instances of a change pattern (in the previous example, insert a binary operator AST node).

The argument  `-output` is used to indicate the folder where Coming will write the results.


# Filtering Commits

## By commit message

Coming provides a filter to discard Commits which commit message does not include some keywords


### Bug fix keywords



For studying only commits which messages include words related to bug fixing (e.g., bug, fix, issue), add the following command.

```

-filter bugfix 
```



### Custom keywords

For studying only commits which messages include `[MATH-`, add the following two commands.

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

``` 
-filter maxfiles  -parameters max_files_per_commit:1
```


# Combining several filters

We can combine the two precedent filters:

``` 
-filter numberhunks:maxfiles  -parameters max_nb_hunks2:max_files_per_commit:1
```

## By test

```
-filter withtest
```

Indicates that only commits with at least one modification on test cases are considered.


# Default Values 


Most of the properties are configured in file "configuration.properties"

One can change any of those properties from the command line by using   `-parameters`

The value of those argument are the following format `<name_property_1>:<value_property_1>:<name_property_2>:<value_property_2> `

In the following command we change the value of two properties: `max_nb_hunks` and `max_files_per_commit` 

``` 
   -parameters max_nb_hunks2::max_files_per_commit:1
```



# Change Pattern specification


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


# Output

Coming writes the output in the folder indicated by the argument ` -output `


When running Coming in mode `-mode mineinstance` the output is a file name `instances_found.json` , which shows the different instances.

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




Other information
---------

Most of the properties are configured in file "configuration.properties"

MAX_LINES_PER_HUNK --> MAX size of diff hunks (for more information, see http://en.wikipedia.org/wiki/Diff)

MAX_HUNKS_PER_FILECOMMIT --> MAX number of hunks per file in the commit

MAX_FILES_PER_COMMIT --> Max number of files in the commit 

MAX_AST_CHANGES_PER_FILE --> max number of AST changes per file

MIN_AST_CHANGES_PER_FILE --> min number of AST changes per file

excludeCommitWithOutTest--> indicates whether a commits without test modification is excluded
