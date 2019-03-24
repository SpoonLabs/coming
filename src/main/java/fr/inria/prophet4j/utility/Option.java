package fr.inria.prophet4j.utility;

public class Option {
    public enum DataOption {
        // human patches: program-repair/defects4j-dissection
        // github.com/program-repair/defects4j-dissection/tree/9d5aeea14e2e2e0c440f8b6970f1b278fc5e2271/projects
        // generated patches: kth-tcs/overfitting-analysis(/dataport/Training/patched_cardumen/)
        CARDUMEN,
        // https://github.com/src-d/datasets/tree/master/PublicGitArchive/pga
        // https://pga.sourced.tech
        // https://stedolan.github.io/jq/manual/
        // https://github.com/src-d/siva-java
        // https://github.com/eclipse/jgit
        // https://github.com/centic9/jgit-cookbook
        PGA,
        // human patches: https://github.com/monperrus/bug-fixes-saner16
        SANER,
    }

    public enum PatchOption {
        CARDUMEN, // only valid for the CARDUMEN DataOption
        // temporary patch
        SPR, // SPR implemented in Java
        // append other candidate-patches generators todo consider
    }

    public enum FeatureOption {
        EXTENDED,
        ORIGINAL,
    }

    public enum ModelOption {
        // right now have best performance
        CROSS_ENTROPY,
        // faster but not so good as cross-entropy
        SUPPORT_VECTOR_MACHINE,
    }

    public DataOption dataOption;
    public PatchOption patchOption;
    public FeatureOption featureOption;
    // DO NOT set modelOption until SUPPORT_VECTOR_MACHINE being checked
    private ModelOption modelOption;

    // PatchOption.CARDUMEN can only pair with DataOption.CARDUMEN
    public Option() {
        this.dataOption = DataOption.CARDUMEN;
        this.patchOption = PatchOption.CARDUMEN;
        this.featureOption = FeatureOption.ORIGINAL;
        this.modelOption = ModelOption.CROSS_ENTROPY;
    }

    public ModelOption getModelOption() {
        return modelOption;
    }
}
