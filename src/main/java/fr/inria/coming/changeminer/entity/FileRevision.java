package fr.inria.coming.changeminer.entity;

import java.util.List;

import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileRevision implements IRevision {

	protected String name;
	protected List<IRevisionPair> children;

	public FileRevision(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<IRevisionPair> getChildren() {
		// TODO:
		/*
		 * List<IRevisionPair> data = new ArrayList<>();
		 * 
		 * if (".DS_Store".equals(fileModif.getName())) continue; for (File fileModif :
		 * difffile.listFiles()) { int i_hunk = 0; String pathname =
		 * fileModif.getAbsolutePath() + File.separator + difffile.getName() + "_" +
		 * fileModif.getName() + "_" + i_hunk; File previousVersion = new File(pathname
		 * + "_s.java"); if (!previousVersion.exists()) { continue; } File postVersion =
		 * new File(pathname + "_t.java");
		 * 
		 * RevisionPair<File> r = new RevisionPair<File>(previousVersion, postVersion);
		 * data.add(r); }
		 * 
		 * return children;
		 */

		return null;
	}

	public void setChildren(List<IRevisionPair> children) {
		this.children = children;
	}

}
