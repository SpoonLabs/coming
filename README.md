
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
mvn install:install-file -Dfile=lib/gt-core-1.0-SNAPSHOT.jar -DgroupId=fr.labri.gumtree -DartifactId=core -Dversion=1.0-SNAPSHOT.jar -Dpackaging=jar
mvn install:install-file -Dfile=lib/genjdt.jar -DgroupId=fr.labri.gumtree -DartifactId=gen -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
mvn -Dskiptest compile
```

Architecture
------
Coming is composed of 5 maven modules: the parent (without code) and 4 java modules:

* gitrepoanalyzer: provides the functionality for navigate a repository using jgit library from eclipse.
* ccore: provides the core of coming. For instance, the functionality to mine change pattern. The core delagates abstracts representation of code such as AST from JDT or Spoon to the other two modules.
* SpoonModule: provides the code to manipulate Spoon  model. 
* JdtModule: provides the code to manipulate JDT model.

###We recommend to see test cases from SpoonModule and JdtModule to observe how coming works.

Other information
---------
Examples of usage are in class MainTest.

Most of the properties are configured in file "configuration.properties"

MAX_LINES_PER_HUNK --> MAX size of diff hunks (for more information, see http://en.wikipedia.org/wiki/Diff)

MAX_HUNKS_PER_FILECOMMIT --> MAX number of hunks per file in the commit

MAX_FILES_PER_COMMIT --> Max number of files in the commit 

MAX_AST_CHANGES_PER_FILE --> max number of AST changes per file

MIN_AST_CHANGES_PER_FILE --> min number of AST changes per file

excludeCommitWithOutTest--> indicates whether a commits without test modification is excluded
