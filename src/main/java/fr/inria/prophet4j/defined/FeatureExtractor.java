package fr.inria.prophet4j.defined;

import fr.inria.prophet4j.defined.Structure.FeatureVector;
import fr.inria.prophet4j.defined.Structure.Repair;
import spoon.reflect.declaration.*;

// based on FeatureExtract.cpp, RepairGenerator.cpp
public interface FeatureExtractor {
    // this is for CodeDiffer.java
    FeatureVector extractFeature(Repair repair, CtElement atom);
}
