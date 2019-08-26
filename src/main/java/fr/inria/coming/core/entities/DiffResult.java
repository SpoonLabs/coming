package fr.inria.coming.core.entities;

import com.github.difflib.text.DiffRow;
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
public class DiffResult<T, R> extends AnalysisResult<T> {

	static Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());
	public List<DiffRow> row_list;

	/**
	 * Filename
	 */
	Map<String, R> diffOfFiles = null;

	public DiffResult(T analyzed, Map<String, R> diffOfFiles) {
		super(analyzed);
		this.diffOfFiles = diffOfFiles;
	}

	public DiffResult(T analyzed, Map<String, R> diffOfFiles,List<DiffRow> row_list) {
		super(analyzed);
		this.diffOfFiles = diffOfFiles;
		this.row_list=row_list;
	}

	public List<DiffRow> getRow_list() {
		return row_list;
	}

	public void setRow_list(List<DiffRow> row_list) {
		this.row_list = row_list;
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
