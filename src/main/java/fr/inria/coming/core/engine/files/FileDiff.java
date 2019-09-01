package fr.inria.coming.core.engine.files;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileDiff implements IRevision {
	Logger log = Logger.getLogger(FileDiff.class.getName());

	protected File diffFolder = null;

	public FileDiff(File diffFolder) {
		super();
		this.diffFolder = diffFolder;
	}

	@Override
	public List<IRevisionPair> getChildren() {
		if (this.diffFolder == null) {
			log.info("Diff folder == null");
//			System.out.println("Diff folder == null");
			return null;
		}
		List<IRevisionPair> pairs = new ArrayList<>();
		try {
			for (File fileModif : diffFolder.listFiles()) {

				if (".DS_Store".equals(fileModif.getName()))
					continue;

				String pathname = calculatePathName(fileModif);

				String filename = fileModif.getName().trim();
				if (ComingProperties.getPropertyBoolean("excludetests")
						&& (filename.startsWith("Test") || filename.endsWith("Test"))) {
					log.debug("Ignore test: " + pathname);
					continue;
				}

				File previousVersion = new File(pathname.trim() + "_s.java");
				File postVersion = new File(pathname.trim() + "_t.java");

				if (!previousVersion.exists() || !postVersion.exists()) {
					log.debug("Missing file in diff " + pathname + " " + diffFolder.getName());
					continue;
				}

				try {
					String previousString = new String(Files.readAllBytes(previousVersion.toPath()));
					String postString = new String(Files.readAllBytes(postVersion.toPath()));

					FilePair fpair = new FilePair(previousString, postString, getName(fileModif));
					pairs.add(fpair);

				} catch (Exception e) {
					e.printStackTrace();
				}
				;
			}
		} catch (Exception e) {
			log.error("Error analyzing " + diffFolder);
//			System.err.println("Error analyzing " + diffFolder);
			e.printStackTrace();
		}

		return pairs;
	}

	private String getName(File fileModif) {
		if (!ComingProperties.getPropertyBoolean("file_complete_name")) {
			return fileModif.getName();
		} else {
			String location = ComingProperties.getProperty("location");
			if (location != null) {
				return fileModif.getAbsolutePath().replace(location, "");
			} else
				return fileModif.getAbsolutePath();

		}

	}

	public String calculatePathName(File fileModif) {
		return
		// The folder with the file name
		fileModif.getAbsolutePath() + File.separator
		// check if add the revision name in the file name
				+ (ComingProperties.getPropertyBoolean("excludecommitnameinfile") ? "" : (diffFolder.getName() + "_"))
				// File name
				+ fileModif.getName()
		// TODO:
		// + "_0"
		;

	}

	@Override
	public String getName() {

		return this.diffFolder != null ? this.diffFolder.getName() : null;
	}

	@Override
	public String toString() {
		return "FileDiff [diffFolder=" + diffFolder + "]";
	}
	
	public String getFolder() {
		return diffFolder+"";
	}

}
