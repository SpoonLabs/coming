package fr.inria.coming.changeminer.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FinalResult {

	protected List<VersionResult> allVersionResults = new ArrayList<>();

	/**
	 * By Default, it adds the results to a list.
	 * 
	 * @param versionResult
	 */
	public void processResult(VersionResult versionResult) {
		this.allVersionResults.add(versionResult);

	}
}
