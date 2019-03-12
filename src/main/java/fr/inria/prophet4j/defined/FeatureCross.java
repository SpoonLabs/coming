package fr.inria.prophet4j.defined;

import java.util.*;

public interface FeatureCross {
    Integer getId();

    List<Feature> getFeatures();

    boolean containFeature(Feature feature);
}
