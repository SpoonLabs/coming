package fr.inria.coming.core.engine.filespair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.files.FilePair;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * This class stores the two files to be analysed in form of IRevision
 * 
 * @author Siddharth Yadav
 */
public class FilePairsDiff implements IRevision {
	Logger log = Logger.getLogger(FilePairsDiff.class.getName());

	private File left, right;

	public FilePairsDiff(File left, File right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public List<IRevisionPair> getChildren() {

		if (left == null || right == null) {
			log.info("files is null");
			return null;
		}

		List<IRevisionPair> pairs = new ArrayList<>();

		try {
			String previousString = new String(Files.readAllBytes(left.toPath()));
			String postString = new String(Files.readAllBytes(right.toPath()));

			String previousNameFile = left.getAbsolutePath();
			String postNameFile = right.getAbsolutePath();
			FilePair fpair = new FilePair(previousString, postString, previousNameFile, postNameFile);
			pairs.add(fpair);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pairs;
	}

	@Override
	public String getName() {
		return this.left != null && this.right != null ? left.getName() + "->" + right.getName() : null;
	}

	@Override
	public String toString() {
		return "FileDiff [left=" + left.getName() + ", right=" + right.getName() + "]";
	}

	@Override
	public String getFolder() {
		return "";
	}

}
