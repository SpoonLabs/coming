# Extension Points of Coming


Mining provides a set of Extension points which allow to override the default behavior of Coming or to add new functionality.
A code walk-through of Coming is presented [here](code_walkthrough.md).


##  Input Reader

The value of argument `-input` can include the name of a class that explores a source of information (e.g. a Git repo, a SVN repo, a file system).
The class must extend from `RevisionNavigationExperiment` and must be added to the classpath.

Accepted values:

a) `git`: Analyzes a git repository

b) `files`: Analyzes a set of files

c)  class name of the input reader to execute.

### Input Location
The `-input` argument must be complemented with the argument `-location`, which indicates the location of the input (e.g. the location in the file system of the Git repository to analyze).


## Execution modes (Analyzers to run)


The value of parameter `mode`  can have the class names of the Analyzer to consider. That class must implement the interface `Analyzer`.
You can add more than one Analyzers by separating them using the char Classpath separator (e.g., ':').

Accepted values:

a) `diff`: AST diff analyzis

b) `mineinstance`: mining of change pattern instances

c) class name of the analyzer to execute, e.g., `myapp.core.MyClassAnalyzer`


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

### Creating your own post-output processor

The interface `IOutput` defines two methods to implements:

```
public interface IOutput {
	/**
	 * Compute output for the final results
	 * 
	 */
	public void generateFinalOutput(FinalResult finalResult);

	/**
	 * Compute the outputs for the results of a revision
	 * 
	 */
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed);
  ```
  One of them, `generateRevisionOutput(RevisionResult resultAllAnalyzed)` receives the results from a revision (e.g., commit) and is invoked *just after* this revision is analyzed.
  The other method,  `generateFinalOutput(FinalResult finalResult)` received the results from the analysis of *all* the revision. This method is invoked at the end of the execution, i.e., once all revision were analyzed.
  
Important:  
By default, Coming does not generate the output just after analyzing the revision (i.e., it does not call method `generateRevisionOutput`). Coming stores the results in memory and at the end it calls method `generateFinalOutput`.
To store the result by revision, pass the property `outputperrevision:true` (using command line argument `-parameters outputperrevision:true:P1:V1:P2:V2` ).
Moreover, to avoid saving the results -which can be memory consuming for large repositories- , pass the property `save_result_revision_analysis:false`


  
  
  ### API for manipulating the results the results
  
The method `run` from Coming (which is invoked by the method `main`)  returns the final results.
For example:

```
    ComingMain cm = new ComingMain();
		Object result = cm.run(new String[] { "-location", "repogit4testv0", "-hunkanalysis", "true" });
		CommitFinalResult cfres = (CommitFinalResult) result;
		Map<Commit, RevisionResult> commits = cfres.getAllResults();
```

There, the map `commits` has the results of each commit.

Coming also provides a call back to obtain the results of each revision just after it is analyzed.

```
  ComingMain cm = new ComingMain();
  Boolean created = cm.createEngine(new String[] { "-location", "repogit4testv0", "-hunkanalysis", "true" });
	cm.registerIntermediateCallback(new IntermediateResultProcessorCallback() {
	int currentIndex = 0;
			@Override
			public void handleResult(RevisionResult result) {
				System.out.println("callback " + currentIndex);
				currentIndex++;
			}
		});
		// Start the analysis
		FinalResult finalresult = cm.start();
```

  ### Analyzing the Result from one revision.
  
  Coming allows to apply different analyzers to a particular revision.
  The entity `RevisionResult` contains the results from each analyzed appyied to a revision.
  It is a `Map` where the keys are the class names of the analyzed applied over the revision, and the values are the results that each analyzer produces. (Coming also provides a method `getResultFromClass` that receives a class and returns the result from that class).
  
  For example:
  
  ```
  AnalysisResult resultFromDiffAnalysis = previousResults.getResultFromClass(FineGrainDifftAnalyzer.class);
	DiffResult diffResut = (DiffResult) resultFromDiffAnalysis;
```
 
 All results inherit from class `AnalysisResult`. Then, it's possible to cast the results according to the analyzer.

 

## Format of the Pattern Specification

The value of argument `-patternparser` can include the name of a class that loads a change pattern.
The class must implement the interface `PatternFileParser`.
By default, Coming uses a XML parser (value `xmlparser`).



