package fr.inria.coming.codefeatures;

import com.github.difflib.text.DiffRow;
import com.google.gson.JsonElement;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FeaturesResult extends AnalysisResult<IRevision> {

	JsonElement features = null;

	public FeaturesResult(IRevision analyzed, JsonElement features) {
		super(analyzed);
		this.features = features;
	}

	public JsonElement getFeatures() {
		return features;
	}

	public void setFeatures(JsonElement features) {
		this.features = features;
	}

}
