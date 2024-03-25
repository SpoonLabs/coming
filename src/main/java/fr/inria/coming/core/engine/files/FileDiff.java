package fr.inria.coming.core.engine.files;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;

/**
 * Represents a diff folder, with the files to analyze
 * There is a pair of files FOO_s.java and FOO_t.java
 * s for source t for target
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
		if (this.diffFolder == null || !this.diffFolder.isDirectory()) {
			log.info("Diff folder skipped "+diffFolder.getName());
			return Collections.EMPTY_LIST;
		}
		try {
			// In the case when the revision to analyze has only one file and it does not
			// have a folder per file
			if (ComingProperties.getPropertyBoolean("onefile")) {
				List<IRevisionPair> pairs = new ArrayList<>();

				File previousVersion = null;
				File postVersion = null;
				for (File fileModif : diffFolder.listFiles()) {

					if (fileModif.getAbsolutePath().endsWith("_s.java")) {
						previousVersion = fileModif;
					} else if (fileModif.getAbsolutePath().endsWith("_t.java")) {
						postVersion = fileModif;
					}

				}
				try {
					if (previousVersion != null && postVersion != null) {
						String previousString = new String(Files.readAllBytes(previousVersion.toPath()));
						String postString = new String(Files.readAllBytes(postVersion.toPath()));

						FilePair fpair = new FilePair(previousString, postString, previousVersion.getName(), postVersion.getName());
						pairs.add(fpair);
					} else {
						log.info("Missing file in pair: " + diffFolder.getAbsolutePath());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				return extractFromFolder(this.diffFolder);
			}
		} catch (Exception e) {
			log.error("Error analyzing " + diffFolder);
			e.printStackTrace();
		}

		throw new IllegalArgumentException("Error analyzing " + diffFolder);
	}

	private List<IRevisionPair> extractFromFolder(File folder) {
		System.out.println("Analyzing diff folder: " + folder.getName());
		List<IRevisionPair> pairs = new ArrayList<>();
		// Normal behavious
		for (File fileSrc : folder.listFiles()) {

			if (".DS_Store".equals(fileSrc.getName()))
				continue;

			if (fileSrc.isDirectory())
				pairs.addAll(extractFromFolder(fileSrc));

			if (!fileSrc.getName().endsWith("_s.java"))
				continue;

			String pathname = calculatePathName(fileSrc);

			String filename = fileSrc.getName().trim();
			if (ComingProperties.getPropertyBoolean("excludetests")
					&& (filename.startsWith("Test") || filename.endsWith("Test"))) {
				log.debug("Ignore test: " + pathname);
				continue;
			}

			File previousVersion = new File(pathname.trim() + "_s.java");
			File postVersion = new File(pathname.trim() + "_t.java");

			if (!previousVersion.exists() || !postVersion.exists()) {
				System.err.println("Missing file in diff " + pathname + " " + folder.getName());
				throw new RuntimeException("Missing file in diff " + pathname + " " + folder.getName());
			}


			try {
				String previousString = new String(Files.readAllBytes(previousVersion.toPath()));
				String postString = new String(Files.readAllBytes(postVersion.toPath()));

				FilePair fpair = new FilePair(previousString, postString, previousVersion.getAbsolutePath(), postVersion.getAbsolutePath());
				pairs.add(fpair);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pairs;
	}

	private String getName(File fileModif) {
		if (!ComingProperties.getPropertyBoolean("file_complete_name")) {
			return FileUtil.extractFileName(fileModif.getName());
		} else {
			String location = ComingProperties.getProperty("location");
			if (location != null) {
				return fileModif.getAbsolutePath().replace(location, "");
			} else
				return fileModif.getAbsolutePath();

		}

	}

	private String getNameFromFile(File fileModif) {
		String result = getName(fileModif);
		return result.replace("_s", "").replace("_t", "");
	}

	public String calculatePathName(File fileModif) {
		return
		// The folder with the file name without the suffix
		fileModif.getAbsolutePath().replace("_s.java", "");
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
		return diffFolder + "";
	}

}
