# Extension Points of Coming


Mining provides a set of Extension points which allow to override the default behavior of Coming or to add new functionality.


##  Input

The value of argument `-input` can include the name of a class that explores a source of information (e.g. a Git repo, a SVN repo, a file system).
The class must extend from `RevisionNavigationExperiment` and must be added to the classpath.

Accepted values:

a) `diff`: AST diff analyzis

b) `mineinstance`: mining of change pattern instances

c) <class name>: class name of the analyzer to execute


## Execution modes (Analyzers to run)


The value of parameter `mode`  can have the class names of the Analyzer to consider. That class must implement the interface `Analyzer`.
You can add more than one filter by separating them using the char Classpath separator (e.g., ':').



##  Commit Filters

The value of parameter `filter`  can have the class names of the filter to consider. That class must implement the interface `IFilter`.
You can add more than one filter by separating them using the char Classpath separator (e.g., ':').

For example `-filter bugfix:myclasses.Filter1:myclassesFilter2`, indicates the use of 3 filters: `bugfix`, included in Coming, and two news  `myclasses.Filter1` and `myclassesFilter2`. Those classes must be included in the classpath.


## Post-Processors and Outputs

The value of parameter `-outputprocessor`  can have the class names of the filter to consider. That class must implement the interface `IOutput`.
You can add more than one filter by separating them using the char Classpath separator (e.g., ':').

A output processor can have one or more of the following goals:

a) to apply a post-processor of the results from the analysis of each commit (e.g. to present the most frequent changes, change patterns, present commits with a given feature)

b) to present the results from specific analyzers (e.g., to export instances of change pattern in a given format such as JSON, XML)


## Format of the Pattern Specification

The value of argument `-patternparser` can include the name of a class that loads a change pattern.
The class must implement the interface `PatternFileParser`.
By default, Coming uses a XML parser (value `xmlparser`).



