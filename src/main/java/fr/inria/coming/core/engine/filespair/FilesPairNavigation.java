package fr.inria.coming.core.engine.filespair;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.entities.interfaces.RevisionOrder;
import fr.inria.coming.main.ComingProperties;

/**
 * This class implements the `filespair` input type/format in coming. Here,
 * -input is `filespiar` and -location is supposed be in the format of
 * `source_file_path:target_file_path`. The mentioned analysis will be performed
 * on revision from the source_file to the target_file.
 *
 * @author Siddharth Yadav
 */
public class FilesPairNavigation extends RevisionNavigationExperiment<IRevision> {

	private File leftFile;
	private File rightFile;

	public FilesPairNavigation() {

		String locationArg = ComingProperties.getProperty("location");
		String[] paths = locationArg.split(File.pathSeparator);

		if (paths.length != 2)
			throw new IllegalArgumentException(
					"Paths to two files required. Give: " + paths.length + " in \"" + locationArg + "\"");

		leftFile = new File(paths[0]);
		rightFile = new File(paths[1]);

		if (!leftFile.exists())
			throw new IllegalArgumentException("Location Left does not exist: " + leftFile);
		if (!rightFile.exists())
			throw new IllegalArgumentException("Location Right does not exist: " + rightFile);

	}

	public FilesPairNavigation(File leftFile, File rightFile) {
		super();

		if (!leftFile.exists())
			throw new IllegalArgumentException("Location Left does not exist: " + leftFile);
		if (!rightFile.exists())
			throw new IllegalArgumentException("Location Right does not exist: " + rightFile);

		this.leftFile = leftFile;
		this.rightFile = rightFile;
	}

	@Override
	public RevisionDataset<IRevision> loadDataset() {

		/*
		 * create a dummy list(of size 1) so that it can be used as a list of
		 * revisions(i.e 1 in this case) for creating a dummy `RevisionDataset` and
		 * `RevisionOrder`.
		 */
		List<IRevision> list = new ArrayList<>();

		FilePairsDiff fileDiff = new FilePairsDiff(leftFile, rightFile);
		list.add(fileDiff);

		RevisionDataset<IRevision> history = new RevisionDataset<>(list);

		this.setNavigationStrategy(new FilesPairRevisionOrder(history));
		return history;
	}
}

/**
 * A Dummy RevisionOrder required to set the NavigationStrategy.. The iterator
 * returned by orderOfNavigation will always contain just one revision.
 */
class FilesPairRevisionOrder implements RevisionOrder<IRevision> {

	private RevisionDataset<IRevision> history;

	FilesPairRevisionOrder(RevisionDataset<IRevision> history) {
		this.history = history;
	}

	@Override
	public Iterator<IRevision> orderOfNavigation(RevisionDataset<IRevision> data) {
		return history.getIterator();
	}
}