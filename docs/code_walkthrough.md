# Code walk-through

The entry point of Coming is the class `ComingMain`.
You can invoke the `main` method or `run`, which returns the results of the analysis (`public FinalResult run(String[] args)`). Both methods receive the arguments given in an array of Strings and call to method `createEngine`, which does the following steps.

* load Input Reader Engine: load the component that reads a given input (e.g., git repository, set of patches, etc.)

* load  Analyzers: load the analyzers that Coming will execute over the input (e.g., instance miner, change summary, etc.). It can load more than one analyzer.

* load Filters: load the input filters. A filter decides whether an element from the input (e.g., a commit from a Git repo) must be analyzed or not.

* load Outputs: load all the output processor. It can load more than one processor.


To know how to use or write a particular Reader, Analyzer, Filter or Output processor, please read the [extension points document](extension_points.md)

Once Coming executes the mentioned "load" steps, it stores in the field `navigatorEngine` the engine (which is the loaded Input Reader mentioned before) containing all the loaded components (filters, analyzes, outputs).
Then, Coming starts the processing by calling Coming's method `start`, which calls the method `analyze` from the engine field `navigatorEngine`. That call returns the final result (that one will also returned by method  `run` as explained before).

The method `analyze` from the engine is one of the most important methods in Coming.
It carries out the next steps:

* Load the data that will be analyzed.

* For each item `I` of the load data (e.g., a commit) modeled by class `IRevision`

* Coming applies the filters over `I` to decide if accept or discard it.

* If `I`is accepted, then Coming iterates over the load analyzers and executes the method `analyze` passing two arguments: 1) the revision item `I`, and  2) the results over previous analyzers applied over `I` (note that the first analyzer always receives an empty result).  The order of iteration is given by the loading order, which in turn is the order or the analyzers in the command line.

```
public AnalysisResult analyze(T input, RevisionResult previousResults);
```

The class `AnalysisResult`, which is the output of one analyzer over one particular revision item, is then stored inside an instance of class `RevisionResult`. It stores the results of all the analyzers for the particular item. That instance correspond to the mentioned second argument of method `analyze`.  `RevisionResult` is a map where the keys are the class name of the analyzers and the values are the `AnalysisResult` obtained.


* After applying all the analyzers over `I`, Coming calls method `processEndRevision` to process the output obtained from each analyzer on `I `. For example, Coming can store in that moment the output into a Json file or to trigger a callback mechanism.


* Finally, once all revision items were analyzed, Coming calls method `processEnd`  to process all the outputs. For example, Coming is able to export -in that moment- all the results into a Json file  or to summary the results.


* The final result, modeled by class `FinalResult`, is also a map: keys are revision items (e.g., Commits, Patchs, etc.) and the values are the instances of `RevisionResult`.






 
