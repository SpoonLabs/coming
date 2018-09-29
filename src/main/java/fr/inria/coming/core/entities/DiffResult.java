package fr.inria.coming.core.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 * @param <T>
 *
 * @param <T>
 */
public class DiffResult<T> extends AnalysisResult<T> {

	/**
	 * Filename
	 */
	Map<String, Diff> diffOfFiles = null;

	public DiffResult(T analyzed, Map<String, Diff> diffOfFiles) {
		super(analyzed);
		this.diffOfFiles = diffOfFiles;
	}

	public Map<String, Diff> getDiffOfFiles() {
		return diffOfFiles;
	}

	public void setDiffOfFiles(Map<String, Diff> diffOfFiles) {
		this.diffOfFiles = diffOfFiles;
	}

	public List<Diff> getAllOps() {
		List<Diff> all = new ArrayList<>();

		for (Diff ops : this.diffOfFiles.values()) {
			all.add(ops);
		}
		return all;
	}

	public String toString() {
		String r = "";
		for (String file : this.diffOfFiles.keySet()) {
			r += "\n" + ("-" + file);

			Diff idiff = this.diffOfFiles.get(file);

			List<Operation> opsFile = idiff.getRootOperations();
			for (Operation operation : opsFile) {
				r += "\n" + ("--op->" + operation);
			}

		}
		return r;
	}
}
