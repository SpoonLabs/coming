package fr.inria.coming.core.entities;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import org.apache.log4j.Logger;

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
public class DiffResult<T, R,K> extends AnalysisResult<T,K> {

	static Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());


	/**
	 * Filename
	 */
	Map<String, R> diffOfFiles = null;

	public DiffResult(T analyzed, Map<String, R> diffOfFiles) {
		super(analyzed);
		this.diffOfFiles = diffOfFiles;
	}

	public DiffResult(T analyzed, Map<String, R> diffOfFiles,List<K> row_list) {
		super(analyzed);
		this.diffOfFiles = diffOfFiles;
		this.row_list=row_list;
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

			try {
				r += "\n" + idiff.toString();
			} catch (Exception e) {

//				System.err.println("Error when printing diff result: " + e.getMessage());
				log.error("Error when printing diff result: " + e.getMessage());
				e.printStackTrace();
			}

		}
		return r;
	}
}
