package fr.inria.prophet4j.utility;

public class Option {
    public enum DataOption {
        // human patches: program-repair/defects4j-dissection/tree/9d5aeea14e2e2e0c440f8b6970f1b278fc5e2271/projects
        // generated patches: github.com/kth-tcs/overfitting-analysis/tree/master/data/Training/patched_cardumen/
        CARDUMEN,
        // human patches: https://github.com/monperrus/bug-fixes-saner16
        SANER,
        // https://github.com/kth-tcs/overfitting-analysis/tree/master/data
        // following 4 ones are all used for Project ODS(OverfittingDetectionSystem)
        BEARS,
        BUG_DOT_JAR,
        BUG_DOT_JAR_MINUS_MATH, // BUG_DOT_JAR without Math (-16)
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
        BUG_DOT_JAR_MINUS_MATH, // only valid for the BUG_DOT_JAR_MINUS_MATH DataOption
        QUIX_BUGS, // only valid for the QUIX_BUGS DataOption
    }

    public enum FeatureOption {
        // based on EXTENDED
        ENHANCED, // appended more ways of crossing features (POS_VF_RF_CT POS_VF_AF_CT AF_RF_CT VF_RF_CT)
        // based on ORIGINAL
        EXTENDED, // appended more features and reorganized them
        ORIGINAL, // Prophet4J
        S4R, // SKETCH4REPAIR (sri-lab@ETH)
    }

    public DataOption dataOption;
    public PatchOption patchOption;
    public FeatureOption featureOption;

    public Option() {
        this.dataOption = DataOption.CARDUMEN;
        this.patchOption = PatchOption.CARDUMEN;
        this.featureOption = FeatureOption.ORIGINAL;
    }

    public enum RankingOption {
        // github.com/kth-tcs/overfitting-analysis/tree/master/data/Training/
        D_HUMAN,
        D_CORRECT,
        D_INCORRECT,
    }
}
