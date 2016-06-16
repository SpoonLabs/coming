[![Travis Build Status](https://travis-ci.org/Spirals-Team/coming.svg?branch=master)](https://travis-ci.org/Spirals-Team/coming)

Coming:
========= 
##A java tool from University of Lille and Inria for mining git repositories

If you use Coming, please cite this paper 

Martinez, M.; Duchien, L.; Monperrus, M., "Automatically Extracting Instances of Code Change Patterns with AST Analysis," in Software Maintenance (ICSM), 2013 29th IEEE International Conference on , vol., no., pp.388-391, 22-28 Sept. 2013
doi: 10.1109/ICSM.2013.54
URL: http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=6676914&isnumber=6676860

    @INPROCEEDINGS{coming2015, 
    author={Martinez, M. and Duchien, L. and Monperrus, M.}, 
    booktitle={Software Maintenance (ICSM), 2013 29th IEEE International Conference on}, 
    title={Automatically Extracting Instances of Code Change Patterns with AST Analysis}, 
    year={2013}, 
    pages={388-391},
    doi={10.1109/ICSM.2013.54}, 
    ISSN={1063-6773},  
    month={Sept},}


Contact
--------
Matias Martinez http://www.martinezmatias.com.ar/

Martin Monperrus http://www.monperrus.net/martin/

Compile
------

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
