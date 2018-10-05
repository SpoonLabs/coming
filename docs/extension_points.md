# Extension Points of Coming


Mining provides a set of Extension points which allow to override the default behavior of Coming or to add new functionality.

(Page under construction)

##  Input

The value of argument `-input` can include the name of a class that explores a source of information (e.g. a Git repo, a SVN repo, a file system).
The class must extend from `RevisionNavigationExperiment` and must be added to the classpath.



## Execution modes (Analyzers to run)


##  Commit Filters

In the value of parameter `filter`  you can add the class names of the filter to consider. That classes must implement the interface `IFilter`.
You can add more than one filter by separing them using the char Classpath separator (e.g., ':').

For example `-filter bugfix:myclasses.Filter1:myclassesFilter2`, indicates the use of 3 filters: `bugfix`, included in Coming, and two news  `myclasses.Filter1` and `myclassesFilter2`. Those classes must be included in the classpath.


## Post-Processors and Outputs


## Format of the Pattern Specification