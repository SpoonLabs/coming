# Prophet4J

This is one tool to evaluate the correctness probability of patch (by learning existing patches).

Features
======

Right now we have two feature-sets. The original one is basically used in the vanilla prophet4j and the extended one is literally the extended version of the original one. The extended version is still in progress.

[Original Features](https://github.com/SpoonLabs/coming/blob/master/docs/features/prophet4j/OriginalFeatures.md)

[Extended Features](https://github.com/SpoonLabs/coming/blob/master/docs/features/prophet4j/ExtendedFeatures.md) (WIP)

Usage
======

We are able to run Prophet4J via CLI, but right now Prophet4J does not support assigning customized data-sets.

    ```bash
    ./prophet4j -t task -d dataOption -p patchOption -f featureOption
    ```

    Task : assign which task we want prophet4j to execute
    * (default value) `Task.LEARN`
    * (candidate values) `Task.LEARN` `Task.EVALUATE`

    DataOption : assign data-set containing both buggy files and patched files (by human patches)
    * (default value) `DataOption.CARDUMEN`
    * (candidate values) `DataOption.CARDUMEN` `~~DataOption.PGA~~` `DataOption.SANER`

    PatchOption : assign data-set containing patched files (by generated patches)
    * (default value) `PatchOption.CARDUMEN`
    * (candidate values) `PatchOption.CARDUMEN` `PatchOption.SANER`

    FeatureOption : assign feature-set containing features
    * (default value) `FeatureOption.ORIGINAL`
    * (candidate values) `FeatureOption.ORIGINAL` `FeatureOption.EXTENDED`

For Learn Task, Prophet4J runs on predefined data-sets (shown above), among them, PatchOption.CARDUMEN is valid iff DataOption.CARDUMEN.

For Evaluation Task, Prophet4J runs on predefined data-sets (not shown above).

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

others
---

[Spoon Document](http://spoon.gforge.inria.fr/index.html)

[Clang Namespace Reference](https://clang.llvm.org/doxygen/namespaceclang.html)

[CPP Reference](https://en.cppreference.com/w/)

[Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/java/index.html)
