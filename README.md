[![Travis Build Status](https://travis-ci.org/Spirals-Team/coming.svg?branch=master)](https://travis-ci.org/Spirals-Team/coming)

Coming
=======
Coming is a tool for mining git repositories

If you use Coming, please cite: 

**[short]** [Automatically Extracting Instances of Code Change Patterns with AST Analysis](https://hal.inria.fr/hal-00861883/file/paper-short.pdf) (Martinez, M.; Duchien, L.; Monperrus, M.) in Software Maintenance (ICSM), 2013 29th IEEE International Conference on , vol., no., pp.388-391, 22-28 Sept. 2013
doi: 10.1109/ICSM.2013.54
URL: http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=6676914&isnumber=6676860

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

Run
------

Extract all commits of repogit4testv0 that insert a binary operator AST node
```
ComingMain.main(new String[]{"repogit4testv0", "BinaryOperator","INS"});
```

Other information
---------

Most of the properties are configured in file "configuration.properties"

MAX_LINES_PER_HUNK --> MAX size of diff hunks (for more information, see http://en.wikipedia.org/wiki/Diff)

MAX_HUNKS_PER_FILECOMMIT --> MAX number of hunks per file in the commit

MAX_FILES_PER_COMMIT --> Max number of files in the commit 

MAX_AST_CHANGES_PER_FILE --> max number of AST changes per file

MIN_AST_CHANGES_PER_FILE --> min number of AST changes per file

excludeCommitWithOutTest--> indicates whether a commits without test modification is excluded
