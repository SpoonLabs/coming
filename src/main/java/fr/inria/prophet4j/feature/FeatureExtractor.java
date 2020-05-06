package fr.inria.prophet4j.feature;

import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.Repair;
import spoon.reflect.declaration.*;

// based on FeatureExtract.cpp, RepairGenerator.cpp
public interface FeatureExtractor {
    // this is for CodeDiffer.java
    FeatureVector extractFeature(Repair repair, CtElement atom);
    FeatureVector extractSimpleP4JFeature(Repair repair, CtElement atom);
}
