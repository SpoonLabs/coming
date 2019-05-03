# Prophet4J

This is one tool to evaluate the correctness probability of patch (by learning from patches).

Features
======

Right now we have four feature-sets.
- [Original Features](https://github.com/SpoonLabs/coming/blob/master/docs/features/prophet4j/OriginalFeatures.md)
is basically features used in the vanilla prophet4j.
- [Extended Features](https://github.com/SpoonLabs/coming/blob/master/docs/features/prophet4j/ExtendedFeatures.md)
is literally the extended version of the original one, by appending more same-type features.
- Enhanced Features
is literally the enhanced version of the extended one, by appending more ways of crossing features.
- S4R Features
is basically features used in sketch4repair.

Usage (Untested)
======

We are able to run Prophet4J via CLI, but right now Prophet4J does not support assigning customized data-sets.

    ```bash
    ./prophet4j -t task -d dataOption -p patchOption -f featureOption
    ```

    task : assign which task we want prophet4j to execute
    * (default value) `LEARN`
    * (candidate values) `LEARN` `EVALUATE`

    dataOption : assign data-set containing both buggy files and patched files (by human patches)
    * (default value) `CARDUMEN`
    * (candidate values) `CARDUMEN` `SANER` `BUG_DOT_JAR`

    patchOption : assign data-set containing patched files (by generated patches)
    * (default value) `CARDUMEN`
    * (candidate values) `CARDUMEN` `SPR` `BUG_DOT_JAR`

    featureOption : assign feature-set containing features
    * (default value) `ORIGINAL`
    * (candidate values) `ORIGINAL` `EXTENDED` `ENHANCED` `S4R`

For Learn task, Prophet4J runs on predefined data-sets (shown above), among them, dataOption=SANER is valid iff patchOption=SPR.

For Evaluation task, Prophet4J runs on predefined data-sets (not shown above).

Tree
======

```
.
├── CLI.java        // command-line interface
├── Demo.java       // source-code entry-point
├── dataset         // pkg handling dataset
│   ├── DataLoader.java         // cls loading data
│   ├── DataManager.java        // cls wrapping data
│   └── PGA.java                // (marginal, used to load commits from PGA)
├── feature         // pkg defining various feature-sets
│   ├── Feature.java            // cls defining the Feature entity
│   ├── FeatureCross.java       // cls defining the FeatureCross entity
│   ├── FeatureExtractor.java   // cls extracting features
│   ├── RepairGenerator.java    // cls generating repairs
│   ├── S4R                     // pkg wrapped the S4R feature-sets
│   ├── enhanced                // pkg defined the enhanced P4J feature-sets
│   ├── extended                // pkg defined the extended P4J feature-sets
│   └── original                // pkg defined the original P4J feature-sets
├── learner         // pkg learning and evaluating (will be decoupled from `coming`)
│   ├── FeatureLearner.java     // cls learning feature-weights by cross-entropy
│   ├── RepairEvaluator.java    // cls evaluating repairs by ranking them
│   └── Tool.java               // (marginal, used as one temporary script)
└── utility         // pkg supporting other packages
    ├── CodeDiffer.java         // cls wrapping workflow
    ├── Option.java             // cls defining some configuration-options
    ├── Structure.java          // cls defining some data-classes
    └── Support.java            // cls defining some static-methods
```

_Special Note_

`Feature` is entity to express feature or namely sub-characteristic

`FeatureCross` is entity to express characteristic by crossing corresponding features

`FeatureVector` is entity which contains feature-crosses to express characteristics of each diff-operation

`FeatureMatrix` is entity which contains feature-vectors to express multi-diff-operations of each patch

`ParameterVector` is entity which contains weights for all feature-crosses

Data
======

[prophet4j-data](https://github.com/kth-tcs/overfitting-analysis/tree/master/prophet4j-data)

Demo
======

[prophet4j-ranking](https://github.com/kth-tcs/overfitting-analysis/tree/master/prophet4j-ranking)

Reference
======

Prophet
----

[Prophet Code](http://rhino.csail.mit.edu/prophet-rep/)

[Prophet Paper](https://people.csail.mit.edu/fanl/papers/prophet-popl16.pdf)

[SPR Paper](https://people.csail.mit.edu/fanl/papers/spr-fse15.pdf)

[SPR Technical Report](https://dspace.mit.edu/bitstream/handle/1721.1/95970/MIT-CSAIL-TR-2015-008.pdf)

Others
---

[Spoon Document](http://spoon.gforge.inria.fr/index.html)

[Clang Namespace Reference](https://clang.llvm.org/doxygen/namespaceclang.html)

[CPP Reference](https://en.cppreference.com/w/)

[Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/java/index.html)
