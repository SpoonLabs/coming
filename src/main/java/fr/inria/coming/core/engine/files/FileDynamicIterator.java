package fr.inria.coming.core.engine.files;

import java.io.File;
import java.util.Iterator;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.entities.interfaces.RevisionOrder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileDynamicIterator implements Iterator<IRevision>, RevisionOrder<IRevision> {

	private int currentFile = 0;

	/**
	 * The folder where all the diff are located
	 */
	protected File rootDirectory = null;
	/**
	 * The numbers of files from the dataset
	 */
	protected int totalNumberFiles = 0;

	public FileDynamicIterator(File rootDirectory) {
		super();
		this.rootDirectory = rootDirectory;
		this.totalNumberFiles = rootDirectory.listFiles().length;
	}

	@Override
	public boolean hasNext() {

		return currentFile < totalNumberFiles;
	}

	@Override
	public FileDiff next() {

		if (currentFile < totalNumberFiles) {
			File diffFolderSrcTgtName = rootDirectory.listFiles()[currentFile];
			currentFile++;

			if (".DS_Store".equals(diffFolderSrcTgtName.getName()))
				return this.next();

			FileDiff fileDiff = new FileDiff(diffFolderSrcTgtName);
			return fileDiff;

		} else {
			return null;
		}

	}

	@Override
	public Iterator<IRevision> orderOfNavigation(RevisionDataset<IRevision> data) {

		return this;
	}

}
