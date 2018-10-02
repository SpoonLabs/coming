# Demo 2018

# commands executed

### Example 1 Mining Add-Method
```
java -classpath ./coming-0.1-SNAPSHOT-jar-with-dependencies.jar fr.inria.coming.main.ComingMain -location  ./commons-math/ -mode mineinstance -action INS -entitytype Method   -parameters maxrevision:150:max_nb_hunks:2  -output ./out
```

### Example 2 Mining Update-Literal

```
java -classpath ./coming-0.1-SNAPSHOT-jar-with-dependencies.jar fr.inria.coming.main.ComingMain -location  ./commons-math/ -mode mineinstance -action UPD -entitytype Literal -filter numberhunks:maxfiles  -parameters maxrevision:150:max_nb_hunks:2:max_files_per_commit:1  -output ./out
```

### Example 3 Mining Pattern Add if- Move Assignment

```
java -classpath ./coming-0.1-SNAPSHOT-jar-with-dependencies.jar fr.inria.coming.main.ComingMain -location  ./commons-math/ -mode mineinstance -pattern ./pattern_INS_IF_MOVE_ASSIG.xml  -filter keywords -filtervalue [MATH-    -output ./out
```

# Commit Version 

[1be8b2429c7ecc022d9fde9a0cdca269810ea64b](https://github.com/Spirals-Team/coming/commits/1be8b2429c7ecc022d9fde9a0cdca269810ea64b)