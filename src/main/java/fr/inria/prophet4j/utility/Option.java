package fr.inria.prophet4j.utility;

import java.util.StringJoiner;

public class Option {
    public enum DataOption {
        // human patches: program-repair/defects4j-dissection/tree/9d5aeea14e2e2e0c440f8b6970f1b278fc5e2271/projects
        // generated patches: github.com/kth-tcs/overfitting-analysis/tree/master/data/Training/patched_cardumen/
        CARDUMEN,
        // human patches: https://github.com/monperrus/bug-fixes-saner16
        SANER,
        // https://github.com/kth-tcs/overfitting-analysis/tree/master/data
        // following ones are for Project ODS(OverfittingDetectionSystem)
        BEARS,
        BUG_DOT_JAR_MINUS_MATH, // BUG_DOT_JAR without Math (-16)
        QUIX_BUGS,
        CLOSURE,
    }

    public enum PatchOption {
        CARDUMEN, // only valid for the CARDUMEN DataOption
        // temporary patch
        SPR, // SPR implemented in Java
        // following ones are for Project ODS(OverfittingDetectionSystem)
        BEARS, // only valid for the BEARS DataOption
        BUG_DOT_JAR_MINUS_MATH, // only valid for the BUG_DOT_JAR_MINUS_MATH DataOption
        QUIX_BUGS, // only valid for the QUIX_BUGS DataOption
    }

    public enum FeatureOption {
        // based on EXTENDED
        ENHANCED, // more ways of crossing features (POS_VF_RF_CT POS_VF_AF_CT AF_RF_CT VF_RF_CT)
        // based on ORIGINAL
        EXTENDED, // more features and reorganized them
        ORIGINAL, // Prophet4J
        S4R, // SKETCH4REPAIR (sri-lab@ETH)
        // S4RO is just for test, DO NOT use it when PatchOption = SPR
        S4RO, // Complex Option
    }

    public enum LearnerOption {
        CROSS_ENTROPY,
    }

    public DataOption dataOption;
    public PatchOption patchOption;
    public FeatureOption featureOption;
    public LearnerOption learnerOption;

    public Option() {
        this.dataOption = DataOption.CARDUMEN;
        this.patchOption = PatchOption.CARDUMEN;
        this.featureOption = FeatureOption.ORIGINAL;
        this.learnerOption = LearnerOption.CROSS_ENTROPY;
    }

    public enum RankingOption {
        D_HUMAN,
        D_CORRECT,
        D_INCORRECT,
        P_CORRECT,
        P_INCORRECT,
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("DataOption: " + this.dataOption.name());
        stringJoiner.add("PatchOption: " + this.patchOption.name());
        stringJoiner.add("FeatureOption: " + this.featureOption.name());
        return stringJoiner.toString();
    }
}
