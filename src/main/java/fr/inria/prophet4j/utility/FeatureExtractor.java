package fr.inria.prophet4j.utility;

import fr.inria.prophet4j.defined.Structure.FeatureManager;
import fr.inria.prophet4j.defined.Structure.Repair;
import spoon.reflect.declaration.*;

// based on FeatureExtract.cpp, RepairGenerator.cpp
public interface FeatureExtractor {
    // this is for CodeDiffer.java
    FeatureManager extractFeature(Repair repair, CtElement atom);
}
