package fr.inria.coming.core.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Matias Martinez
 * @param <T>
 *
 * @param <T>
 */
public class DiffResult<T, R> extends AnalysisResult<T> {

	/**
	 * Filename
	 */
	Map<String, R> diffOfFiles = null;

	public DiffResult(T analyzed, Map<String, R> diffOfFiles) {
		super(analyzed);
		this.diffOfFiles = diffOfFiles;
	}

	public Map<String, R> getDiffOfFiles() {
		return diffOfFiles;
	}

	public void setDiffOfFiles(Map<String, R> diffOfFiles) {
		this.diffOfFiles = diffOfFiles;
	}

	public List<R> getAll() {
		List<R> all = new ArrayList<>();

		for (R ops : this.diffOfFiles.values()) {
			all.add(ops);
		}
		return all;
	}

	public String toString() {
		String r = "";
		for (String file : this.diffOfFiles.keySet()) {
			r += "\n" + ("-" + file);

			R idiff = this.diffOfFiles.get(file);

			// List<Operation> opsFile = idiff.getRootOperations();
			// for (Operation operation : opsFile) {
			// r += "\n" + ("--op->" + operation);
			// }
			r += "\n" + idiff.toString();

		}
		return r;
	}
}
