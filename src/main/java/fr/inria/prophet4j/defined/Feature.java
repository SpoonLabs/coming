package fr.inria.prophet4j.defined;

public interface Feature {
    int POS_SIZE = Position.values().length;

    enum Position implements Feature {
        POS_C, // current line
        POS_P, // previous lines
        POS_N, // next lines
    }

    enum CrossType implements Feature {
        RF_CT, // RepairFeatureNum     = RepairFeatureNum
        POS_AF_RF_CT, // GlobalFeatureNum     = 3 * AtomFeatureNum * RepairFeatureNum
        POS_AF_AF_CT, // VarCrossFeatureNum   = 3 * AtomFeatureNum * AtomFeatureNum
        AF_VF_CT, // ValueCrossFeatureNum = AtomFeatureNum * ValueFeatureNum
    }
}
