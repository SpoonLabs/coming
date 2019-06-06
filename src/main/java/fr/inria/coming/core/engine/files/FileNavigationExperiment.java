package fr.inria.coming.core.engine.files;

import java.io.File;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.main.ComingProperties;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileNavigationExperiment extends RevisionNavigationExperiment<IRevision> {

	public File flocation = null;

	public FileNavigationExperiment() {

		this(ComingProperties.getProperty("location"));
	}

	public FileNavigationExperiment(String repositoryPath) {

		flocation = new File(repositoryPath);
		if (!flocation.exists())
			throw new IllegalArgumentException("Location does not exist: " + flocation);

		this.setNavigationStrategy(new FileDynamicIterator(flocation));
	}

	@Override
	public RevisionDataset<IRevision> loadDataset() {

		RevisionDataset dataset = new FileDatasets(flocation);

		return dataset;
	}

}
