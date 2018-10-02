package fr.inria.coming.core.engine.files;

import java.util.Collection;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class FileNavigationExperiment extends RevisionNavigationExperiment<IRevision> {

	@Override
	public Collection<IRevision> loadDataset() {
		// TODO:
		/*
		 * String dirPath = ComingProperties.getProperty("location"); File dir = new
		 * File(dirPath);
		 * 
		 * for (File difffile : dir.listFiles()) {
		 * 
		 * if (difffile.isFile() || difffile.listFiles() == null) continue;
		 * 
		 * // Accept
		 * 
		 * 
		 * 
		 * 
		 * 
		 * } }return data;
		 */
		return null;
	}

}
