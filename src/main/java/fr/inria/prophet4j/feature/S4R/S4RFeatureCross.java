package fr.inria.prophet4j.feature.S4R;

import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.feature.FeatureCross;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static fr.inria.prophet4j.feature.S4R.S4RFeature.*;

public class S4RFeatureCross implements FeatureCross, Serializable {
    static final long serialVersionUID = 1L;
    private Integer id;
    private Double degree;
    private List<Feature> features;

    public S4RFeatureCross(Integer id) {
        this(id, 1.0);
    }

    public S4RFeatureCross(Integer id, Double degree) {
        this.id = id;
        this.degree = degree;
        this.features = new ArrayList<>();
        this.features.add(CodeFeature.values()[id]);
    }

    public S4RFeatureCross(CrossType crossType, List<Feature> features) {
        this(crossType, features, 1.0);
    }

    public S4RFeatureCross(CrossType crossType, List<Feature> features, Double degree) {
        if (crossType == CrossType.CF_CT) {
            assert features.size() == 1;
            assert features.get(0) instanceof CodeFeature;
            this.id = ((CodeFeature) features.get(0)).ordinal();
        }
        this.degree = degree;
        this.features = features;
    }

    public Integer getId() {
        return id;
    }

    public Double getDegree() {
        return degree;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public boolean containFeature(Feature feature) {
        return features.contains(feature);
    }

    @Override
    public String toString() {
        return "FeatureCross: " + features;
    }

	@Override
	public List<Feature> getSimpleP4JFeatures() {
		// TODO Auto-generated method stub
		return null;
	}
}
