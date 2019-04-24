package fr.inria.prophet4j.utility;

public class Option {
    public enum DataOption {
        // human patches: program-repair/defects4j-dissection
        // github.com/program-repair/defects4j-dissection/tree/9d5aeea14e2e2e0c440f8b6970f1b278fc5e2271/projects
        // generated patches: kth-tcs/overfitting-analysis(/dataport/Training/patched_cardumen/)
        CARDUMEN,
        // human patches: https://github.com/monperrus/bug-fixes-saner16
        SANER,
        // https://github.com/kth-tcs/overfitting-analysis/tree/master/data
        // following 4 ones are all used for Project ODS(OverfittingDetectionSystem)
        BEARS,
        BUG_DOT_JAR,
        DEFECTS4J,
        QUIX_BUGS,
    }

    // append other candidate-patches generators todo consider
    public enum PatchOption {
        CARDUMEN, // only valid for the CARDUMEN DataOption
        // temporary patch
        SPR, // SPR implemented in Java
        // following 4 ones are all used for Project ODS(OverfittingDetectionSystem)
        BEARS, // only valid for the BEARS DataOption
        BUG_DOT_JAR, // only valid for the BUG_DOT_JAR DataOption
        DEFECTS4J, // only valid for the DEFECTS4J DataOption
        QUIX_BUGS, // only valid for the QUIX_BUGS DataOption
    }

    public enum FeatureOption {
        ENHANCED, // appended more ways of crossing features, based on EXTENDED
        EXTENDED, // appended more features and rearranged them, based on ORIGINAL
        ORIGINAL, // Prophet4J
        S4R, // SKETCH4REPAIR (sri-lab@ETH)
    }

    public DataOption dataOption;
    public PatchOption patchOption;
    public FeatureOption featureOption;

    // PatchOption.CARDUMEN can only pair with DataOption.CARDUMEN
    public Option() {
        this.dataOption = DataOption.CARDUMEN;
        this.patchOption = PatchOption.CARDUMEN;
        this.featureOption = FeatureOption.ORIGINAL;
    }

    public enum RankingOption {
        D_HUMAN,
        D_CORRECT,
        D_INCORRECT,
    }
}
