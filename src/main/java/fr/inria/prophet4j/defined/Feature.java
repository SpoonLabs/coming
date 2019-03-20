package fr.inria.prophet4j.defined;

public interface Feature {
    int POS_SIZE = Position.values().length;

    enum Position implements Feature {
        POS_C, // current line
        POS_F, // former lines
        POS_L, // latter lines
    }
}
