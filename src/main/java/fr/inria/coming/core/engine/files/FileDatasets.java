package fr.inria.coming.core.engine.files;

import java.io.File;
import java.util.Iterator;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionDataset;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileDatasets extends RevisionDataset<IRevision> {

	/**
	 * The folder where all the diff are located
	 */
	protected File rootDirectory = null;

	public FileDatasets(File rootDirectory) {
		super();
		this.rootDirectory = rootDirectory;

	}

	@Override
	public Iterator<IRevision> getIterator() {

		return new FileDynamicIterator(rootDirectory);
	}

	@Override
	public int size() {

		return this.rootDirectory != null ? rootDirectory.listFiles().length : 0;
	}

}
