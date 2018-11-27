package fr.inria.coming.core.engine.files;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileDiff implements IRevision {

	protected File diffFolder = null;

	public FileDiff(File diffFolder) {
		super();
		this.diffFolder = diffFolder;
	}

	@Override
	public List<IRevisionPair> getChildren() {
		if (this.diffFolder == null) {
			System.out.println("Diff folder == null");
			return null;
		}
		List<IRevisionPair> pairs = new ArrayList<>();
		for (File fileModif : diffFolder.listFiles()) {
			int i_hunk = 0;

			if (".DS_Store".equals(fileModif.getName()))
				continue;

			String pathname = fileModif.getAbsolutePath() + File.separator + diffFolder.getName() + "_"
					+ fileModif.getName() /* + "_" + i_hunk */;
			File previousVersion = new File(pathname + "_s.java");
			if (!previousVersion.exists()) {
				break;
			}

			File postVersion = new File(pathname + "_t.java");
			try {
				String previousString = new String(Files.readAllBytes(previousVersion.toPath()));
				String postString = new String(Files.readAllBytes(postVersion.toPath()));

				FilePair fpair = new FilePair(previousString, postString, diffFolder.getName());
				pairs.add(fpair);

			} catch (Exception e) {
				e.printStackTrace();
			}
			;
		}

		return pairs;
	}

	@Override
	public String getName() {

		return this.diffFolder != null ? this.diffFolder.getName() : null;
	}

	@Override
	public String toString() {
		return "FileDiff [diffFolder=" + diffFolder + "]";
	}

}
