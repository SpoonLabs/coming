# Extension Points of Coming


Mining provides a set of Extension points which allow to override the default behavior of Coming or to add new functionality.

(Page under construction)

##  Input

The value of argument `-input` can include the name of a class that explores a source of information (e.g. a Git repo, a SVN repo, a file system).
The class must extend from `RevisionNavigationExperiment` and must be added to the classpath.



## Execution modes (Analyzers to run)


##  Commit Filters

In the value of parameter `filter`  you can add the class names of the filter to consider. That classes must implement the interface `IFilter`.
You can add more than one filter by separating them using the char Classpath separator (e.g., ':').

For example `-filter bugfix:myclasses.Filter1:myclassesFilter2`, indicates the use of 3 filters: `bugfix`, included in Coming, and two news  `myclasses.Filter1` and `myclassesFilter2`. Those classes must be included in the classpath.


## Post-Processors and Outputs

In the value of parameter `-outputprocessor`  you can add the class names of the filter to consider. That classes must implement the interface `IOutput`.
You can add more than one filter by separating them using the char Classpath separator (e.g., ':').

A output processor can have one or more of the following goals:

a) to apply a post-processor of the results from the analysis of each commit (e.g. to present the most frequent changes, change patterns, present commits with a given feature)

b) to present the results from specific analyzers (e.g., to export instances of change pattern in a given format such as JSON, XML)


## Format of the Pattern Specification