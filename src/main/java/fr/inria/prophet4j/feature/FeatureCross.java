package fr.inria.prophet4j.feature;

import java.util.*;

// entity to express characteristic by crossing corresponding features
public interface FeatureCross {
    Integer getId();

    Double getDegree();

    List<Feature> getFeatures();

    boolean containFeature(Feature feature);
}
