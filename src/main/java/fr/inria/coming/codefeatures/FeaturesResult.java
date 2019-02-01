package fr.inria.coming.codefeatures;

import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FeaturesResult extends AnalysisResult<IRevision> {

	List<Cntx> features = null;

	public FeaturesResult(IRevision analyzed, List<Cntx> features) {
		super(analyzed);
		this.features = features;
	}

	public List<Cntx> getFeatures() {
		return features;
	}

	public void setFeatures(List<Cntx> features) {
		this.features = features;
	}

}
